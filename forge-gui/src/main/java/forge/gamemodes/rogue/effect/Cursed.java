package forge.gamemodes.rogue.effect;

import forge.game.player.RegisteredPlayer;
import forge.gamemodes.rogue.RogueRun;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum Cursed implements PlaneboundBoon {

    SPELL_DISCOUNT("cursed_spell_discount", "Bargain",
            "All of Planebound's spells cost {1} less to cast.",
            "Cursed - Bargain") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    },
    SCRY("cursed_scry", "Foresight",
            "At the beginning of each of Planebound's upkeeps, they scry 2.",
            "Cursed - Foresight") {
        @Override
        public void onMatchStart(RegisteredPlayer human, RegisteredPlayer opponent, RogueRun run) {
            RogueEffect.addCardToCommandZone(cardName, human);
            run.consumeEffect(getId());
        }
    },
    FREE_CREATURE("cursed_free_creature", "Summon",
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
    },
    FREE_LANDS("cursed_free_lands", "Fortify",
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
    };

    private final String id;
    private final String displayName;
    private final String description;
    protected final String cardName;

    Cursed(String id, String displayName, String description, String cardName) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cardName = cardName;
    }

    @Override
    public BoonCategory getCategory() { return BoonCategory.CURSED; }

    @Override
    public String getId() { return id; }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getRawDescription() { return description; }

    public static Cursed getRandomExcluding(Set<Cursed> exclude) {
        return PlaneboundBoon.getRandomExcluding(values(), exclude);
    }

    public static Cursed fromId(String id) {
        for (Cursed c : values())
            if (c.id.equals(id)) return c;
        return null;
    }
}
