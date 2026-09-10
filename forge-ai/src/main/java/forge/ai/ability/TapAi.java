package forge.ai.ability;

import forge.ai.*;
import forge.card.ColorSet;
import forge.game.ability.AbilityUtils;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.combat.CombatUtil;
import forge.game.cost.Cost;
import forge.game.cost.CostPart;
import forge.game.cost.CostPayLife;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;
import forge.util.collect.FCollectionView;

import java.util.ArrayList;
import java.util.List;

public class TapAi extends TapAiBase {

    @Override
    protected AiAbilityDecision checkApiLogic(Player ai, SpellAbility sa) {
        final PhaseHandler phase = ai.getGame().getPhaseHandler();
        final Player turn = phase.getPlayerTurn();

        if (turn.isOpponentOf(ai) && phase.getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
            // Tap things down if it's Human's turn
        } else if (turn.equals(ai)) {
            if (isSorcerySpeed(sa, ai) && phase.getPhase().isBefore(PhaseType.COMBAT_BEGIN)) {
                // Cast it if it's a sorcery.
            } else if (phase.getPhase().isBefore(PhaseType.COMBAT_DECLARE_BLOCKERS)) {
                // Aggro Brains are willing to use TapEffects aggressively instead of defensively
                if (!AiProfileUtil.getBoolProperty(ai, AiProps.PLAY_AGGRO)) {
                    return new AiAbilityDecision(0, AiPlayDecision.CantPlayAi);
                }
            } else {
                // Don't tap down after blockers
                return new AiAbilityDecision(0, AiPlayDecision.CantPlayAi);
            }
        } else if (!playReusable(ai, sa)) {
            // Generally don't want to tap things with an Instant during Players turn outside of combat
            return new AiAbilityDecision(0, AiPlayDecision.CantPlayAi);
        }

        final Card source = sa.getHostCard();

        final String aiLogic = sa.getParamOrDefault("AILogic", "");
        if ("GoblinPolkaBand".equals(aiLogic)) {
            return SpecialCardAi.GoblinPolkaBand.consider(ai, sa);
        } else if ("Arena".equals(aiLogic)) {
            return SpecialCardAi.Arena.consider(ai, sa);
        }

        if (sa.usesTargeting()) {
            // X controls the minimum targets
            if ("X".equals(sa.getTargetRestrictions().getMinTargets()) && sa.getSVar("X").equals("Count$xPaid")) {
                ComputerUtilCost.setMaxXValue(sa, ai, sa.isTrigger());
            }

            sa.resetTargets();
            if (tapPrefTargeting(ai, source, sa, false)) {
                return new AiAbilityDecision(100, AiPlayDecision.WillPlay);
            }
            return new AiAbilityDecision(0, AiPlayDecision.TargetingFailed);
        } else {
            CardCollection untap;
            if (sa.hasParam("CardChoices")) {
                untap = CardLists.getValidCards(source.getGame().getCardsIn(ZoneType.Battlefield), sa.getParam("CardChoices"), ai, source, sa);
            } else {
                untap = AbilityUtils.getDefinedCards(source, sa.getParam("Defined"), sa);
            }

            int value = 0;
            for (final Card c : untap) {
                if (c.isUntapped()) {
                    value += ComputerUtilCard.evaluateCreature(c);
                }
            }

            if (value > 0) {
                return new AiAbilityDecision(100, AiPlayDecision.WillPlay);
            }
            return new AiAbilityDecision(0, AiPlayDecision.CantPlayAi);
        }
    }

