package forge.ai.ability;


import forge.ai.*;
import forge.card.MagicColor;
import forge.game.ability.AbilityUtils;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.combat.CombatUtil;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.util.MyRandom;
import forge.util.TextUtil;

import java.util.List;

public class RollPlanarDiceAi extends SpellAbilityAi {
    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#canPlayAI(forge.game.player.Player, java.util.Map, forge.card.spellability.SpellAbility)
     */
    @Override
    protected AiAbilityDecision canPlay(Player ai, SpellAbility sa) {
        if (ai.getGame().getActivePlanes() == null) {
            return new AiAbilityDecision(0, AiPlayDecision.CantPlayAi);
        }
        
        for (Card c : ai.getGame().getActivePlanes()) {
            if (willRollOnPlane(ai, sa, c)) {
                return new AiAbilityDecision(100, AiPlayDecision.WillPlay);
            }
        }
        return new AiAbilityDecision(0, AiPlayDecision.CantPlayAi);
    }

    private boolean willRollOnPlane(Player ai, SpellAbility sa, Card plane) {
        boolean decideToRoll = false;
        boolean rollInMain1 = false;
        String modeName = "never";
        int maxActivations = AiProfileUtil.getIntProperty(ai, AiProps.DEFAULT_MAX_PLANAR_DIE_ROLLS_PER_TURN);
        int chance = AiProfileUtil.getIntProperty(ai, AiProps.DEFAULT_PLANAR_DIE_ROLL_CHANCE);
        int hesitationChance = AiProfileUtil.getIntProperty(ai, AiProps.PLANAR_DIE_ROLL_HESITATION_CHANCE);
        int minTurnToRoll = AiProfileUtil.getIntProperty(ai, AiProps.DEFAULT_MIN_TURN_TO_ROLL_PLANAR_DIE);
        
        if (plane.hasSVar("AIRollPlanarDieParams")) {
            String[] params = plane.getSVar("AIRollPlanarDieParams").trim().split("\\|");
            for (String param : params) {
                String[] paramData = param.toLowerCase().split("\\$");
                String paramName = paramData[0].trim();
                String paramValue = paramData[1].trim();

                switch (paramName) {
                    case "mode":
                        modeName = paramValue;
                        break;
                    case "chance":
                        chance = Integer.parseInt(paramValue);
                        break;
                    case "minturn":
                        minTurnToRoll = Integer.parseInt(paramValue);
                        break;
                    case "maxrollsperturn":
                        maxActivations = Integer.parseInt(paramValue);
                        break;
                    case "rollinmain1":
                        if (paramValue.equals("true")) {
                            rollInMain1 = true;
                        }
                        break;
                    case "lowpriority":
                        // this is handled in AiController.saComparator at the moment
                        break;
                    case "cardsinhandle": // num of cards in hand less than or equal to N
                        if (ai.getCardsIn(ZoneType.Hand).size() > Integer.parseInt(paramValue)) {
                            return false;
                        }
                        break;
                    case "cardsinhandge": // num of cards in hand greater than or equal to N
                        if (ai.getCardsIn(ZoneType.Hand).size() < Integer.parseInt(paramValue)) {
                            return false;
                        }
                        break;
                    case "cardsingraveyardle":
                        if (ai.getCardsIn(ZoneType.Graveyard).size() > Integer.parseInt(paramValue)) {
                            return false;
                        }
                        break;
                    case "hasvalidcardinzone":
                        String[] zoneAndValidity = param.substring(param.indexOf('$') + 1).trim().split(":", 2);
                        ZoneType zone = ZoneType.smartValueOf(zoneAndValidity[0].trim());
                        if ((zone != ZoneType.Battlefield && zone != ZoneType.Graveyard)
                                || CardLists.getValidCards(ai.getGame().getCardsIn(zone), zoneAndValidity[1].trim(), ai, plane, sa).isEmpty()) {
                            return false;
                        }
                        break;
                    case "hasavailableinstantsorcery":
                        if (paramValue.equals("true") && ComputerUtilAbility.getSpellAbilities(
                                ComputerUtilAbility.getAvailableCards(ai.getGame(), ai), ai).stream().noneMatch(ability ->
                                ability.isSpell() && (ability.getCardState().getType().isInstant()
                                || ability.getCardState().getType().isSorcery())
                                && !ComputerUtilCard.isCardRemAIDeck(ability.getHostCard())
                                && ability.canCastTiming(ai) && ComputerUtilAbility.isFullyTargetable(ability))) {
                            return false;
                        }
                        break;
                    case "hasattackablecreature":
                        if (paramValue.equals("true") && !CombatUtil.canAttack(ai)) {
                            return false;
                        }
                        break;
                    case "devotionexceedsrollcost":
                        if (paramValue.equals("true")) {
                            String color = ComputerUtilCard.getMostProminentColor(
                                    ai.getCardsIn(ZoneType.Battlefield), MagicColor.Constant.ONLY_COLORS);
                            int devotion = AbilityUtils.calculateAmount(plane, "Count$Devotion." + color, sa);
                            int rollCost = ComputerUtilMana.calculateManaCost(sa.getPayCosts(), sa, ai, true, 0, false)
                                    .getConvertedManaCost();
                            if (devotion <= rollCost) {
                                return false;
                            }
                        }
                        break;
                    case "stopifunlimitedhandsize":
                        if (ai.isUnlimitedHandSize()) {
                            return false;
                        }
                        break;
                    default:
                        System.out.println(TextUtil.concatNoSpace("Unexpected AI hint parameter in card ", plane.getName(), " in RollPlanarDiceAi: ", paramName, "."));
                        break;
                }
            }
            
            switch (modeName) {
                case "always":
                    decideToRoll = true;
                    break;
                case "random":
                    if (MyRandom.getRandom().nextInt(100) < chance) {
                        decideToRoll = true;
                    }
                    break;
                case "never":
                    return false;
                default:
                    return false;
            }

            if (ai.getGame().getPhaseHandler().getTurn() < minTurnToRoll) {
                decideToRoll = false;
            } else if (!rollInMain1 && ai.getGame().getPhaseHandler().getPhase().isBefore(PhaseType.MAIN2)) {
                decideToRoll = false;
            }

            if (decideToRoll && ai.getGame().getPhaseHandler().getPlanarDiceSpecialActionThisTurn() >= maxActivations) {
                boolean extraRollWindow = sa.hasParam("SpecialAction")
                        && (ai.getGame().getPhaseHandler().is(PhaseType.MAIN2, ai)
                        || (rollInMain1 && ai.getGame().getPhaseHandler().is(PhaseType.MAIN1, ai)));
                if (!extraRollWindow || !canSpendSurplusMana(ai, sa)) {
                    decideToRoll = false;
                }
            }
        
            // check if the AI hesitates
            if (MyRandom.getRandom().nextInt(100) < hesitationChance) {
                decideToRoll = false; // hesitate
            }
        }

        return decideToRoll;
    }

