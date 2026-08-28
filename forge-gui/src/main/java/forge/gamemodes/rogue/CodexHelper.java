package forge.gamemodes.rogue;

import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gamemodes.rogue.effect.BazaarItem;
import forge.gamemodes.rogue.effect.ChestEffect;
import forge.gamemodes.rogue.effect.EventEffect;
import forge.gamemodes.rogue.effect.NPCEffect;
import forge.gamemodes.rogue.effect.RogueEffect;
import forge.gamemodes.rogue.effect.WoundEffect;
import forge.item.PaperCard;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class CodexHelper {
    public enum TraitCategory {
        NPC,
        CHEST,
        EVENT,
        WOUND
    }

    private static final ZoneType[] PLANEBOUND_PUBLIC_ZONES = {
        ZoneType.Battlefield,
        ZoneType.Graveyard,
        ZoneType.Exile,
        ZoneType.Command,
        ZoneType.Stack
    };

    private static final Map<String, Set<String>> COMMANDER_REWARD_NAMES = new HashMap<>();

    private CodexHelper() {
    }

    public static void recordCardRewardOptions(RogueRun run, Collection<PaperCard> cards) {
        recordCommanderRewardCards(run, cards, false);
    }

    public static void recordAcquiredCards(RogueRun run, Collection<PaperCard> cards) {
        recordCommanderRewardCards(run, cards, true);
    }

    public static void recordBazaarInventory(RogueRun run, Collection<BazaarItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (BazaarItem item : items) {
            if (item == null) {
                continue;
            }
            recordCommanderRewardCard(run, item.card(), false);
            recordTraitChoice(item.traitEffect());
        }
    }

    public static void recordBazaarPurchases(RogueRun run, Collection<BazaarItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (BazaarItem item : items) {
            if (item == null) {
                continue;
            }
            recordCommanderRewardCard(run, item.card(), true);
            recordTraitAcquired(item.traitEffect());
        }
    }

    public static void recordTraitChoices(Collection<? extends RogueEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        for (RogueEffect effect : effects) {
            recordTraitChoice(effect);
        }
    }

    public static void recordTraitChoice(RogueEffect effect) {
        if (getTraitCategory(effect) == null) {
            return;
        }
        RogueMetaProgress.getInstance().markTraitSeen(effect);
    }

    public static void recordTraitAcquired(RogueEffect effect) {
        if (getTraitCategory(effect) == null) {
            return;
        }
        RogueMetaProgress.getInstance().markTraitAcquired(effect);
    }

    public static void recordTraitsAcquired(Collection<? extends RogueEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        for (RogueEffect effect : effects) {
            recordTraitAcquired(effect);
        }
    }

    public static void recordPlaneboundEncounter(RoguePlanebound planebound) {
        RogueMetaProgress.getInstance().markPlaneboundEncountered(planebound);
    }

    public static void recordPlaneboundCommanderCards(RoguePlanebound planebound, Collection<PaperCard> cards) {
        if (planebound == null || cards == null || cards.isEmpty()) {
            return;
        }

        for (PaperCard card : cards) {
            if (card != null) {
                RogueMetaProgress.getInstance().markPlaneboundCardSeen(planebound, card);
            }
        }
    }

    public static void recordPlaneboundPublicCards(RoguePlanebound planebound, Player player) {
        if (planebound == null || player == null) {
            return;
        }

        for (ZoneType zone : PLANEBOUND_PUBLIC_ZONES) {
            for (Card card : player.getCardsIn(zone)) {
                recordPlaneboundCard(planebound, card);
            }
        }
    }

    public static TraitCategory getTraitCategory(RogueEffect effect) {
        if (effect == null || effect.getId() == null || effect.getId().isBlank()) {
            return null;
        }
        if (effect instanceof NPCEffect) {
            return TraitCategory.NPC;
        }
        if (effect instanceof WoundEffect) {
            return TraitCategory.WOUND;
        }
        String effectCardReference = effect.getEffectCardReference();
        if (effectCardReference == null || effectCardReference.isBlank()) {
            return null;
        }
        if (effect instanceof ChestEffect) {
            return TraitCategory.CHEST;
        }
        if (effect instanceof EventEffect) {
            return TraitCategory.EVENT;
        }
        return null;
    }

    private static void recordCommanderRewardCards(RogueRun run, Collection<PaperCard> cards, boolean acquired) {
        if (cards == null || cards.isEmpty()) {
            return;
        }

        for (PaperCard card : cards) {
            recordCommanderRewardCard(run, card, acquired);
        }
    }

    private static void recordCommanderRewardCard(RogueRun run, PaperCard card, boolean acquired) {
        if (run == null || card == null || run.getSelectedRogueDeck() == null) {
            return;
        }

        String commanderName = run.getSelectedRogueDeck().getCommanderCardName();
        if (commanderName == null || !getCommanderRewardNames(commanderName).contains(normalize(card))) {
            return;
        }

        RogueMetaProgress progress = RogueMetaProgress.getInstance();
        if (acquired) {
            progress.markCommanderRewardCardAcquired(commanderName, card);
        } else {
            progress.markCommanderRewardCardSeen(commanderName, card);
        }
    }

    private static void recordPlaneboundCard(RoguePlanebound planebound, Card card) {
        if (card == null) {
            return;
        }

        if (card.getPaperCard() instanceof PaperCard paperCard) {
            RogueMetaProgress.getInstance().markPlaneboundCardSeen(planebound, paperCard);
        }
    }

    private static Set<String> getCommanderRewardNames(String commanderName) {
        return COMMANDER_REWARD_NAMES.computeIfAbsent(commanderName, key -> {
            Set<String> names = new HashSet<>();
            RogueConfig.loadRogueDecks().stream()
                .filter(deck -> key.equals(deck.getCommanderCardName()))
                .findFirst()
                .ifPresent(deck -> {
                    for (PaperCard card : deck.getRewardPoolCards()) {
                        String normalized = normalize(card);
                        if (normalized != null) {
                            names.add(normalized);
                        }
                    }
                });
            return names;
        });
    }

    private static String normalize(PaperCard card) {
        return card == null || card.getRules() == null ? null : card.getRules().getNormalizedName();
    }
}
