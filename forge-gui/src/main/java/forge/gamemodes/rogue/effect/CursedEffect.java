package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum CursedEffect implements PlaneboundEffect {

    BARGAIN("cursed_bargain", "Bargain",
            "All of Planebound's spells cost {1} less to cast.",
        "Cursed - Bargain") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    },
    BLOODTHIRSTY("cursed_bloodthirsty", "Bloodthirsty",
        "Creatures your opponents control get +2/+2 and have haste.",
        "Cursed - Bloodthirsty") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    },
    INSIGHT("cursed_insight", "Insight",
            "Planebound draws 1 additional card at the start of each match.",
        "Cursed - Insight") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            if (opponent == null) return;
            opponent.setStartingHand(opponent.getStartingHand() + 1);
            addEffectCardToCommandZone(human);
            run.consumeEffect(getId());
        }
    },
    RAMP("cursed_ramp", "Ramp",
        "Planebound starts the match with 2 random lands from their deck on the battlefield.",
        null) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            if (opponent == null) return;
            List<PaperCard> lands = new ArrayList<>();
            for (PaperCard c : opponent.getDeck().getMain().toFlatList()) {
                if (c.getRules().getType().isLand()) lands.add(c);
            }
            if (lands.isEmpty()) return;
            Collections.shuffle(lands);
            List<IPaperCard> toMove = new ArrayList<>();
            for (int i = 0; i < Math.min(2, lands.size()); i++) toMove.add(lands.get(i));
            RogueEffect.moveCardsFromDeckToBattlefield(toMove, opponent);
            run.consumeEffect(getId());
        }
    },
    SUMMON("cursed_summon", "Summon",
            "Planebound starts the match with a random creature from their deck on the battlefield.",
            null) {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            if (opponent == null) return;
            List<PaperCard> creatures = new ArrayList<>();
            for (PaperCard c : opponent.getDeck().getMain().toFlatList()) {
                if (c.getRules().getType().isCreature()) creatures.add(c);
            }
            if (creatures.isEmpty()) return;
            Collections.shuffle(creatures);
            List<IPaperCard> toMove = new ArrayList<>();
            toMove.add(creatures.get(0));
            RogueEffect.moveCardsFromDeckToBattlefield(toMove, opponent);
            run.consumeEffect(getId());
        }
    };

    private final String id;
    private final String displayName;
    private final String description;
    private final String effectCardReference;

    CursedEffect(String id, String displayName, String description, String effectCardReference) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.effectCardReference = effectCardReference;
    }

    @Override
    public PlaneboundEffectCategory getCategory() { return PlaneboundEffectCategory.CURSED; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    @Override
    public String getEffectCardReference() { return effectCardReference; }

    public static CursedEffect getRandomExcluding(Set<CursedEffect> exclude) {
        return PlaneboundEffect.getRandomExcluding(values(), exclude);
    }

    public static CursedEffect fromId(String id) {
        for (CursedEffect c : values())
            if (c.id.equals(id)) return c;
        return null;
    }
}
