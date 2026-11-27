# Standalone Rogue Commander for Android

## Distribution

Standalone Android releases are published by `.github/workflows/build-desktop-snapshot.yml`
when `rogueCommander` is pushed, alongside the existing desktop downloads and MSI.
Manual workflow dispatch on that branch uses the same release process. Other branches
do not publish standalone Android artifacts.

The GitHub release tag remains `rogue-commander-latest`. Android uses:

| File | Purpose |
| --- | --- |
| `rogue-commander-android-<version>.apk` | Signed Android application, named using `android-version.txt` |
| `assets.zip` | Matching resources, including Rogue scripts, portraits, sound and music |
| `android-version.txt` | Android display version |
| `android-build.txt` | Android build timestamp |

Desktop `version.txt` and `build.txt` are kept separate. Android binaries and resources
come only from this fork's release, including builds without `-SNAPSHOT` in the version.
Any newer Android build timestamp is eligible for an update; there is no 23-hour delay.

The app ID is `com.bennybenedek.roguecommander`, while its Java entry points remain
`forge.app.Launcher` and `forge.app.Main`. It installs beside Forge rather than replacing it.
Android 8 / API 26 is the minimum declared by the APK; Android 11 or newer and at least
6 GB of RAM are recommended. Actual compatibility must be checked on devices.

## One-time Signing Setup

Do this before the first release push. Never use Forge's key, a debug key, or a new key
for every build. All later APKs must retain this standalone signing identity.

A dedicated RSA release key has been generated outside the repository at:

```text
%USERPROFILE%\.rogue-commander\android-signing\rogue-commander.p12
%USERPROFILE%\.rogue-commander\android-signing\signing-credentials.clixml
%USERPROFILE%\.rogue-commander\android-signing\rogue-commander-certificate.pem
```

The folder is private to the Windows user that generated the key. The credential file
uses Windows user encryption; it is not a portable password backup. Back up the PKCS12
file separately and record its passwords in a secure password manager before relying
on this identity for public updates. Losing the key or its passwords prevents normal
updates of installed copies. Do not commit these files or paste their contents into chat.

Configure these repository Actions secrets in `bennybenedek/forge`:

| Secret | Value |
| --- | --- |
| `ROGUE_ANDROID_KEYSTORE_BASE64` | Base64 of the PKCS12 file |
| `ROGUE_ANDROID_KEY_ALIAS` | `rogue-commander` |
| `ROGUE_ANDROID_STORE_PASSWORD` | Keystore password |
| `ROGUE_ANDROID_KEY_PASSWORD` | Key password |

With an authenticated GitHub CLI, the following PowerShell commands configure the
secrets without displaying their values. Run these manually; repository changes do not
configure GitHub credentials or publish a release automatically.

```powershell
$signing = Import-Clixml "$HOME/.rogue-commander/android-signing/signing-credentials.clixml"
$repo = 'bennybenedek/forge'
$keystoreBase64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($signing.KeystorePath))
gh secret set ROGUE_ANDROID_KEYSTORE_BASE64 --repo $repo --body $keystoreBase64
gh secret set ROGUE_ANDROID_KEY_ALIAS --repo $repo --body $signing.Alias
$storePassword = [Net.NetworkCredential]::new('', $signing.StorePassword).Password
$keyPassword = [Net.NetworkCredential]::new('', $signing.KeyPassword).Password
gh secret set ROGUE_ANDROID_STORE_PASSWORD --repo $repo --body $storePassword
gh secret set ROGUE_ANDROID_KEY_PASSWORD --repo $repo --body $keyPassword
Remove-Variable keystoreBase64,storePassword,keyPassword,signing
```

The workflow checks that credentials exist, verifies the keystore alias, and verifies
the final APK's certificate against that key before publishing. The temporary runner
keystore is removed even if the build fails. Do not enable Maven debug logging when
signing: external signing tools receive the passwords as process arguments.

## Builds

Java 17, Maven, Android SDK platform 35, Build Tools 35.0.0 and platform-tools are
required for an APK build. Install Card-Forge's Android Maven plugin 4.6.2 JAR and POM
from its GitHub release into the matching Maven repository directory, as the workflow
does. This fork uses Maven, not a new Gradle project.

