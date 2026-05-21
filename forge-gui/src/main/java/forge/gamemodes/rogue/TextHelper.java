package forge.gamemodes.rogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared text helpers for Rogue Commander UI markup.
 */
public final class TextHelper {
    private static final int INNER_MARKER_GROUP = 1;
    private static final int CARD_PREVIEW_GROUP = 2;
    private static final int KEYWORD_PREVIEW_GROUP = 3;
    private static final Pattern CARD_MARKER_PATTERN = Pattern.compile("\\[\\[(.+?)]]");
    private static final Pattern KEYWORD_MARKER_PATTERN = Pattern.compile("\\{\\{(.+?)}}");
    private static final Pattern PREVIEW_MARKER_PATTERN = Pattern.compile("(\\[\\[(.+?)]]|\\{\\{(.+?)}})");

    private TextHelper() {
    }

    public static String stripCardMarkers(String text) {
        return stripPreviewMarkers(text);
    }

    public static String stripPreviewMarkers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String withoutCardMarkers = stripCardPattern(text);
        return stripPattern(withoutCardMarkers, KEYWORD_MARKER_PATTERN);
    }

    private static String stripCardPattern(String text) {
        Matcher matcher = CARD_MARKER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group(INNER_MARKER_GROUP).trim();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(extractCardDisplayName(token)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String stripPattern(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(INNER_MARKER_GROUP).trim()));
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

        String cardName = extractCardDisplayName(matcher.group(1).trim());
        return cardName.isEmpty() ? null : cardName;
    }

    public static List<PreviewReference> extractPreviewReferences(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        LinkedHashMap<String, PreviewReference> cards = new LinkedHashMap<>();
        LinkedHashMap<String, PreviewReference> keywords = new LinkedHashMap<>();
        Matcher matcher = PREVIEW_MARKER_PATTERN.matcher(text);
        int order = 0;

        while (matcher.find()) {
            if (matcher.group(CARD_PREVIEW_GROUP) != null) {
                String token = matcher.group(CARD_PREVIEW_GROUP).trim();
                if (!token.isEmpty() && !cards.containsKey(token)) {
                    cards.put(token, new PreviewReference(PreviewReferenceType.CARD, token, order++));
                }
            } else if (matcher.group(KEYWORD_PREVIEW_GROUP) != null) {
                String token = matcher.group(KEYWORD_PREVIEW_GROUP).trim();
                if (!token.isEmpty() && !keywords.containsKey(token)) {
                    keywords.put(token, new PreviewReference(PreviewReferenceType.KEYWORD, token, order++));
                }
            }
        }

        if (cards.isEmpty() && keywords.isEmpty()) {
            return List.of();
        }

        List<PreviewReference> references = new ArrayList<>(cards.size() + keywords.size());
        references.addAll(cards.values());
        references.addAll(keywords.values());
        return references;
    }

    private static String extractCardDisplayName(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }

        int separatorIndex = token.indexOf('|');
        return separatorIndex >= 0 ? token.substring(0, separatorIndex).trim() : token;
    }
}
