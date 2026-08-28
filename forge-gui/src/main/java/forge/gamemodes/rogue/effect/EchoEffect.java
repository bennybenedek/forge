package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.AetherUpgrade;
import forge.gamemodes.rogue.RogueRun;
import forge.item.PaperCard;

/**
 * Enum defining all available Echo effects (=Boons) in Rogue Commander mode.
 * Each constant implements its own RogueRunEffect trigger methods.
 */
public enum EchoEffect implements RogueEffect {

    // Base boons (requiredUpgradeLevel=0) — always visible

    VITAL_INFUSION("vital_infusion", "Vital Infusion",
        "Begin each Run with +%s Max Life.",
        new EffectRankContext(
            new int[]{3, 6, 9, 12}, // Echo costs per rank (rank 1-4)
            new int[]{3, 6, 9, 12}, // Effect values: +3/+6/+9/+12 life
            3, 0),
        EffectType.PERMANENT, null) {
        @Override
        public void onRunStart(RogueRun run) {
            int bonus = getEffectValueAtRank(run.getRunEffectRank(getId()));
            if (bonus > 0) run.addMaxLife(bonus);
        }
    },

    AETHER_MARKET("aether_market", "Aether Market",
        "Gain +%s starting Gold.",
        new EffectRankContext(
            new int[]{3, 6, 9, 12}, // Echo costs per rank
            new int[]{3, 6, 9, 12}, // Effect values: +3/+6/+9/+12 gold
            3, 0),
        EffectType.PERMANENT, null) {
        @Override
        public void onRunStart(RogueRun run) {
            int bonus = getEffectValueAtRank(run.getRunEffectRank(getId()));
            if (bonus > 0) run.addGold(bonus);
        }
    },