    private boolean canSpendSurplusMana(Player ai, SpellAbility roll) {
        final int rollMana = ComputerUtilMana.calculateManaCost(roll.getPayCosts(), roll, ai, true, 0, false)
                .getConvertedManaCost();
        final boolean main1 = ai.getGame().getPhaseHandler().is(PhaseType.MAIN1, ai);
        final AiController aic = ((PlayerControllerAi) ai.getController()).getAi();
        CardCollection cards = ComputerUtilAbility.getAvailableCards(ai.getGame(), ai);
        cards = ComputerUtilCard.dedupeCards(cards);
        final List<SpellAbility> possible = ComputerUtilAbility.getSpellAbilities(cards, ai);
        // Current-play filtering can omit future-phase abilities and counters with no target yet.
        for (Card card : cards) {
            if (card.getController() != ai) {
                continue;
            }
            for (SpellAbility ability : card.getSpellAbilities()) {
                if ((ability.getApi() == ApiType.Counter || ability.getRestrictions().isOpponentTurn()
                        || ability.getRestrictions().getPhases().contains(PhaseType.MAIN2))
                        && !possible.contains(ability)) {
                    possible.add(ability);
                }
            }
        }
        final List<SpellAbility> abilities = ComputerUtilAbility.getOriginalAndAltCostAbilities(possible, ai);

        for (SpellAbility candidate : abilities) {
            if (candidate.getApi() == ApiType.RollPlanarDice || candidate.isManaAbility()
                    || (!candidate.isSpell() && !candidate.isActivatedAbility())
                    || candidate.getPayCosts() == null || !candidate.getPayCosts().hasManaCost()
                    || ComputerUtilCard.isCardRemAIDeck(candidate.getHostCard())) {
                continue;
            }
            if (ComputerUtilMana.calculateManaCost(candidate.getPayCosts(), candidate, ai, true, 0, false)
                    .getConvertedManaCost() == 0 || !ComputerUtilCost.canPayCost(candidate, ai, false)) {
                continue;
            }

            boolean main2Only = candidate.getRestrictions().getPhases().contains(PhaseType.MAIN2)
                    && !candidate.getRestrictions().getPhases().contains(PhaseType.MAIN1);
            if (!SpellAbilityAi.isSorcerySpeed(candidate, ai) && !main2Only) {
                if (!candidate.getRestrictions().isPlayerTurn() && candidate.canCastTiming(ai)
                        && (candidate.getApi() == ApiType.Counter || candidate.getRestrictions().isOpponentTurn()
                        || (candidate.canPlay() && ComputerUtilAbility.isFullyTargetable(candidate)))
                        && !ComputerUtilMana.canPayManaCost(candidate, ai, rollMana, false)) {
                    return false;
                }
                continue;
            }
            if (!main1 || candidate.getRestrictions().isOpponentTurn()
                    || (!candidate.getRestrictions().getPhases().isEmpty()
                    && !candidate.getRestrictions().getPhases().contains(PhaseType.MAIN2))) {
                continue;
            }

            if (!main2Only) {
                SpellAbility preview = candidate.copy(ai);
                if (candidate.isSpell() && candidate.getHostCard().isPermanent()) {
                    preview.putParam("AIIgnoreMain2Preference", "True");
                }
                AiPlayDecision decision = aic.canPlaySa(preview);
                if (decision != AiPlayDecision.WillPlay && decision != AiPlayDecision.WaitForMain2
                        && decision != AiPlayDecision.MissingPhaseRestrictions) {
                    continue;
                }
            }

            // Paying for the future play plus this roll preserves colored mana needs.
            if ((rollMana > 0 && candidate.getPayCosts().getTotalMana().countX() > 0)
                    || !ComputerUtilMana.canPayManaCost(candidate, ai, rollMana, false)) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellAiLogic#chkAIDrawback(java.util.Map, forge.card.spellability.SpellAbility, forge.game.player.Player)
     */
    @Override
    public AiAbilityDecision chkDrawback(Player aiPlayer, SpellAbility sa) {
        // for potential implementation of drawback checks?
        return canPlay(aiPlayer, sa);
    }

}
