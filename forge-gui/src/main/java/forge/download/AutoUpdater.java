package forge.download;

import static forge.localinstance.properties.ForgeConstants.GITHUB_FORGE_URL;
import static forge.localinstance.properties.ForgeConstants.GITHUB_RELEASES_ATOM;
import static forge.localinstance.properties.ForgeConstants.GITHUB_SNAPSHOT_URL;

import forge.gui.FThreads;
import forge.gui.GuiBase;
import forge.gui.download.GuiDownloadZipService;
import forge.gui.util.SOptionPane;
import forge.localinstance.properties.ForgePreferences;
import forge.model.FModel;
import forge.util.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;

public class AutoUpdater {
    private static final boolean VERSION_FROM_METADATA = true;
    private static final Localizer localizer = Localizer.getInstance();

    public static String[] updateChannels = new String[]{ "none", "snapshot", "release"};

    private final boolean isLoading;
    private String updateChannel;
    private String version;
    private final String buildVersion;
    private String versionUrlString;
    private String packageUrl;
    private String packagePath;
    private String buildDate = "";
    private Date snapsBuildDate;

    public AutoUpdater(boolean loading) {
        isLoading = loading;
        updateChannel = FModel.getPreferences().getPref(ForgePreferences.FPref.AUTO_UPDATE);
        buildVersion = BuildInfo.getVersionString();
    }

    public Date getSnapsBuildDate() {
        return snapsBuildDate;
    }

