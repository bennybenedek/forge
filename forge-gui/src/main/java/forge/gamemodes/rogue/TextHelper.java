package forge.gamemodes.rogue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared text helpers for Rogue Commander UI markup.
 */
public final class TextHelper {
    private static final Pattern CARD_MARKER_PATTERN = Pattern.compile("\\[\\[(.+?)]]");

    private TextHelper() {
    }

    public static String stripCardMarkers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher matcher = CARD_MARKER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1).trim()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String extractFirstCardName(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        Matcher matcher = CARD_MARKER_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        String cardName = matcher.group(1).trim();
        return cardName.isEmpty() ? null : cardName;
    }
}
