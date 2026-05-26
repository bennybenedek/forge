package forge.screens.home.rogue;

import forge.gamemodes.rogue.PreviewReference;
import forge.gamemodes.rogue.PreviewReferenceType;
import forge.toolbox.FButton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared helper for Rogue buttons.
 */
public final class RogueButtonHelper {

  private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
  private static final String HIGHLIGHT_COLOR = "#E0B437";
  private RogueButtonHelper() {
  }

  public static FButton createChoiceButton(String label, String description) {
    return createChoiceButton(label, description, List.of());
  }

  public static FButton createChoiceButton(String label, String description, List<PreviewReference> references) {
    return new FButton(buildChoiceHtml(label, description, references));
  }

  private static String buildChoiceHtml(String label, String description, List<PreviewReference> references) {
    String escapedLabel = escapeHtml(label);
    if (description == null || description.isEmpty()) {
      return "<html><div style='padding:6px 10px;'><center><font size=4>" + escapedLabel
          + "</font></center></div></html>";
    }
    String highlightedDescription = applyAutomaticCardHighlights(description, references);
    return "<html><div style='padding:6px 10px;'><center><font size=4>" + escapedLabel
        + "</font><br>" + formatDescriptionHtml(highlightedDescription) + "</center></div></html>";
  }

  private static String formatDescriptionHtml(String description) {
    Matcher matcher = HIGHLIGHT_PATTERN.matcher(description);
    StringBuilder sb = new StringBuilder();
    int lastIndex = 0;

    sb.append("<font size=3>");

    while (matcher.find()) {
      appendDescriptionSegment(sb, description.substring(lastIndex, matcher.start()), false);
      appendDescriptionSegment(sb, matcher.group(1), true);
      lastIndex = matcher.end();
    }
    appendDescriptionSegment(sb, description.substring(lastIndex), false);
    sb.append("</font>");
    return sb.toString();
  }

  private static String applyAutomaticCardHighlights(String description, List<PreviewReference> references) {
    if (references == null || references.isEmpty()) {
      return description;
    }

    List<String> cardNames = new ArrayList<>();
    for (PreviewReference reference : references) {
      if (reference.type() != PreviewReferenceType.CARD) {
        continue;
      }
      String cardName = extractCardDisplayName(reference.token());
      if (cardName.isEmpty() || description.contains("**" + cardName + "**")) {
        continue;
      }
      cardNames.add(cardName);
    }

    cardNames.sort(Comparator.comparingInt(String::length).reversed());

    String highlightedDescription = description;
    for (String cardName : cardNames) {
      highlightedDescription = highlightedDescription.replace(cardName, "**" + cardName + "**");
    }
    return highlightedDescription;
  }

  private static void appendDescriptionSegment(StringBuilder sb, String text, boolean highlighted) {
    if (text.isEmpty()) {
      return;
    }
    if (highlighted) {
      sb.append("&#160;<font color='").append(HIGHLIGHT_COLOR).append("'>");
      sb.append(escapeHtml(text));
      sb.append("</font>");
      return;
    }
    sb.append(escapeHtml(text));
  }

  private static String extractCardDisplayName(String token) {
    if (token == null || token.isBlank()) {
      return "";
    }

    int separatorIndex = token.indexOf('|');
    return separatorIndex >= 0 ? token.substring(0, separatorIndex).trim() : token;
  }

  private static String escapeHtml(String text) {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
  }
}
