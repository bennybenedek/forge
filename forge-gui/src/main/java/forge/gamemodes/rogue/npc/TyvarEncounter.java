package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.SanctumContext;
import java.util.List;

/**
 * Tyvar Kell - Commander Trainer NPC.
 * Progresses through Sanctum encounters before offering boons on future runs.
 */
public enum TyvarEncounter implements NPCEncounter {

    /** Hidden buildup phase: each entered Sanctum advances Tyvar's reveal progress. */
    BEFORE_REVEAL(0) {
        @Override
        public void onBeforeSanctum(SanctumContext ctx, RogueRun run) {
            incrementNpcLevel();
            if (RogueMetaProgress.getInstance().getNPCLevel(getNpc().id) < 2) {
                return;
            }
            ctx.preSanctumDialogs.add(buildContext(
                "Stranger", STRANGER_AVATAR_INDEX,
                List.of(
                    "Near the Sanctum's cold stone floor, a man in ripped clothes drags himself forward, " +
                        "leaving a thin trail of blood behind him.",
                    "His face is pale, his breathing ragged, " +
                        "and one trembling hand reaches toward you. \"Help me... please.\""
                ),
                List.of()
            ));
            addHelpStrangerChoice(ctx);
        }
    },

    /** Tyvar has been found and waits for help at a Sanctum. */
    WAITING_FOR_HELP(2) {
        @Override
        public void onBeforeSanctum(SanctumContext ctx, RogueRun run) {
            addHelpStrangerChoice(ctx);
        }

        @Override
        public NPCContext onSanctumChoice(SanctumContext.SanctumChoice choice, RogueRun run) {
            if (!HELP_STRANGER_CHOICE_ID.equals(choice.id())) {
                return null;
            }
            incrementNpcLevel();
            return buildContext(
                List.of(
                    "The stranger steadies himself, pain still written across every movement, but pride returning to his eyes. " +
                        "\"I am Tyvar Kell. You did not owe me aid, but you gave it anyway. I won't forget that.\"",
                    "He grips your forearm with surprising strength. \"Let me repay you properly. From now on, " +
                        "when your journey begins, I will help train your Commander for the battles ahead.\""
                ),
                List.of()
            );
        }
    },

    /** After being helped, Tyvar offers Commander training boons at the start of runs. */
    OFFERING_BOONS(3) {
        @Override
        public NPCContext onRunStart(RogueRun run) {
            return buildOfferingBoonsContext(run);
        }
    };

    private static final String HELP_STRANGER_CHOICE_ID = "tyvar_help_stranger";
    private static final int STRANGER_AVATAR_INDEX = 125;

    private final int requiredLevel;

    TyvarEncounter(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public NPC getNpc() { return NPC.TYVAR; }

    @Override
    public int getRequiredLevel() { return requiredLevel; }

    private static void addHelpStrangerChoice(SanctumContext ctx) {
        ctx.extraChoices.add(new SanctumContext.SanctumChoice(
            HELP_STRANGER_CHOICE_ID, "Help Stranger", "???"));
    }

    @Override
    public List<String> getOfferingBoonMonologues() {
        return List.of(
            "Tyvar Kell steps from the shadows, his elven features lit by the glow of his Planeswalker spark. " +
                "\"I've watched you fight, and I see potential. Let me train your Commander.\"",
            "Tyvar cracks his knuckles and grins. \"A commander should lead from the front. Pick a lesson, " +
                "and I'll make sure yours hits harder than before.\"",
            "\"Raw strength is useful,\" Tyvar says, pacing beside your camp, \"but knowing where to spend it wins wars. " +
                "Choose your training.\""
        );
    }
}