    LINGERING_AURA("lingering_aura", "Lingering Aura",
        "Gain %s life after each match victory.",
        new EffectRankContext(
            new int[]{2, 4, 8, 16}, // Echo costs per rank
            new int[]{2, 4, 6, 8},  // Effect values: gain 2/4/6/8 life
            3, 0),
        EffectType.PERMANENT, "Echo Boon - Lingering Aura") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            int rank = run.getRunEffectRank(getId());
            int heal = getEffectValueAtRank(rank);
            if (heal > 0) {
                RogueEffect.addCardToCommandZone(getEffectCardReferenceForRank(rank), human);
            }
        }

        @Override
        public void onMatchWin(RogueRun run) {
            if (run.getCurrentLife() >= run.getMaxLife()) return;
            int heal = getEffectValueAtRank(run.getRunEffectRank(getId()));
            if (heal > 0) run.gainLifeUpToMax(heal);
        }
    },

    SPECTRAL_BARGAIN("spectral_bargain", "Spectral Bargain",
        "Gain %s free reroll(s) in Card Rewards and Bazaar.",
        new EffectRankContext(
            new int[]{2, 4, 8, 12}, // Echo costs (rank 1-3)
            new int[]{1, 2, 3, 4},  // Effect values: 1/2/3 free rerolls
            3, 0),
        EffectType.PERMANENT, null) {
        @Override
        public void onCardSelection(CardSelectionContext ctx, RogueRun run) {
            ctx.freeRerolls += getEffectValueAtRank(run.getRunEffectRank(getId()));
        }
    },

    MYTHIC_COLLECTOR("mythic_collector", "Mythic Collector",
        "See +%s more mythic cards in Rewards and Bazaar.",
        new EffectRankContext(
            new int[]{3, 6, 9, 12}, // Echo costs per rank
            new int[]{1, 2, 3, 4},  // Effect values: +1/+2/+3/+4 extra mythics
            3, 0),
        EffectType.PERMANENT, null) {
        @Override
        public void onCardSelection(CardSelectionContext ctx, RogueRun run) {
            ctx.extraMythics += getEffectValueAtRank(run.getRunEffectRank(getId()));
        }
    },

    LAST_SPARK("last_spark", "Last Spark",
        "Survive defeat and revive with 5 life, %s time(s).",
        new EffectRankContext(
            new int[]{10, 20}, // Echo costs (rank 1-2)
            new int[]{1, 2},   // Effect values: 1/2 revive charges
            1, 0),
        EffectType.CONSUME, "Echo Boon - Last Spark") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            int rank = run.getRunEffectRank(getId());
            int charges = getEffectValueAtRank(rank);
            if (charges > 0) {
                RogueEffect.addCardToCommandZone(getEffectCardReferenceForRank(rank), human);
            }
        }

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

    EXPANDED_MIND("expanded_mind", "Expanded Mind",
        "Keep +%s extra cards from Card Rewards.",
        new EffectRankContext(
            new int[]{8, 12}, // Echo costs (rank 1-2)
            new int[]{1, 2},  // Effect values: +1/+2 extra picks
            1, 1),
        EffectType.PERMANENT, null) {
        @Override
        public void onCardReward(CardRewardContext ctx, RogueRun run) {
            ctx.maxPicks += getEffectValueAtRank(run.getRunEffectRank(getId()));
        }
    },

    BRACKET_BREAKER("bracket_breaker", "Bracket Breaker",
        "Replace %s non-mythic card(s) of Card Rewards with cards from the Gamechanger list.",
        new EffectRankContext(
            new int[]{8, 12, 16}, // Echo costs (rank 1-3)
            new int[]{1, 2, 3},   // Effect values: replace 1/2/3 non-mythics
            2, 1),
        EffectType.PERMANENT, null) {
        @Override
        public void onCardReward(CardRewardContext ctx, RogueRun run) {
            int count = getEffectValueAtRank(run.getRunEffectRank(getId()));
            if (count <= 0) {
                return;
            }

            ctx.nonMythicCardReplacementCount += count;
            ctx.nonMythicCardReplacementCandidates.addAll(run.getGamechangerCardsForActiveCommander());
        }
    },

    FORTITUDE("fortitude", "Fortitude",
        "Fellows and Items you control have hexproof and indestructible.",
        new EffectRankContext(
            new int[]{10}, // Echo cost
            new int[]{1},  // Non-ranked boon
            1, 1),
        EffectType.PERMANENT, "Echo Boon - Fortitude") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            if (run.getRunEffectRank(getId()) > 0) {
                RogueEffect.addCardToCommandZone(getEffectCardReference(), human);
            }
        }

        @Override
        public int getEffectiveMaxRank(int upgradeLevel) {
            return 1;
        }
    },

    FARSIGHT("farsight", "Farsight",
        "Reveal %s more Planebound row(s) on the map.",
        new EffectRankContext(
            new int[]{6, 10, 14}, // Echo costs (rank 1-3)
            new int[]{1, 2, 3},   // Effect values: reveal 1/2/3 more Planebound rows
            2, 1),
        EffectType.PERMANENT, null) {
        @Override
        public void onPathUpdate(PathUpdateContext ctx, RogueRun run) {
            ctx.additionalVisiblePlaneboundRows += getEffectValueAtRank(
                run.getRunEffectRank(getId()));
        }
    },

    FATEBENDING("fatebending", "Fatebending",
        "Reroll NPC Boons, Chest Loot and Events up to %s time(s) during the Run.",
        new EffectRankContext(
            new int[]{6, 10, 14, 18}, // Echo costs (rank 1-4)
            new int[]{1, 2, 3, 4},    // Effect values: 1/2/3/4 reroll charges
            3, 1),
        EffectType.CONSUME, null) {
        @Override
        public int getChargesForRank(int rank) {
            return getEffectValueAtRank(rank);
        }

        @Override
        public void onBeforeNpcBoons(ChoiceRerollContext ctx, RogueRun run) {
            ctx.remainingRerolls = run.getRunEffectCharges(getId());
        }

        @Override
        public void onBeforeChestLoot(ChoiceRerollContext ctx, RogueRun run) {
            ctx.remainingRerolls = run.getRunEffectCharges(getId());
        }

        @Override
        public void onBeforeEvent(ChoiceRerollContext ctx, RogueRun run) {
            ctx.remainingRerolls = run.getRunEffectCharges(getId());
        }

        @Override
        public void onChoiceReroll(ChoiceRerollContext ctx, RogueRun run) {
            run.consumeEffect(getId());
        }
    },

    PLANEBENDING("planebending", "Planebending",
        "Reroll Planebound Nodes of the next row on the map up to %s time(s) during the Run.",
        new EffectRankContext(
            new int[]{6, 10, 14, 18}, // Echo costs (rank 1-4)
            new int[]{1, 2, 3, 4},    // Effect values: 1/2/3/4 reroll charges
            3, 1),
        EffectType.CONSUME, null) {
        @Override
        public int getChargesForRank(int rank) {
            return getEffectValueAtRank(rank);
        }

        @Override
        public void onPathUpdate(PathUpdateContext ctx, RogueRun run) {
            ctx.remainingPlaneboundRerolls = run.getRunEffectCharges(getId());
        }

        @Override
        public void onPathNodeReroll(PathUpdateContext ctx, RogueRun run) {
            run.consumeEffect(getId());
        }
    };

//    OPENING_VISION("foresight", "Opening Vision",
//        "Start each match with +%s opening hand card.",
//        new EffectRankContext(
//        new int[]{8, 12}, // Echo costs (rank 1-2)
//        new int[]{1, 2},  // Effect values: +1/+2 cards
//        1, 1),
//    EffectType.PERMANENT, "Echo Boon - Opening Vision") {
//        @Override
//        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
//            int rank = run.getRunEffectRank(getId());
//            int extra = getEffectValueAtRank(rank);
//            if (extra > 0) {
//                human.setStartingHand(human.getStartingHand() + extra);
//                RogueEffect.addCardToCommandZone(getEffectCardReferenceForRank(rank), human);
//            }
//        }
//    },

