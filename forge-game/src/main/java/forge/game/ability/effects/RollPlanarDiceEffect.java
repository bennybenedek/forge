package forge.game.ability.effects;

import forge.game.Game;
import forge.game.PlanarDice;
import forge.game.ability.AbilityUtils;
import forge.game.ability.SpellAbilityEffect;
import forge.game.cost.Cost;
import forge.game.event.GameEventRollDie;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.trigger.Trigger;
import forge.game.trigger.TriggerHandler;
import forge.game.trigger.WrappedAbility;

/**
 * TODO: Write javadoc for this type.
 *
 */
public class RollPlanarDiceEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        final Player activator = sa.getActivatingPlayer();
        final Game game = activator.getGame();

        if (game.getActivePlanes() == null) { // not a planechase game, nothing happens
            return;
        }
        if (sa.hasParam("SpecialAction")) {
            game.getPhaseHandler().incPlanarDiceSpecialActionThisTurn();
        }
        // Play the die roll sound
        game.fireEvent(new GameEventRollDie());
        PlanarDice.roll(activator, null);

        if (sa.hasParam("QueueAdditionalRolls")) {
            queueAdditionalRoll(sa, activator);
        }
    }

    private static void queueAdditionalRoll(final SpellAbility sa, final Player activator) {
        final int additionalRolls = AbilityUtils.calculateAmount(sa.getHostCard(), sa.getParam("QueueAdditionalRolls"), sa);
        if (additionalRolls < 1) {
            return;
        }

        final SpellAbility additionalRoll = sa.copy(sa.getHostCard(), activator, false, true);
        if (additionalRolls > 1) {
            additionalRoll.putParam("QueueAdditionalRolls", String.valueOf(additionalRolls - 1));
        } else {
            additionalRoll.removeParam("QueueAdditionalRolls");
        }
        additionalRoll.setPayCosts(Cost.Zero);
        additionalRoll.setDescription("Roll the planar die.");
        additionalRoll.setStackDescription(additionalRoll.getHostCard() + " - Roll the planar die.");

        final Trigger trigger = TriggerHandler.parseTrigger(
                "Mode$ Immediate | TriggerDescription$ Roll the planar die.",
                additionalRoll.getHostCard(), additionalRoll.isIntrinsic());
        trigger.setOverridingAbility(additionalRoll);

        final WrappedAbility wrapper = new WrappedAbility(trigger, additionalRoll, null);
        wrapper.setActivatingPlayer(activator);
        activator.getGame().getStack().add(wrapper);
    }
}
