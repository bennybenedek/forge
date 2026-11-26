package forge.ai.ability;

import forge.ai.*;
import forge.ai.simulation.ActivationGuard;
import forge.ai.simulation.RampPlan;
import forge.card.MagicColor;
import forge.card.mana.ManaAtom;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostShard;
import forge.game.ability.AbilityUtils;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.card.CardUtil;
import forge.game.combat.CombatUtil;
import forge.game.cost.Cost;
import forge.game.cost.CostPart;
import forge.game.cost.CostPayLife;
import forge.game.cost.CostPartMana;
import forge.game.cost.CostSacrifice;
import forge.game.cost.CostTap;
import forge.game.mana.Mana;
import forge.game.mana.ManaCostBeingPaid;
import forge.game.phase.PhaseHandler;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.AbilityManaPart;
import forge.game.zone.ZoneType;
import forge.util.collect.FCollectionView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class TapAi extends TapAiBase {

    @Override
    protected AiAbilityDecision checkApiLogic(Player ai, SpellAbility sa) {
        if (ManaDenial.isLandTapper(sa)) {
            // Rishadan Port, Rishadan Dockhand: which land and when is ManaDenial's call; the generic logic below
            // only looks at creatures and permanents with tap abilities, so it never taps a land
            final Card land = ManaDenial.choose(ai, sa);
            if (land == null) {
                return new AiAbilityDecision(0, AiPlayDecision.CantPlayAi);
            }
            sa.resetTargets();
            sa.getTargets().add(land);
            return new AiAbilityDecision(100, AiPlayDecision.WillPlay);
        }

        final PhaseHandler phase = ai.getGame().getPhaseHandler();

        final AiAbilityDecision cantrip = cantripAtTheirEnd(ai, sa, phase);
        if (cantrip != null) {
            return cantrip;
        }

        if (phase.getPlayerTurn().isOpponentOf(ai) && phase.getPhase().isBefore(PhaseType.COMBAT_DECLARE_ATTACKERS)) {
            // Tap things down if it's Human's turn
        } else if (phase.isPlayerTurn(ai)) {
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

    /**
     * A tap with a card drawn (Ice of Fire // Ice) is a cantrip that costs two mana: at the end of the opponent's turn,
     * with the mana otherwise unused, it is cast on their best untapped land (or other permanent): they untap it at once,
     * but the card is drawn, which is the point. Not at sorcery speed; the rest of this class judges the other times.
     */
    private static AiAbilityDecision cantripAtTheirEnd(final Player ai, final SpellAbility sa, final PhaseHandler phase) {
        if (!sa.isSpell() || sa.getSubAbility() == null || sa.getSubAbility().getApi() != forge.game.ability.ApiType.Draw
                || !sa.usesTargeting() || isSorcerySpeed(sa, ai)
                || !phase.is(PhaseType.END_OF_TURN) || !phase.getPlayerTurn().isOpponentOf(ai) || !ai.canDraw()) {
            return null;
        }
        Card best = null;
        for (final Card c : ai.getOpponents().getCardsIn(ZoneType.Battlefield)) {
            if (c.isUntapped() && sa.canTarget(c) && (best == null || c.isLand() && !best.isLand()
                    || c.isLand() == best.isLand() && ComputerUtilCard.evaluatePermanentList(new CardCollection(c)) > ComputerUtilCard.evaluatePermanentList(new CardCollection(best)))) {
                best = c;
            }
        }
        if (best == null) {
            return null;
        }
        sa.resetTargets();
        sa.getTargets().add(best);
        return new AiAbilityDecision(100, AiPlayDecision.WillPlay);
    }

    private record ManaUnit(byte colors, Card source, AbilityManaPart restrictions) {
        private Mana asMana(final byte color, final Player payer) {
            return new Mana(color, source, restrictions, payer);
        }
    }

    private record ManaOption(SpellAbility ability, List<ManaUnit> output, ManaCost activation, int life) { }

    private record ManaEstimate(List<ManaUnit> pool, List<List<ManaOption>> sources, int life) { }

    private record ShockSpell(SpellAbility ability, ManaCost mana, boolean approved) { }

    private static boolean shocklandEnablesPlay(final Player payer, final Card land, final int amount, final int reserve) {
        final Map<Card, List<ShockSpell>> spells = new LinkedHashMap<>();
        final List<SpellAbility> all = ComputerUtilAbility.getSpellAbilities(
                payer.getCardsIn(ZoneType.Hand, ZoneType.Command), payer);
        final boolean fetched = land.isInZone(ZoneType.Library) && payer.getGame().getStack().isResolving()
                && payer.getGame().getPhaseHandler().is(PhaseType.MAIN1, payer);
        if (fetched) {
            for (final Card card : payer.getCardsIn(ZoneType.Hand, ZoneType.Command)) {
                for (final SpellAbility spell : card.getSpells()) {
                    if (!all.contains(spell) && (spell.getApi() == ApiType.PermanentCreature
                            || spell.getApi() == ApiType.PermanentNoncreature)) {
                        all.add(spell);
                    }
                }
            }
        }
        for (final SpellAbility original : ComputerUtilAbility.getOriginalAndAltCostAbilities(all, payer)) {
            final SpellAbility spell = original.copy(payer);
            if (spell.getPayCosts() == null || !spell.isSpell()) {
                continue;
            }
            final ManaCost mana = ComputerUtilMana.calculateManaCost(spell.getPayCosts(), spell, payer, true, 0, false).toManaCost();
            if (mana.getCMC() == 1 && payer.getLife() < 14) {
                continue;
            }
            final boolean permanent = spell.getApi() == ApiType.PermanentCreature || spell.getApi() == ApiType.PermanentNoncreature;
            if (permanent && payer.getGame().getPhaseHandler().is(PhaseType.MAIN1, payer)) {
                spell.putParam("AIIgnoreMain2Preference", "True");
            }
            final AiPlayDecision decision = ((PlayerControllerAi) payer.getController()).getAi().canPlaySa(spell);
            final boolean approved = decision == AiPlayDecision.WillPlay || decision == AiPlayDecision.WaitForMain2
                    || fetched && permanent && spell.getPayCosts().isOnlyManaCost();
            // The combined line can justify a mana-token producer whose primary effect the heuristic would hold.
            boolean manaProducer = RampPlan.tokenMana(spell.getHostCard()) > 0 && spell.canPlay();
            for (SpellAbility part = spell; part != null; part = part.getSubAbility()) {
                manaProducer &= !part.usesTargeting();
            }
            if (!approved && !manaProducer) {
                continue;
            }
            spells.computeIfAbsent(spell.getHostCard(), c -> new ArrayList<>()).add(new ShockSpell(spell, mana, approved));
        }
        final ManaEstimate without = estimateMana(payer, land, false, payer.getLife() - reserve);
        final ManaEstimate with = estimateMana(payer, land, true, payer.getLife() - reserve - amount);
        // Preserve the existing single-play policy even when another, equally expensive play is already possible.
        for (final List<ShockSpell> modes : spells.values()) {
            for (final ShockSpell spell : modes) {
                if (spell.approved && payEstimate(payer, spell.ability, new ManaCostBeingPaid(spell.mana), with, m -> true, new HashMap<>())
                        && !payEstimate(payer, spell.ability, new ManaCostBeingPaid(spell.mana), without, m -> true, new HashMap<>())) {
                    return true;
                }
            }
        }
        final List<List<ShockSpell>> candidates = new ArrayList<>(spells.values());
        if (candidates.size() > 10) {
            candidates.subList(10, candidates.size()).clear();
        }
        return spendable(payer, candidates, with, 0, new HashMap<>())
                > spendable(payer, candidates, without, 0, new HashMap<>());
    }

    private static ManaEstimate estimateMana(final Player payer, final Card land, final boolean includeLand, final int life) {
        final List<ManaUnit> pool = new ArrayList<>();
        for (final Mana mana : payer.getManaPool()) {
            pool.add(new ManaUnit(mana.getColor(), mana.getSourceCard(), mana.getManaAbility()));
        }
        final List<List<ManaOption>> sources = new ArrayList<>();
        final CardCollection cards = new CardCollection(ComputerUtilMana.getAvailableManaSources(payer, true));
        cards.remove(land);
        if (includeLand) {
            cards.add(land);
        }
        for (final Card card : cards) {
            final List<ManaOption> options = new ArrayList<>();
            for (final SpellAbility original : ComputerUtilMana.getAIPlayableMana(card)) {
                final SpellAbility ability = original.copy(payer);
                if (card != land && !ability.canPlay()) {
                    continue;
                }
                final Cost costs = ability.getPayCosts();
                if (costs == null || costs.getCostParts().stream().anyMatch(c -> !(c instanceof CostTap)
                        && !(c instanceof CostPartMana) && !(c instanceof CostPayLife)
                        && !(c instanceof CostSacrifice && c.payCostFromSource()))) {
                    continue;
                }
                int lifeCost = 0;
                for (final CostPart c : costs.getCostParts()) {
                    if (c instanceof CostPayLife) {
                        lifeCost += AbilityUtils.calculateAmount(card, c.getAmount(), ability);
                    }
                }
                if (lifeCost > life || !payer.canPayLife(lifeCost, false, ability)) {
                    continue;
                }
                final ManaCost activation = costs.getTotalMana();
                final Set<String> producible = CardUtil.canProduce(6, ability, new HashSet<>());
                for (final byte color : ManaAtom.MANATYPES) {
                    if (!producible.contains(MagicColor.toLongString(color))) {
                        continue;
                    }
                    for (SpellAbility part = ability; part != null; part = part.getSubAbility()) {
                        final AbilityManaPart mp = part.getManaPart();
                        if (mp != null && (mp.isAnyMana() || part.getApi() == ApiType.ManaReflected || mp.choosesColorOnResolution(part))) {
                            mp.setExpressChoice(MagicColor.toShortString(color));
                        }
                    }
                    final String produced = ComputerUtilMana.predictMana(ability, payer, ManaCostShard.valueOf(color));
                    final List<ManaUnit> output = new ArrayList<>();
                    AbilityManaPart restrictions = null;
                    for (SpellAbility part = ability; part != null; part = part.getSubAbility()) {
                        if (part.getManaPart() != null) {
                            restrictions = part.getManaPart();
                            break;
                        }
                    }
                    if (produced.startsWith("Combo ")) {
                        byte colors = 0;
                        for (final String type : produced.substring(6).split(" ")) {
                            colors |= ManaAtom.fromName(type);
                        }
                        final int count = AbilityUtils.calculateAmount(card, ability.getParamOrDefault("Amount", "1"), ability);
                        for (int i = 0; i < count; i++) {
                            output.add(new ManaUnit(colors, card, restrictions));
                        }
                    } else {
                        for (final String type : produced.split(" ")) {
                            final byte colors = ManaAtom.fromName(type);
                            if (colors != 0) {
                                output.add(new ManaUnit(colors, card, restrictions));
                            } else if (type.matches("[0-9]+")) {
                                for (int i = 0; i < Integer.parseInt(type); i++) {
                                    output.add(new ManaUnit((byte) ManaAtom.COLORLESS, card, restrictions));
                                }
                            }
                        }
                    }
                    final ManaOption option = new ManaOption(ability, output, activation, lifeCost);
                    if (!output.isEmpty() && options.stream().noneMatch(o -> o.output.equals(output)
                            && o.activation.equals(activation) && o.life == option.life)) {
                        options.add(option);
                    }
                }
            }
            // Ordinary duals/any-color rocks have one unit, not one unit per possible color.
            if (!options.isEmpty() && options.stream().allMatch(o -> o.activation.isZero() && o.life == 0
                    && o.output.size() == 1 && o.output.get(0).restrictions.getManaRestrictions().isEmpty()
                    && o.output.get(0).restrictions.getExtraManaRestriction().isEmpty())) {
                byte colors = 0;
                for (final ManaOption option : options) {
                    colors |= option.output.get(0).colors;
                }
                pool.add(new ManaUnit(colors, card, null));
            } else if (options.size() == 1 && options.get(0).activation.isZero() && options.get(0).life == 0) {
                pool.addAll(options.get(0).output);
            } else if (!options.isEmpty()) {
                sources.add(options);
            }
        }
        return new ManaEstimate(pool, sources, life);
    }

    /** Pay on immutable estimates; filter costs are paid before their output exists. */
    private static boolean payEstimate(final Player payer, final SpellAbility spell, final ManaCostBeingPaid cost,
            final ManaEstimate mana, final Predicate<ManaEstimate> paid, final Map<ManaCost, Set<ManaEstimate>> visited) {
        if (cost.isPaid()) {
            return paid.test(mana);
        }
        if (!visited.computeIfAbsent(cost.toManaCost(), c -> new HashSet<>()).add(mana)) {
            return false;
        }
        final List<Byte> possible = new ArrayList<>();
        for (final ManaUnit unit : mana.pool) {
            possible.add(unit.colors);
        }
        for (final List<ManaOption> options : mana.sources) {
            byte colors = 0;
            int count = 0;
            for (final ManaOption option : options) {
                count = Math.max(count, option.output.size());
                for (final ManaUnit unit : option.output) {
                    colors |= unit.colors;
                }
            }
            for (int i = 0; i < count; i++) {
                possible.add(colors);
            }
        }
        if (!ManaSourceAllocation.canPayColoredPips(cost.toManaCost(), possible)) {
            return false;
        }
        final Set<String> tried = new HashSet<>();
        for (int i = 0; i < mana.pool.size(); i++) {
            final ManaUnit unit = mana.pool.get(i);
            final String key = unit.colors + ":" + unit.source.isSnow() + ":" + (unit.restrictions == null ? ""
                    : unit.restrictions.getManaRestrictions() + ":" + unit.restrictions.getExtraManaRestriction());
            if (!tried.add(key)) {
                continue;
            }
            for (final byte color : ManaAtom.MANATYPES) {
                if ((unit.colors & color) == 0) {
                    continue;
                }
                final Mana payment = unit.asMana(color, payer);
                if (!payment.meetsManaRestrictions(spell) || !cost.isNeeded(payment, payer.getManaPool())) {
                    continue;
                }
                final ManaCostBeingPaid rest = new ManaCostBeingPaid(cost);
                rest.payMana(payment, payer.getManaPool());
                final List<ManaUnit> pool = new ArrayList<>(mana.pool);
                pool.remove(i);
                if (payEstimate(payer, spell, rest, new ManaEstimate(pool, mana.sources, mana.life), paid, visited)) {
                    return true;
                }
            }
        }
        if (mana.life >= 2 && cost.containsPhyrexianMana() && ComputerUtilMana.canPayLifeForManaShard(payer, spell, 2)) {
            final ManaCostBeingPaid rest = new ManaCostBeingPaid(cost);
            rest.payPhyrexian();
            if (payEstimate(payer, spell, rest, new ManaEstimate(mana.pool, mana.sources, mana.life - 2), paid, visited)) {
                return true;
            }
        }
        for (int i = 0; i < mana.sources.size(); i++) {
            final List<ManaOption> options = mana.sources.get(i);
            final List<List<ManaOption>> sources = new ArrayList<>(mana.sources);
            sources.remove(i);
            for (final ManaOption option : options) {
                if (option.life > mana.life) {
                    continue;
                }
                final ManaEstimate activating = new ManaEstimate(mana.pool, sources, mana.life - option.life);
                if (payEstimate(payer, option.ability, new ManaCostBeingPaid(option.activation), activating, remaining -> {
                    final List<ManaUnit> pool = new ArrayList<>(remaining.pool);
                    pool.addAll(option.output);
                    return payEstimate(payer, spell, cost, new ManaEstimate(pool, remaining.sources, remaining.life), paid, visited);
                }, new HashMap<>())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int spendable(final Player payer, final List<List<ShockSpell>> candidates, final ManaEstimate mana,
            final int used, final Map<Integer, Map<ManaEstimate, Integer>> memo) {
        final Map<ManaEstimate, Integer> values = memo.computeIfAbsent(used, k -> new HashMap<>());
        if (values.containsKey(mana)) {
            return values.get(mana);
        }
        final int[] best = {0};
        for (int i = 0; i < candidates.size(); i++) {
            final int nextUsed = used | (1 << i);
            if (nextUsed == used) {
                continue;
            }
            for (final ShockSpell spell : candidates.get(i)) {
                if (!spell.ability.getPayCosts().isOnlyManaCost()) {
                    continue;
                }
                payEstimate(payer, spell.ability, new ManaCostBeingPaid(spell.mana), mana, remaining -> {
                    final List<ManaUnit> pool = new ArrayList<>(remaining.pool);
                    final Card card = spell.ability.getHostCard();
                    int tokens = 0;
                    boolean treasure = false;
                    for (SpellAbility part = spell.ability; part != null; part = part.getSubAbility()) {
                        final String script = part.getParamOrDefault("TokenScript", "");
                        if (part.getApi() == ApiType.Token && (script.contains("treasure") || script.contains("eldrazi_spawn"))) {
                            tokens = RampPlan.tokenMana(card);
                            treasure = script.contains("treasure");
                        }
                    }
                    for (int n = 0; n < tokens; n++) {
                        pool.add(new ManaUnit(treasure ? MagicColor.ALL_COLORS : (byte) ManaAtom.COLORLESS, card, null));
                    }
                    best[0] = Math.max(best[0], spell.mana.getCMC() + spendable(payer, candidates,
                            new ManaEstimate(pool, remaining.sources, remaining.life), nextUsed, memo));
                    return false;
                }, new HashMap<>());
            }
        }
        values.put(mana, best[0]);
        return best[0];
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
                    final int reserve = Math.max(10, ActivationGuard.nextTurnThreat(payer) + 5);
                    return amount != null && payer.canPayLife(amount, true, sa)
                            && payer.getLife() - amount >= reserve
                            && shocklandEnablesPlay(payer, source, amount, reserve);
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