//    SPARK_KINDLE("spark_kindle", "Spark Kindle",
//        "Begin each match with %s basic land(s) on battlefield.",
//        new EffectRankContext(
//            new int[]{5, 10, 20}, // Echo costs (rank 1-3)
//            new int[]{1, 2, 3},   // Effect values: 1/2/3 tapped lands
//            2, 1),
//        EffectType.PERMANENT, "Echo Boon - Spark Kindle") {
//        @Override
//        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
//            int rank = run.getRunEffectRank(getId());
//            int count = getEffectValueAtRank(rank);
//            if (count <= 0) return;
//            RogueEffect.moveCardsFromDeckToBattlefield(c -> c.getRules().getType().isBasicLand(),
//                count, human);
//            RogueEffect.addCardToCommandZone(getEffectCardReferenceForRank(rank), human);
//        }
//    },

//    FRACTURED_BINDING("fractured_binding", "Fractured Binding",
//        "Your Commander costs %s less to cast.",
//        new EffectRankContext(
//            new int[]{4, 8, 12, 16}, // Echo costs (rank 1-4)
//            new int[]{1, 2, 3, 4},   // Effect values: {1}/{2}/{3}/{4} less
//            3, 1),
//        EffectType.PERMANENT, "Echo Boon - Fractured Binding") {
//        @Override
//        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
//            int rank = run.getRunEffectRank(getId());
//            int reduction = getEffectValueAtRank(rank);
//            if (reduction > 0) {
//                RogueEffect.addCardToCommandZone(getEffectCardReferenceForRank(rank), human);
//            }
//        }
//    };

    private final String id;
    private final String displayName;
    private final String description;
    private final EffectRankContext rankContext;
    private final EffectType effectType;
    private final String effectCardReference;
    EchoEffect(String id, String displayName, String description, EffectRankContext rankContext,
             EffectType effectType, String effectCardReference) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rankContext = rankContext;
        this.effectType = effectType;
        this.effectCardReference = effectCardReference;
    }

    @Override
    public EffectType getEffectType() {
        return effectType;
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
    public String getRawDescription() { return description; }

    public String getDescriptionForRank(int rank) {
        int value = getEffectValueAtRank(rank);
        return description.formatted(value > 0 ? Integer.toString(value) : "?");
    }

    @Override
    public String getActiveDescription(RogueRun run) {
        return getDescriptionForRank(run.getRunEffectRank(getId()));
    }

    @Override
    public String getEffectCardReference() { return effectCardReference; }

    @Override
    public PaperCard getEffectCard() {
        return null;
    }

    public int getMaxRank() {
        return rankContext.maxRank();
    }

    /**
     * Whether this boon is accessible given the current Aether Upgrade level.
     */
    public boolean isAccessibleAt(int upgradeLevel) {
        return rankContext.requiredUpgradeLevel() <= upgradeLevel;
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
        return rankContext.maxRank() + bonus;
    }

    /**
     * Get the echo cost to upgrade from (rank-1) to (rank).
     * Bounds use echoCosts.length to allow the extra rank from Aether Upgrade 3.
     * @param rank The target rank (1-indexed)
     * @return The cost in echoes, or 0 if invalid rank
     */
    public int getEchoCostForRank(int rank) {
        if (rank < 1 || rank > rankContext.echoCosts().length) {
            return 0;
        }
        return rankContext.echoCosts()[rank - 1];
    }

    /**
     * Get the effect value at a specific rank.
     * Bounds use effectValues.length to allow the extra rank from Aether Upgrade 3.
     * @param rank The current rank (1-indexed)
     * @return The effect magnitude, or 0 if not unlocked
     */
    public int getEffectValueAtRank(int rank) {
        if (rank < 1 || rank > rankContext.effectValues().length) {
            return 0;
        }
        return rankContext.effectValues()[rank - 1];
    }

    /**
     * Get the description showing all rank values with the current rank highlighted.
     * Uses HTML formatting. Shows values up to the effective max rank for the given upgrade level.
     * @param currentRank The current rank (0 = not unlocked)
     * @param upgradeLevel The current Aether Upgrade level
     * @return HTML-formatted description string
     */
    @Override
    public String getDescriptionWithAllRanks(int currentRank, int upgradeLevel) {
        String allValues = buildAllValuesString(currentRank, upgradeLevel);
        return "<html>" + description.formatted(allValues) + "</html>";
    }

    /**
     * Build the all-values string showing values up to effectiveMaxRank(upgradeLevel).
     * The current rank value is highlighted with bold+underline.
     */
    private String buildAllValuesString(int currentRank, int upgradeLevel) {
        int effectiveMax = getEffectiveMaxRank(upgradeLevel);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rankContext.effectValues().length && (i + 1) <= effectiveMax; i++) {
            if (i > 0) {
                sb.append(" / ");
            }
            int rankForThisValue = i + 1;
            if (rankForThisValue == currentRank) {
                sb.append("<b><u>").append(rankContext.effectValues()[i]).append("</u></b>");
            } else {
                sb.append(rankContext.effectValues()[i]);
            }
        }
        return sb.toString();
    }

    private record EffectRankContext(int[] echoCosts, int[] effectValues, int maxRank,
                                     int requiredUpgradeLevel) { }

    /**
     * Find a BoonType by its ID.
     */
    public static EchoEffect fromId(String id) {
        for (EchoEffect type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
