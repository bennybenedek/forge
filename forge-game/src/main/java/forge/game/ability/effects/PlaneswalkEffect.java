package forge.game.ability.effects;

import forge.game.Game;
import forge.game.GameType;
import forge.game.ability.AbilityKey;
import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardCollectionView;
import forge.game.player.Player;
import forge.game.replacement.ReplacementResult;
import forge.game.replacement.ReplacementType;
import forge.game.spellability.SpellAbility;
import forge.game.trigger.TriggerType;
import forge.util.Localizer;

import java.util.List;
import java.util.Map;


public class PlaneswalkEffect extends SpellAbilityEffect {
    @Override
    public void resolve(SpellAbility sa) {
        Player activator = sa.getActivatingPlayer();
        Game game = activator.getGame();

        if (game.getActivePlanes() == null) { // not a planechase game, nothing happens
            return;
        }

        if (sa.hasParam("Optional") && !activator.getController().confirmAction(sa, null,
                Localizer.getInstance().getMessage("lblWouldYouLikeToPlaneswalk"), null)) {
                    return;
        }

        final Map<AbilityKey, Object> repParams = AbilityKey.mapFromAffected(activator);
        Object cause = sa.hasParam("Cause") ? sa.getParam("Cause") : sa;
        repParams.put(AbilityKey.Cause, cause);
        if (game.getReplacementHandler().run(ReplacementType.Planeswalk, repParams) == ReplacementResult.Replaced) {
            return;
        }

        // In Rogue Commander, trigger planeswalk events but stay on the same plane
        if (game.getRules().getGameType() == GameType.RogueCommander) {
            List<Card> currentPlanes = game.getActivePlanes();
            if (currentPlanes != null && !currentPlanes.isEmpty()) {
                // Trigger "planeswalk away" effects
                final Map<AbilityKey, Object> fromParams = AbilityKey.newMap();
                fromParams.put(AbilityKey.Cards, new CardCollection(currentPlanes));
                game.getTriggerHandler().runTrigger(TriggerType.PlaneswalkedFrom, fromParams, false);

                // Trigger "planeswalk to" effects (same plane, we stay)
                final Map<AbilityKey, Object> toParams = AbilityKey.newMap();
                toParams.put(AbilityKey.Cards, new CardCollection(currentPlanes));
                game.getTriggerHandler().runTrigger(TriggerType.PlaneswalkedTo, toParams, false);
            }
            return;
        }

        if (!sa.hasParam("DontPlaneswalkAway")) {
            for (Player p : game.getPlayers()) {
                p.leaveCurrentPlane();
            }
        }
        if (sa.hasParam("Defined")) {
            CardCollectionView destinations = AbilityUtils.getDefinedCards(sa.getHostCard(), sa.getParam("Defined"), sa);
            activator.planeswalkTo(sa, destinations);
        } else {
            activator.planeswalk(sa);
        }
    }
}