    @Override
    public boolean willPayUnlessCost(Player payer, SpellAbility sa, Cost cost, boolean alreadyPaid, FCollectionView<Player> payers) {
        // Check for shocklands and similar ETB replacement effects
        if (sa.hasParam("ETB")) {
            final Card source = sa.getHostCard();
            for (final CostPart part : cost.getCostParts()) {
                if (part instanceof CostPayLife) {
                    final CostPayLife lifeCost = (CostPayLife) part;
                    Integer amount = lifeCost.convertAmount();
                    if (payer.getLife() > (amount + 1) && payer.canPayLife(amount, true, sa)) {
                        final int availableMana = ComputerUtilMana.getAvailableManaEstimate(payer);
                        final int sourceMana = source.getMaxManaProduced();
                        final int manaWithSource = availableMana + sourceMana;
                        final ColorSet availableColorsWithoutSource = ColorSet.fromNames(
                                ComputerUtilCost.getAvailableManaColors(payer, List.of()));
                        final ColorSet availableColorsWithSource = ColorSet.fromNames(
                                ComputerUtilCost.getAvailableManaColors(payer, source));
                        final List<SpellAbility> all = ComputerUtilAbility.getSpellAbilities(
                                payer.getCardsIn(ZoneType.Hand, ZoneType.Command), payer);

                        for (final SpellAbility testSa : ComputerUtilAbility.getOriginalAndAltCostAbilities(all, payer)) {
                            final Cost payCosts = testSa.getPayCosts();
                            if (payCosts == null) {
                                continue;
                            }

                            // Pay life only when this land adds the mana needed for a spell the AI wants to play.
                            final int spellManaCost = payCosts.getTotalMana().getCMC();
                            final boolean colorsFitWithoutSource = payCosts.getTotalMana()
                                    .canBePaidWithAvailable(availableColorsWithoutSource.getColor());
                            final boolean colorsFitWithSource = payCosts.getTotalMana()
                                    .canBePaidWithAvailable(availableColorsWithSource.getColor());
                            final boolean payableWithoutSource = availableMana >= spellManaCost && colorsFitWithoutSource;
                            final boolean payableWithSource = manaWithSource >= spellManaCost && colorsFitWithSource;
                            if (payableWithoutSource || !payableWithSource) {
                                continue;
                            }

                            final boolean ignoreMain2Preference = payer.getGame().getPhaseHandler().is(PhaseType.MAIN1, payer)
                                    && testSa.isSpell() && (testSa.getApi() == ApiType.PermanentCreature
                                    || testSa.getApi() == ApiType.PermanentNoncreature);
                            SpellAbility evaluationSa = testSa;
                            if (ignoreMain2Preference) {
                                // An untapped land can also fund a permanent the AI prefers to cast after combat.
                                evaluationSa = testSa.copy(payer);
                                evaluationSa.putParam("AIIgnoreMain2Preference", "True");
                            }
                            final AiPlayDecision playDecision = ((PlayerControllerAi) payer.getController()).getAi().canPlaySa(evaluationSa);
                            final boolean willPlay = playDecision == AiPlayDecision.WillPlay || playDecision == AiPlayDecision.WaitForMain2;
                            if (colorsFitWithSource && willPlay) {
                                return true;
                            }
                        }

                        final boolean fetchedShockland = source.getZone() != null
                                && source.getZone().getZoneType() == ZoneType.Library
                                && payer.getGame().getStack().isResolving()
                                && payer.getGame().getPhaseHandler().is(PhaseType.MAIN1, payer);
                        if (fetchedShockland) {
                            final List<SpellAbility> rawPermanentSpells = new ArrayList<>();
                            for (final Card card : payer.getCardsIn(ZoneType.Hand, ZoneType.Command)) {
                                for (final SpellAbility rawSa : card.getSpells()) {
                                    if (rawSa.getApi() == ApiType.PermanentCreature
                                            || rawSa.getApi() == ApiType.PermanentNoncreature) {
                                        rawPermanentSpells.add(rawSa);
                                    }
                                }
                            }

                            for (final SpellAbility testSa : ComputerUtilAbility.getOriginalAndAltCostAbilities(rawPermanentSpells, payer)) {
                                final Cost payCosts = testSa.getPayCosts();
                                if (payCosts == null || !payCosts.isOnlyManaCost()) {
                                    continue;
                                }
                                final int spellManaCost = payCosts.getTotalMana().getCMC();
                                final boolean colorsFitWithoutSource = payCosts.getTotalMana()
                                        .canBePaidWithAvailable(availableColorsWithoutSource.getColor());
                                final boolean colorsFitWithSource = payCosts.getTotalMana()
                                        .canBePaidWithAvailable(availableColorsWithSource.getColor());
                                final boolean payableWithoutSource = availableMana >= spellManaCost && colorsFitWithoutSource;
                                final boolean payableWithSource = manaWithSource >= spellManaCost && colorsFitWithSource;
                                if (payableWithoutSource || !payableWithSource) {
                                    continue;
                                }

                                if (colorsFitWithSource) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            }
        } else if (sa.hasParam("UnlessSwitched")) {
            // effect is each opponent may sacrifice to tap creature
            Card source = sa.getHostCard();
            if (alreadyPaid) {
                return false;
            }
            // if it can't attack the payer, do nothing?
            // TODO check if it can attack team mates?
            if (!CombatUtil.canAttack(source, payer)) {
                return false;
            }

            // predict combat damage
            int dmg = ComputerUtilCombat.damageIfUnblocked(source, payer, null, false);
            if (payer.getLife() < dmg * 1.5) {
                return true;
            }
        }
        return super.willPayUnlessCost(payer, sa, cost, alreadyPaid, payers);
    }
}
