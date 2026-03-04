package forge.gamemodes.rogue;

import forge.game.player.RegisteredPlayer;
import forge.util.MyRandom;
import java.util.*;

/**
 * Descension levels for Rogue Commander — applies progressively harder modifiers.
 * Each level implements its own RogueEffect trigger methods.
 *
 * Active effects per descension level (cumulative):
 *   Level 1: 2 random Normal Planes replaced by Elite Planes (afterPathGeneration)
 *   Level 2: Bloodthirst — creatures deal +1 damage to you (onMatchStart)
 *   Level 3: Taxing Mana — all spells cost {1} more (onMatchStart)
 */
public enum DescensionLevel implements RogueEffect {

    LEVEL_1(1, "Bloodthirsty",
        "Whenever a creature an opponent controls deals damage to you, it deals 1 additional damage.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run, RogueMetaProgress progress) {
            RogueEffect.addCustomCardToCommandZone("Descension - Bloodthirst", human);
        }
    },

    LEVEL_2(2, "Elite Paths",
        "2 random Normal Planes of the Path are replaced by Elite Planes.") {
        @Override
        public void afterPathGeneration(List<RoguePathNode> nodes, int descensionLevel) {
            Set<String> usedNames = new HashSet<>();
            for (RoguePathNode node : nodes) {
                if (node instanceof NodePlanebound np && np.getPlaneboundType() == RoguePlaneboundType.ELITE) {
                    usedNames.add(np.getRoguePlanebound().planeboundName());
                }
            }
            List<RoguePlanebound> unusedElites = new ArrayList<>();
            for (RoguePlanebound pb : RogueConfig.loadPlanebounds()) {
                if (pb.type() == RoguePlaneboundType.ELITE && !usedNames.contains(pb.planeboundName())) {
                    unusedElites.add(pb);
                }
            }
            if (unusedElites.size() < 2) return;
            Collections.shuffle(unusedElites, MyRandom.getRandom());

            List<Integer> normalIndices = new ArrayList<>();
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) instanceof NodePlanebound np && np.getPlaneboundType() == RoguePlaneboundType.NORMAL) {
                    normalIndices.add(i);
                }
            }
            if (normalIndices.size() < 2) return;
            Collections.shuffle(normalIndices, MyRandom.getRandom());

            for (int i = 0; i < 2; i++) {
                int idx = normalIndices.get(i);
                NodePlanebound orig = (NodePlanebound) nodes.get(idx);
                NodePlanebound replacement = new NodePlanebound(unusedElites.get(i));
                replacement.setRowIndex(orig.getRowIndex());
                replacement.setColumnIndex(orig.getColumnIndex());
                nodes.set(idx, replacement);
            }
        }
    },

    LEVEL_3(3, "Taxing Mana",
        "Every spell you cast costs {1} more to cast.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RogueRun run, RogueMetaProgress progress) {
            RogueEffect.addCustomCardToCommandZone("Descension - Taxing Mana", human);
        }
    };

    public final int level;
    public final String name;
    public final String description;

    DescensionLevel(int level, String name, String description) {
        this.level = level;
        this.name = name;
        this.description = description;
    }

    public static DescensionLevel forLevel(int level) {
        for (DescensionLevel dl : values()) {
            if (dl.level == level) return dl;
        }
        return null;
    }

    public static int getMaxLevel() {
        return values().length;
    }
}
