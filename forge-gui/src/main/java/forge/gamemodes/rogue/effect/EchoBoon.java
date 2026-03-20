package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.AetherUpgrade;
import forge.gamemodes.rogue.RogueRun;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enum defining all available Boons (permanent upgrades) in Rogue Commander mode.
 * Each constant implements its own RogueRunEffect trigger methods.
 */
public enum EchoBoon implements RogueEffect {

    // Base boons (requiredUpgradeLevel=0) — always visible

    VITAL_INFUSION("vital_infusion", "Vital Infusion",
        "Begin each Run with additional Max Life.",
        new int[]{3, 6, 9, 12},     // Echo costs per rank (rank 1-4)
        new int[]{3, 6, 9, 12},     // Effect values: +3/+6/+9/+12 life
        3, 0) {
        @Override
        public void onRunStart(RogueRun run) {
            int bonus = getEffectValueAtRank(run.getRunBoonRank(getId()));
            if (bonus > 0) run.setStartingLife(run.getStartingLife() + bonus);
        }
    },

    AETHER_MARKET("aether_market", "Aether Market",
        "Gain additional starting Gold at the beginning of each Run.",
        new int[]{3, 6, 9, 12},    // Echo costs per rank
        new int[]{3, 6, 9, 12},    // Effect values: +3/+6/+9/+12 gold
        3, 0) {
        @Override
        public void onRunStart(RogueRun run) {
            int bonus = getEffectValueAtRank(run.getRunBoonRank(getId()));
            if (bonus > 0) run.setCurrentGold(run.getCurrentGold() + bonus);
        }
    },

    LINGERING_AURA("lingering_aura", "Lingering Aura",
        "Heal Life after each Plane match victory.",
        new int[]{2, 4, 8, 16},     // Echo costs per rank
        new int[]{2, 4, 6, 8},      // Effect values: heal 2/4/6/8
        3, 0) {
        @Override
        public void onMatchWin(RogueRun run) {
            if (run.getCurrentLife() >= run.getStartingLife()) return;
            int heal = getEffectValueAtRank(run.getRunBoonRank(getId()));
            if (heal > 0) run.healLife(heal);
        }
    },

    SPECTRAL_RECALIBRATION("spectral_recalibration", "Spectral Recalibration",
        "Gain rerolls per Card Reward or Bazaar selection.",
        new int[]{6, 12, 18},      // Echo costs (rank 1-3)
        new int[]{1, 2, 3},        // Effect values: 1/2/3 rerolls per selection
        2, 0) {
        @Override
        public void onCardSelection(CardSelectionContext ctx, RogueRun run) {
            ctx.rerolls += getEffectValueAtRank(run.getRunBoonRank(getId()));
        }
    },

    MYTHIC_COLLECTOR("mythic_collector", "Mythic Collector",
        "More cards from Card Rewards and Bazaar will be mythic rarity.",
        new int[]{3, 6, 9, 12},    // Echo costs per rank
        new int[]{1, 2, 3, 4},     // Effect values: +1/+2/+3/+4 extra mythics
        3, 0) {
        @Override
        public void onCardSelection(CardSelectionContext ctx, RogueRun run) {
            ctx.extraMythics += getEffectValueAtRank(run.getRunBoonRank(getId()));
        }
    },

    LAST_SPARK("last_spark", "Last Spark",
        "Survive defeat and revive with 5 life.",
        new int[]{10, 20},         // Echo costs (rank 1-2)
        new int[]{1, 2},           // Effect values: 1/2 revive charges
        1, 0) {
        @Override
        public EffectType getEffectType() { return EffectType.CONSUME; }

        @Override
        public int getChargesForRank(int rank) { return getEffectValueAtRank(rank); }

        @Override
        public void onDefeat(DefeatContext ctx, RogueRun run) {
            ctx.revived = true;
            ctx.reviveLife = 5;
            run.consumeEffect(getId());
        }
    },

    //  Aether Upgrade 1

