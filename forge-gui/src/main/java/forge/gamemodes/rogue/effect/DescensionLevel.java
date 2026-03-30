package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueConfig;
import forge.gamemodes.rogue.RoguePlanebound;
import forge.gamemodes.rogue.RoguePlaneboundType;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.path.NodePlanebound;
import forge.gamemodes.rogue.path.RoguePathNode;
import forge.util.MyRandom;
import java.util.*;

/**
 * Descension levels for Rogue Commander — applies progressively harder modifiers.
 * Each level implements its own RogueEffect trigger methods.
 *
 * Active effects per descension level (cumulative):
 *   Level 1: Wrathful — some Planebounds gain random advantages (afterPathGeneration)
 *   Level 2: Cursed — some Planebounds gain powerful opponent buffs (afterPathGeneration)
 *   Level 3: Bloodthirsty — creatures deal +1 damage to you (onMatchStart)
 *   Level 4: Elite Paths — 2 random Normal Planes replaced by Elite Planes (afterPathGeneration)
 *   Level 5: Taxing Mana — all spells cost {1} more (onMatchStart)
 */
public enum DescensionLevel implements RogueEffect {

    LEVEL_1(1, "Wrathful",
        "Some Planebounds on the path become Wrathful, gaining random buffs for the match.") {
        @Override
        public void afterPathGeneration(RogueRun run) {
            List<NodePlanebound> planeboundNodes = new ArrayList<>();
            for (RoguePathNode node : run.getPath().getNodes()) {
                if (node instanceof NodePlanebound np) {
                    planeboundNodes.add(np);
                }
            }
            if (planeboundNodes.isEmpty()) return;

            int markerCount = planeboundNodes.size() - 3;
            Random rng = MyRandom.getRandom();
            for (int i = 0; i < markerCount; i++) {
                List<NodePlanebound> eligible = new ArrayList<>();
                for (NodePlanebound np : planeboundNodes) {
                    if (np.getWrathfulCount() < 2) eligible.add(np);
                }
                if (eligible.isEmpty()) break;
                NodePlanebound target = eligible.get(rng.nextInt(eligible.size()));
                target.setWrathfulCount(target.getWrathfulCount() + 1);
            }
        }
    },

    LEVEL_2(2, "Cursed",
        "Some Planebounds on the path become Cursed, gaining powerful buffs for the opponent.") {
        @Override
        public void afterPathGeneration(RogueRun run) {
            List<NodePlanebound> planeboundNodes = new ArrayList<>();
            for (RoguePathNode node : run.getPath().getNodes()) {
                if (node instanceof NodePlanebound np) {
                    planeboundNodes.add(np);
                }
            }
            if (planeboundNodes.isEmpty()) return;

            int markerCount = planeboundNodes.size() / 2;
            Random rng = MyRandom.getRandom();
            for (int i = 0; i < markerCount; i++) {
                List<NodePlanebound> eligible = new ArrayList<>();
                for (NodePlanebound np : planeboundNodes) {
                    if (np.getCursedCount() < 2) eligible.add(np);
                }
                if (eligible.isEmpty()) break;
                NodePlanebound target = eligible.get(rng.nextInt(eligible.size()));
                target.setCursedCount(target.getCursedCount() + 1);
            }
        }
    },

    LEVEL_3(3, "Bloodthirsty",
        "Whenever a creature an opponent controls deals damage to you, it deals 1 additional damage.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Descension - Bloodthirsty", human);
        }
    },

    LEVEL_4(4, "Elite Paths",
        "2 random Normal Planes of the Path are replaced by Elite Planes.") {
        @Override
        public void afterPathGeneration(RogueRun run) {
            List<RoguePathNode> nodes = run.getPath().getNodes();
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

    LEVEL_5(5, "Taxing Mana",
        "Every spell you cast costs {1} more to cast.") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone("Descension - Taxing Mana", human);
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

    @Override
    public String getDisplayName() { return name; }

    @Override
    public String getDescription() { return description; }

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
