package forge.gamemodes.rogue;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses and evaluates unlock conditions for Rogue Commander decks.
 *
 * Syntax: Key$ Value | Key$ Value | ...
 *
 * Supported conditions:
 * - Default$ True/False - Always unlocked or always locked
 * - RunsWon$ N - Unlock after winning N total runs
 * - MatchesWon$ N - Unlock after winning N total matches
 * - WinWithCommander$ Name - Unlock after winning a run with specified commander
 * - UseCommander$ Name - Unlock after using specified commander in any run
 * - MaxLife$ N - Unlock after reaching N life in any run
 * - MaxGold$ N - Unlock after earning N gold in any run
 * - CreatureTypes$ N - Unlock after having N different creature types in deck
 */
public class RogueUnlockCondition {

    private final Map<String, String> conditions;
    private final String rawCondition;

    /**
     * Parse an unlock condition string.
     * @param conditionString The condition string (e.g., "Default$ False" or "RunsWon$ 3 | WinWithCommander$ Meria")
     */
    public RogueUnlockCondition(String conditionString) {
        this.rawCondition = conditionString;
        this.conditions = new HashMap<>();

        if (conditionString == null || conditionString.trim().isEmpty()) {
            // No condition means always unlocked
            conditions.put("Default", "True");
            return;
        }

        // Parse pipe-separated conditions
        String[] parts = conditionString.split("\\|");
        for (String part : parts) {
            String trimmed = part.trim();
            int dollarIndex = trimmed.indexOf('$');
            if (dollarIndex > 0) {
                String key = trimmed.substring(0, dollarIndex).trim();
                String value = trimmed.substring(dollarIndex + 1).trim();
                conditions.put(key, value);
            }
        }
    }

    /**
     * Evaluate whether this condition is met based on current meta progress.
     * For conditions with multiple parts, ALL conditions must be met (AND logic).
     * @return true if the commander should be unlocked
     */
    public boolean evaluate() {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();

        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (!evaluateSingleCondition(key, value, progress)) {
                return false; // Any failed condition means locked
            }
        }

        return true; // All conditions passed
    }

    /**
     * Evaluate a single condition key-value pair.
     */
    private boolean evaluateSingleCondition(String key, String value, RogueMetaProgress progress) {
        switch (key) {
            case "Default":
                return Boolean.parseBoolean(value);

            case "RunsStarted":
                return progress.getTotalRunsStarted() >= parseIntSafe(value);

            case "RunsCompleted":
                return progress.getTotalRunsCompleted() >= parseIntSafe(value);

            case "RunsWon":
                return progress.getTotalRunsWon() >= parseIntSafe(value);

            case "MatchesWon":
                return progress.getTotalMatchesWon() >= parseIntSafe(value);

            case "WinWithCommander":
                return progress.hasWonWithCommander(value);

            case "UseCommander":
                return progress.hasUsedCommander(value);

            case "MaxLife":
                return progress.getMaxLifeInRun() >= parseIntSafe(value);

            case "MaxGold":
                return progress.getMaxGoldInRun() >= parseIntSafe(value);

            case "CreatureTypes":
                return progress.getMaxCreatureTypesInDeck() >= parseIntSafe(value);

            default:
                System.err.println("Unknown unlock condition key: " + key);
                return true; // Unknown conditions are ignored (don't block unlock)
        }
    }

    /**
     * Safely parse an integer, returning 0 on failure.
     */
    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number in unlock condition: " + value);
            return Integer.MAX_VALUE; // Impossible to meet on parse error
        }
    }

    /**
     * Get a human-readable description of the unlock condition.
     * @return Description for display in UI
     */
    public String getDescription() {
        if (conditions.containsKey("Default")) {
            return Boolean.parseBoolean(conditions.get("Default"))
                ? "Always available"
                : "Locked";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            if (sb.length() > 0) {
                sb.append(" AND ");
            }
            sb.append(getConditionDescription(entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }

    /**
     * Get description for a single condition.
     */
    private String getConditionDescription(String key, String value) {
        switch (key) {
            case "RunsStarted":
                return "Start " + value + " run(s).";
            case "RunsCompleted":
                return "Complete " + value + " run(s).";
            case "RunsWon":
                return "Win " + value + " run(s).";
            case "MatchesWon":
                return "Win " + value + " matches.";
            case "WinWithCommander":
                return "Win a run with " + value;
            case "UseCommander":
                return "Use " + value + " in a run.";
            case "MaxLife":
                return "Have " + value + "+ life after any Match.";
            case "MaxGold":
                return "Earn " + value + " gold in a run.";
            case "CreatureTypes":
                return "Have " + value + "+ creature types in your deck.";
            default:
                return key + ": " + value;
        }
    }

    /**
     * Check if this is a default unlock (always available or always locked).
     */
    public boolean isDefault() {
        return conditions.size() == 1 && conditions.containsKey("Default");
    }

    /**
     * Check if this is always locked (Default$ False with no other conditions).
     */
    public boolean isAlwaysLocked() {
        return isDefault() && !Boolean.parseBoolean(conditions.get("Default"));
    }

    /**
     * Get the raw condition string.
     */
    public String getRawCondition() {
        return rawCondition;
    }

    @Override
    public String toString() {
        return rawCondition != null ? rawCondition : "Default$ True";
    }
}