For local release signing, load the generated credentials into this process only:

```powershell
$signing = Import-Clixml "$HOME/.rogue-commander/android-signing/signing-credentials.clixml"
$env:ROGUE_ANDROID_KEYSTORE_PATH = $signing.KeystorePath
$env:ROGUE_ANDROID_KEY_ALIAS = $signing.Alias
$env:ROGUE_ANDROID_STORE_PASSWORD = [Net.NetworkCredential]::new('', $signing.StorePassword).Password
$env:ROGUE_ANDROID_KEY_PASSWORD = [Net.NetworkCredential]::new('', $signing.KeyPassword).Password
```

Build from the repository root, with the SDK path configured on this machine:

```powershell
mvn -U -B clean -P windows-linux,android-release-build install -DskipTests "-Dandroid.sdk.path=$env:ANDROID_SDK_ROOT" "-Dandroid.manifest.versionCodeUpdateFromVersion=false" "-Dandroid.manifestMerger.versionCode=1"
```

Version code `1` is for a first local test only. A device that already has a higher
version code will reject it as a downgrade. CI uses `GITHUB_RUN_NUMBER` so subsequent
published APKs increase monotonically, independently of the root player-facing version.
Do not reset that workflow's run numbering after publishing Android builds.

After signing, clear these variables from the terminal process:

```powershell
Remove-Item Env:ROGUE_ANDROID_KEYSTORE_PATH,Env:ROGUE_ANDROID_KEY_ALIAS,Env:ROGUE_ANDROID_STORE_PASSWORD,Env:ROGUE_ANDROID_KEY_PASSWORD
Remove-Variable signing
```

Signed APKs and `assets.zip` are generated under `forge-gui-android/target`.
The workflow verifies signatures, release identity, version code, non-debuggable status,
and matching embedded timestamps before renaming the selected APK for publication.
It also checks the resource archive for the Rogue roster, portraits, audio and card scripts.
Missing credentials or artifacts fail the release; an unsigned APK is never substituted.

Desktop/mobile-dev compilation alone does not produce or validate an APK. The mobile-dev
entry point remains `forge.app.Main`; its successful gameplay smoke test need not be
repeated just to configure a new launch profile. Use an isolated profile when checking
the changed startup/navigation so normal desktop saves are not modified.

## Player Installation and Updates

1. Download the versioned `rogue-commander-android-<version>.apk` from this fork's GitHub release.
2. Allow the browser or file manager to install unknown apps, then install the APK.
3. Launch Rogue Commander and grant the storage access requested by the launcher.
4. Download the matching resources when prompted. The app restarts after extraction.
5. Start or continue a run. The app opens Rogue Commander directly, not Forge's mode selector.

The APK contains its own runtime; players do not install Java. `assets.zip` is normally
downloaded by the app, not manually installed. Keep internet access for the first resource
download. Subsequent launches can use the installed matching resources offline.

When updating, allow Rogue Commander to open the downloaded APK installer if Android
requests that permission. Install over the existing copy; do not uninstall or clear app
data. Android 11+ uses the app-specific OBB directory with a `RogueCommander` subfolder.
Android 8-10 uses the separate external-storage `RogueCommander` folder. Run saves remain
under `data/rogue/` inside those locations. No data is imported from Forge's directories.

## Before Public Release

- Configure signing secrets and secure the portable key/password backup.
- Validate the workflow and compile desktop, mobile and mobile-dev.
- Check changed startup, Settings/Back and run selection in portrait and landscape.
- On a real Android device, check clean install and first resource download.
- Restart offline after resources are installed, then play a match and verify save/resume.
- Install a newer CI-signed APK over the first one and verify progress survives.
- Check coexistence with Forge and that neither app accesses the other's data.
- Check that a missing resource download does not falsely mark extraction as complete.
- Verify desktop downloads and the MSI job still work in the same serialized release.

iOS, app stores, automated public release execution and save migration are outside this change.