    FORESIGHT("foresight", "Foresight",
        "Start each match with 1 additional opening hand card.",
        new int[]{8, 12},          // Echo costs (rank 1-2)
        new int[]{1, 2},           // Effect values: +1/+2 cards
        1, 1) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            int extra = getEffectValueAtRank(run.getRunBoonRank(getId()));
            if (extra > 0) human.setStartingHand(human.getStartingHand() + extra);
        }
    },

    EXPANDED_MIND("expanded_mind", "Expanded Mind",
        "Keep additional cards from Card Rewards.",
        new int[]{8, 12},       // Echo costs (rank 1-2)
        new int[]{1, 2},        // Effect values: +1/+2 extra picks
        1, 1) {
        @Override
        public void onCardReward(CardRewardContext ctx, RogueRun run) {
            ctx.maxPicks += getEffectValueAtRank(run.getRunBoonRank(getId()));
        }
    },

    SPARK_KINDLE("spark_kindle", "Spark Kindle",
        "Begin each match with basic lands from your deck already on the battlefield.",
        new int[]{5, 10, 20},      // Echo costs (rank 1-3)
        new int[]{1, 2, 3},        // Effect values: 1/2/3 tapped lands
        2, 1) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            int count = getEffectValueAtRank(run.getRunBoonRank(getId()));
            if (count <= 0) return;
            List<PaperCard> basicLands = new ArrayList<>();
            for (PaperCard c : human.getDeck().getMain().toFlatList()) {
                if (c.getRules().getType().isBasicLand()) basicLands.add(c);
            }
            if (basicLands.isEmpty()) return;
            Collections.shuffle(basicLands);
            List<IPaperCard> toMove = new ArrayList<>();
            for (int i = 0; i < Math.min(count, basicLands.size()); i++) toMove.add(basicLands.get(i));
            RogueEffect.moveCardsFromDeckToBattlefield(toMove, human);
        }
    },

    FRACTURED_BINDING("fractured_binding", "Fractured Binding",
        "Reduce the Mana Cost for casting your Commander.",
        new int[]{4, 8, 12, 16},   // Echo costs (rank 1-4)
        new int[]{1, 2, 3, 4},     // Effect values: {1}/{2}/{3}/{4} less
        3, 1) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            int reduction = getEffectValueAtRank(run.getRunBoonRank(getId()));
            if (reduction > 0) RogueEffect.addCustomCardToCommandZone("Echo - Fractured Binding " + reduction, human);
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final int[] echoCosts;
    private final int[] effectValues;
    private final int maxRank;
    private final int requiredUpgradeLevel; // 0 = always accessible; 1 = requires Aether Upgrade 1

    EchoBoon(String id, String displayName, String description,
             int[] echoCosts, int[] effectValues, int maxRank, int requiredUpgradeLevel) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.echoCosts = echoCosts;
        this.effectValues = effectValues;
        this.maxRank = maxRank;
        this.requiredUpgradeLevel = requiredUpgradeLevel;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public int getMaxRank() {
        return maxRank;
    }

    /**
     * Whether this boon is accessible given the current Aether Upgrade level.
     */
    public boolean isAccessibleAt(int upgradeLevel) {
        return requiredUpgradeLevel <= upgradeLevel;
    }

    /**
     * Get the effective maximum rank, including any bonus from Aether Upgrades.
     * Aether Upgrade 3 adds +1 max rank to all boons.
     */
    public int getEffectiveMaxRank(int upgradeLevel) {
        int bonus = 0;
        for (int l = 1; l <= upgradeLevel; l++) {
            AetherUpgrade u = AetherUpgrade.forLevel(l);
            if (u != null) bonus += u.extraBoonRanks;
        }
        return maxRank + bonus;
    }

    /**
     * Get the echo cost to upgrade from (rank-1) to (rank).
     * Bounds use echoCosts.length to allow the extra rank from Aether Upgrade 3.
     * @param rank The target rank (1-indexed)
     * @return The cost in echoes, or 0 if invalid rank
     */
    public int getEchoCostForRank(int rank) {
        if (rank < 1 || rank > echoCosts.length) {
            return 0;
        }
        return echoCosts[rank - 1];
    }

    /**
     * Get the effect value at a specific rank.
     * Bounds use effectValues.length to allow the extra rank from Aether Upgrade 3.
     * @param rank The current rank (1-indexed)
     * @return The effect magnitude, or 0 if not unlocked
     */
    public int getEffectValueAtRank(int rank) {
        if (rank < 1 || rank > effectValues.length) {
            return 0;
        }
        return effectValues[rank - 1];
    }

    /**
     * Get the description showing all rank values with the current rank highlighted.
     * Uses HTML formatting. Shows values up to the effective max rank for the given upgrade level.
     * @param currentRank The current rank (0 = not unlocked)
     * @param upgradeLevel The current Aether Upgrade level
     * @return HTML-formatted description string
     */
    public String getDescriptionWithAllRanks(int currentRank, int upgradeLevel) {
        String allValues = buildAllValuesString(currentRank, upgradeLevel);

      return switch (this) {
        case VITAL_INFUSION -> "<html>Begin each Run with +" + allValues + " Max Life.</html>";
        case AETHER_MARKET -> "<html>Gain +" + allValues + " starting Gold.</html>";
        case LINGERING_AURA -> "<html>Heal " + allValues + " Life after each match victory.</html>";
        case FORESIGHT ->
            "<html>Start each match with +" + allValues + " opening hand card.</html>";
        case MYTHIC_COLLECTOR ->
            "<html>+" + allValues + " more mythic cards in Rewards and Bazaar.</html>";
        case LAST_SPARK ->
            "<html>Survive defeat and revive with 5 life, " + allValues + " time(s).</html>";
        case EXPANDED_MIND -> "<html>Keep +" + allValues + " extra cards from Card Rewards.</html>";
        case SPARK_KINDLE ->
            "<html>Begin each match with " + allValues + " basic land(s) on battlefield.</html>";
        case FRACTURED_BINDING ->
            "<html>Your Commander costs " + allValues + " less to cast.</html>";
        case SPECTRAL_RECALIBRATION ->
            "<html>Gain " + allValues + " reroll(s) per Card Reward or Bazaar selection.</html>";
        default -> "<html>" + description + "</html>";
      };
    }

    /**
     * Build the all-values string showing values up to effectiveMaxRank(upgradeLevel).
     * The current rank value is highlighted with bold+underline.
     */
    private String buildAllValuesString(int currentRank, int upgradeLevel) {
        int effectiveMax = getEffectiveMaxRank(upgradeLevel);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < effectValues.length && (i + 1) <= effectiveMax; i++) {
            if (i > 0) {
                sb.append(" / ");
            }
            int rankForThisValue = i + 1;
            if (rankForThisValue == currentRank) {
                sb.append("<b><u>").append(effectValues[i]).append("</u></b>");
            } else {
                sb.append(effectValues[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Find a BoonType by its ID.
     */
    public static EchoBoon fromId(String id) {
        for (EchoBoon type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