    public boolean attemptToUpdate(CompletableFuture<String> cf) {
        System.out.println("DEBUG: AutoUpdater.attemptToUpdate() called");
        if (!verifyUpdateable()) {
            System.out.println("DEBUG: verifyUpdateable() returned false - update check aborted");
            return false;
        }
        System.out.println("DEBUG: verifyUpdateable() passed - proceeding with update check");
        try {
            if (downloadUpdate(cf)) {
                extractAndRestart();
            } else {
                System.out.println("DEBUG: downloadUpdate() returned false - no update needed or download failed");
            }
        } catch(IOException | URISyntaxException | ExecutionException | InterruptedException e) {
            System.err.println("ERROR: Exception during update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void extractAndRestart() {
        extractUpdate();
        restartForge();
    }

    public boolean verifyUpdateable() {
        System.out.println("DEBUG: verifyUpdateable() - buildVersion: " + buildVersion);
        System.out.println("DEBUG: verifyUpdateable() - updateChannel: " + updateChannel);
        if (buildVersion.contains("GIT")) {
            System.out.println("DEBUG: Development build (GIT) - auto-updater not available");
            SOptionPane.showMessageDialog(
                "Auto-update is not available in development builds.\n\n" +
                "You are running a GIT development build from source code.\n" +
                "To use auto-update, please download and run a packaged release from:\n" +
                "https://github.com/bennybenedek/forge/releases",
                "Development Build",
                SOptionPane.INFORMATION_ICON
            );
            return false;
        }

        if (isLoading) {
            System.out.println("DEBUG: Update check during loading - skipping");
            // TODO This doesn't work yet, because FSkin isn't loaded at the time.
            return false;
        } else if (updateChannel.equals("none")) {
            System.out.println("DEBUG: Update channel not set - asking user");
            String message = localizer.getMessage("lblYouHaventSetUpdateChannel");
            List<String> options = List.of(localizer.getMessageorUseDefault("lblCancel", "Cancel"), localizer.getMessageorUseDefault("lblRelease", "Release"), localizer.getMessageorUseDefault("lblSnapshot", "Snapshot"));
            int option = SOptionPane.showOptionDialog(message, localizer.getMessage("lblManualCheck"), null, options, 0);
            if (option < 1) {
                System.out.println("DEBUG: User cancelled channel selection");
                return false;
            }
            updateChannel = options.get(option);
            System.out.println("DEBUG: User selected update channel: " + updateChannel);
        }

        if (buildVersion.contains("SNAPSHOT")) {
            if (!updateChannel.equalsIgnoreCase(localizer.getMessageorUseDefault("lblSnapshot", "Snapshot"))) {
                System.out.println("ERROR: Snapshot build versions must use snapshot update channel to work");
                System.out.println("ERROR: Current channel: " + updateChannel + ", required: Snapshot");
                SOptionPane.showMessageDialog("Your build is a SNAPSHOT version, but update channel is set to '" + updateChannel + "'.\n\nPlease set update channel to 'Snapshot' in Settings > Preferences > Updates.", "Update Channel Mismatch", SOptionPane.ERROR_ICON);
                return false;
            }

            versionUrlString = GITHUB_SNAPSHOT_URL + "version.txt";
            System.out.println("DEBUG: Using snapshot version URL: " + versionUrlString);
        } else {
            if (!updateChannel.equalsIgnoreCase(localizer.getMessageorUseDefault("lblRelease", "Release"))) {
                System.out.println("ERROR: Release build versions must use release update channel to work");
                System.out.println("ERROR: Current channel: " + updateChannel + ", required: Release");
                SOptionPane.showMessageDialog("Your build is a RELEASE version, but update channel is set to '" + updateChannel + "'.\n\nPlease set update channel to 'Release' in Settings > Preferences > Updates.", "Update Channel Mismatch", SOptionPane.ERROR_ICON);
                return false;
            }
            versionUrlString = GITHUB_RELEASES_ATOM;
            System.out.println("DEBUG: Using release version URL: " + versionUrlString);
        }

        // Check the internet connection
        System.out.println("DEBUG: Testing network connection...");
        String serverToTest;
        try {
            serverToTest = new URL(versionUrlString).getHost();
        } catch (MalformedURLException e) {
            serverToTest = "github.com";
        }
        if (!testNetConnection(serverToTest)) {
            System.out.println("ERROR: Network connection test failed for " + serverToTest);
            SOptionPane.showMessageDialog("Cannot connect to update server (" + serverToTest + ").\n\nPlease check your internet connection.", "Network Error", SOptionPane.ERROR_ICON);
            return false;
        }
        System.out.println("DEBUG: Network connection OK");

        // Download appropriate version file
        System.out.println("DEBUG: Comparing build with latest version...");
        return compareBuildWithLatestChannelVersion();
    }

    private boolean testNetConnection(String host) {
        // test against the host updates are actually fetched from;
        // releases.cardforge.org is no longer reachable and blocked all updates
        try {
            host = new URL(versionUrlString).getHost();
        } catch (MalformedURLException e) {
            host = "github.com";
        }
        try (Socket socket = new Socket()) {
            System.out.println("DEBUG: Testing connection to " + host + ":443");
            InetSocketAddress address = new InetSocketAddress(host, 443);
            socket.connect(address, 1000);
            System.out.println("DEBUG: Connection successful");
            return true;
        } catch (IOException e) {
            System.out.println("DEBUG: Connection failed: " + e.getMessage());
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    private boolean compareBuildWithLatestChannelVersion() {
        try {
            System.out.println("DEBUG: Retrieving version info...");
            retrieveVersion();

            if (buildVersion.contains("SNAPSHOT")) {
                System.out.println("DEBUG: SNAPSHOT build - checking build timestamp");
                URL url = new URL(GITHUB_SNAPSHOT_URL + "build.txt");
                System.out.println("DEBUG: Downloading build.txt from: " + url);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                snapsBuildDate = simpleDateFormat.parse(FileUtil.readFileToString(url));
                buildDate = BuildInfo.getTimestamp().toString();
                System.out.println("DEBUG: Local build timestamp:  " + buildDate);
                System.out.println("DEBUG: Remote build timestamp: " + snapsBuildDate);

                // For rogueCommander development - allow immediate updates (no 23-hour threshold)
                Date localTimestamp = BuildInfo.getTimestamp();
                if (localTimestamp == null || snapsBuildDate == null) {
                    System.out.println("ERROR: Timestamp is null - cannot compare");
                    return false;
                }

                // Return true if remote build is ANY amount newer than local build
                if (snapsBuildDate.after(localTimestamp)) {
                    System.out.println("DEBUG: Remote build is newer - update available!");
                    return true;
                } else {
                    System.out.println("DEBUG: Local build is up to date (or newer)");
                    SOptionPane.showMessageDialog("You are already running the latest version.\n\nLocal:  " + buildDate + "\nRemote: " + snapsBuildDate, "Up to Date", SOptionPane.INFORMATION_ICON);
                    return false;
                }
            }

            // Release version check
            System.out.println("DEBUG: Release build - checking version number");
            System.out.println("DEBUG: Local version:  " + buildVersion);
            System.out.println("DEBUG: Remote version: " + version);

            if (StringUtils.isEmpty(version)) {
                System.out.println("ERROR: Remote version is empty");
                SOptionPane.showMessageDialog("Could not retrieve remote version information.", "Update Check Failed", SOptionPane.ERROR_ICON);
                return false;
            }
            if (buildVersion.equals(version)) {
                System.out.println("DEBUG: Versions match - already up to date");
                SOptionPane.showMessageDialog("You are already running the latest version.\n\nCurrent version: " + buildVersion, "Up to Date", SOptionPane.INFORMATION_ICON);
                return false;
            }

            System.out.println("DEBUG: Version mismatch - assuming update available");
            return true;

        } catch (IOException e) {
            System.err.println("ERROR: IOException during version check: " + e.getMessage());
            String message;
            if (e.getMessage().contains("rogue-commander-latest")) {
                message = "No Rogue Commander builds available yet.\n\nPlease wait for the build pipeline to complete, then try again.\n\nYou can check the build status at:\nhttps://github.com/bennybenedek/forge/actions";
            } else {
                message = "Update files not found.\n\n" + e.getMessage();
            }
            SOptionPane.showOptionDialog(message, localizer.getMessage("lblError"), null, List.of("Ok"));
            return false;
        } catch (Exception e) {
            System.err.println("ERROR: Exception during version check: " + e.getMessage());
            e.printStackTrace();
            SOptionPane.showOptionDialog(e.getMessage(), localizer.getMessage("lblError"), null, List.of("Ok"));
            return false;
        }
    }

    private void retrieveVersion() throws MalformedURLException {
        if (VERSION_FROM_METADATA && updateChannel.equalsIgnoreCase(localizer.getMessageorUseDefault("lblRelease", "Release"))) {
            extractVersionFromGithubRelease();
        } else {
            URL versionUrl = new URL(versionUrlString);
            version = FileUtil.readFileToString(versionUrl);
        }
        if (updateChannel.equalsIgnoreCase(localizer.getMessageorUseDefault("lblRelease", "Release"))) {
            packageUrl = GITHUB_FORGE_URL + "releases/download/forge-" + version + "/forge-installer-" + version + ".jar";
        } else {
            packageUrl = GITHUB_SNAPSHOT_URL + "forge-installer-" + version + ".jar";
        }
    }

    private void extractVersionFromGithubRelease() {
        String releaseTag = RSSReader.getLatestReleaseTag(GITHUB_RELEASES_ATOM);
        if (releaseTag.startsWith("forge-")) {
            version = releaseTag.substring("forge-".length());
        } else {
            version = releaseTag;
        }
    }

    private boolean downloadUpdate(CompletableFuture<String> cf) throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        // TODO Change the "auto" to be more auto.
        if (isLoading) {
            // We need to preload enough of a Skins to show a dialog and a button if we're in loading
            // splashScreen.prepareForDialogs();
            return downloadFromBrowser();
        }
        String logs = snapsBuildDate == null ? "" : cf.get();
        String v = snapsBuildDate == null ? version : version + TextUtil.enclosedParen(snapsBuildDate.toString());
        String b = buildDate.isEmpty() ? buildVersion : buildVersion + TextUtil.enclosedParen(buildDate);
        String message = localizer.getMessage("lblNewVersionForgeAvailableUpdateConfirm", v, b) + logs;
        final List<String> options = List.of(localizer.getMessage("lblUpdateNow"), localizer.getMessage("lblUpdateLater"));
        if (SOptionPane.showOptionDialog(message, localizer.getMessage("lblNewVersionAvailable"), null, options, 0) == 0) {
            return downloadFromForge();
        }

        return false;
    }

    private boolean downloadFromBrowser() throws URISyntaxException, IOException {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            // Linking directly there will auto download, but won't auto-update
            desktop.browse(new URI(packageUrl));
            return true;
        } else {
            System.out.println("Download latest version: " + packageUrl);
            return false;
        }
    }

    private boolean downloadFromForge() {
        System.out.println("DEBUG: Downloading from: " + packageUrl);
        System.out.println("DEBUG: Saving to: " + System.getProperty("user.home") + "/Downloads/" + version + "-upgrade.jar");

        WaitCallback<Boolean> callback = new WaitCallback<Boolean>() {
            @Override
            public void run() {
                GuiBase.getInterface().download(new GuiDownloadZipService("Auto Updater", "the new version", packageUrl, System.getProperty("user.home") + "/Downloads/", null, null) {
                    @Override
                    public void downloadAndUnzip() {
                        System.out.println("DEBUG: Attempting to download JAR installer...");
                        packagePath = download(version + "-upgrade.jar");

                        if (packagePath != null) {
                            System.out.println("DEBUG: Download successful! Saved to: " + packagePath);
                            restartAndUpdate(packagePath);
                        } else {
                            System.err.println("ERROR: Download failed - check HTTP response code above");
                            // Show error dialog to user
                            FThreads.invokeInEdtLater(() -> {
                                SOptionPane.showMessageDialog(
                                    "Failed to download the update.\n\n" +
                                    "Check the console log for details.\n\n" +
                                    "Download manually from:\n" +
                                    "https://github.com/bennybenedek/forge/releases",
                                    "Download Failed",
                                    SOptionPane.ERROR_ICON
                                );
                            });
                        }
                    }
                }, this);
            }
        };

        SwingUtilities.invokeLater(callback);

        return false;
    }
    private void restartAndUpdate(String packagePath) {
        if (SOptionPane.showOptionDialog(localizer.getMessage("lblForgeUpdateMessage", packagePath), localizer.getMessage("lblRestart"), null, List.of(localizer.getMessage("lblOK")), 0) == 0) {
            try {
                File installer = new File(packagePath);
                if (installer.exists() && packagePath.endsWith(".jar")) {
                    // Execute the JAR installer using the same Java that's running Forge
                    String javaHome = System.getProperty("java.home");
                    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
                    ProcessBuilder pb = new ProcessBuilder(javaBin, "-jar", installer.getAbsolutePath());
                    pb.start();
                    System.out.println("DEBUG: Launched installer JAR with: " + javaBin + " -jar " + installer.getAbsolutePath());
                } else if (installer.exists()) {
                    // For non-JAR files, open the parent folder
                    final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    if (desktop != null) {
                        desktop.open(installer.getParentFile());
                    }
                } else {
                    System.err.println("ERROR: Installer file not found: " + packagePath);
                }
            } catch (IOException e) {
                System.err.println("ERROR: Failed to launch installer:");
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
    private void extractUpdate() {
        // TODO Something like https://stackoverflow.com/questions/315618/how-do-i-extract-a-tar-file-in-java
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null) {
            try {
                desktop.open(new File(packagePath).getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(packagePath);
        }
    }

    private void restartForge() {
        if (isLoading || SOptionPane.showConfirmDialog(localizer.getMessage("lblForgeHasBeenUpdateRestartForgeToUseNewVersion"), localizer.getMessage("lblExitNowConfirm"))) {
            System.exit(0);
        }
    }
}
