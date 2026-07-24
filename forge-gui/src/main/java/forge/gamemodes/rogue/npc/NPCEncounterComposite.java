package forge.gamemodes.rogue.npc;

import forge.gamemodes.rogue.RogueEvent;
import forge.gamemodes.rogue.RogueMetaProgress;
import forge.gamemodes.rogue.RogueRun;
import forge.gamemodes.rogue.effect.BazaarContext;
import forge.gamemodes.rogue.effect.EventEffect;
import forge.gamemodes.rogue.effect.SanctumContext;
import forge.util.MyRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dispatcher that resolves active NPC encounters based on progression level.
 * For each NPC, finds the highest-level encounter the player qualifies for.
 */
public enum NPCEncounterComposite {

    INSTANCE;

    /** All registered NPC encounter constants. Add new NPCs here. */
    private static final NPCEncounter[] ALL_ENCOUNTERS = concat(
        TyvarEncounter.values(),
        GontiEncounter.values(),
        NarsetEncounter.values(),
        HenzieEncounter.values()
    );

    private static NPCEncounter[] concat(NPCEncounter[]... arrays) {
        int total = 0;
        for (NPCEncounter[] a : arrays) total += a.length;
        NPCEncounter[] result = new NPCEncounter[total];
        int i = 0;
        for (NPCEncounter[] a : arrays)
            for (NPCEncounter e : a) result[i++] = e;
        return result;
    }

    /**
     * For each NPC, finds the encounter whose requiredLevel is the highest
     * the player's NPC progression level still qualifies for.
     * Returns at most one encounter per NPC.
     */
    public List<NPCEncounter> getEncountersForCurrentLevel(RogueMetaProgress progress) {
        Map<NPC, NPCEncounter> encounterPerNpc = new HashMap<>();
        for (NPCEncounter npcEncounter : ALL_ENCOUNTERS) {
            NPC npc = npcEncounter.getNpc();
            int playerNpcLevel = progress.getNPCLevel(npc.id);
            if (playerNpcLevel < npcEncounter.getRequiredLevel()) continue;
            NPCEncounter current = encounterPerNpc.get(npc);
            if (current == null || npcEncounter.getRequiredLevel() > current.getRequiredLevel()) {
                encounterPerNpc.put(npc, npcEncounter);
            }
        }
        return new ArrayList<>(encounterPerNpc.values());
    }

    public List<NPCContext> onAfterMatch(RogueRun run) {
        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        List<NPCContext> results = new ArrayList<>();
        for (NPCEncounter enc : getEncountersForCurrentLevel(progress)) {
            NPCContext ctx = enc.onAfterMatch(run);
            if (ctx != null) results.add(ctx);
        }
        return results;
    }

    public List<NPCContext> onRunStart(RogueMetaProgress progress, RogueRun run) {
        List<NPCEncounter> encounters = getEncountersForCurrentLevel(progress);
        Collections.shuffle(encounters, MyRandom.getRandom());
        for (NPCEncounter enc : encounters) {
            NPCContext ctx = enc.onRunStart(run);
            if (ctx != null) {
                return Collections.singletonList(ctx);
            }
        }
        return List.of();
    }

    public void onBeforeSanctum(SanctumContext ctx, RogueRun run, RogueMetaProgress progress) {
        for (NPCEncounter enc : getEncountersForCurrentLevel(progress)) {
            enc.onBeforeSanctum(ctx, run);
        }
    }

    public List<NPCContext> onSanctumChoice(SanctumContext.SanctumChoice choice, RogueRun run,
                                            RogueMetaProgress progress) {
        List<NPCContext> results = new ArrayList<>();
        for (NPCEncounter enc : getEncountersForCurrentLevel(progress)) {
            NPCContext npcCtx = enc.onSanctumChoice(choice, run);
            if (npcCtx != null) results.add(npcCtx);
        }
        return results;
    }

    public List<NPCContext> onAfterEventChoice(RogueEvent event, RogueEvent.EventChoice choice,
                                               EventEffect effect, RogueRun run, RogueMetaProgress progress) {
        List<NPCContext> results = new ArrayList<>();
        for (NPCEncounter enc : getEncountersForCurrentLevel(progress)) {
            NPCContext npcCtx = enc.onAfterEventChoice(event, choice, effect, run);
            if (npcCtx != null) results.add(npcCtx);
        }
        return results;
    }

    public void onBeforeBazaar(BazaarContext ctx, RogueMetaProgress progress) {
        for (NPCEncounter enc : getEncountersForCurrentLevel(progress)) {
            enc.onBeforeBazaar(ctx);
        }
    }

    public List<NPCContext> onAfterBazaarPurchase(BazaarContext ctx, RogueMetaProgress progress) {
        List<NPCContext> results = new ArrayList<>();
        for (NPCEncounter enc : getEncountersForCurrentLevel(progress)) {
            NPCContext npcCtx = enc.onAfterBazaarPurchase(ctx);
            if (npcCtx != null) results.add(npcCtx);
        }
        return results;
    }
}
