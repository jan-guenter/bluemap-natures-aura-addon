/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.naturesaura.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules.ItemStackView;
import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules.TimerFill;
import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules.GoldPowderShape;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NaturesAuraRenderRulesTest {

    @Test
    void goldenLeavesBlendFromBiomeFoliageToExactFinalColor() {
        assertEquals(0x326532, NaturesAuraRenderRules.goldenLeavesTint(0x326532, 0));
        assertEquals(0xF2FF00, NaturesAuraRenderRules.goldenLeavesTint(0x326532, 3));
        assertThrows(IllegalArgumentException.class,
                () -> NaturesAuraRenderRules.goldenLeavesTint(0, 4));
    }

    @Test
    void projectileDirectionsUseExactSerializedOrdinals() {
        assertEquals(3, NaturesAuraRenderRules.projectileQuarterTurns(2));
        assertEquals(2, NaturesAuraRenderRules.projectileQuarterTurns(5));
        assertEquals(1, NaturesAuraRenderRules.projectileQuarterTurns(3));
        assertEquals(0, NaturesAuraRenderRules.projectileQuarterTurns(4));
        assertEquals(-1, NaturesAuraRenderRules.projectileQuarterTurns(1));
    }

    @Test
    void goldPowderUsesFlatPlanesOnlyForDotAndStraightLines() {
        assertEquals(GoldPowderShape.DOT, NaturesAuraRenderRules.goldPowderShape(
                Map.of("north", "none", "east", "none",
                        "south", "none", "west", "none")));
        assertEquals(GoldPowderShape.EAST_WEST,
                NaturesAuraRenderRules.goldPowderShape(
                        Map.of("north", "none", "east", "side",
                                "south", "none", "west", "none")));
        assertEquals(GoldPowderShape.EAST_WEST,
                NaturesAuraRenderRules.goldPowderShape(
                        Map.of("north", "none", "east", "side",
                                "south", "none", "west", "side")));
        assertEquals(GoldPowderShape.NORTH_SOUTH,
                NaturesAuraRenderRules.goldPowderShape(
                        Map.of("north", "side", "east", "none",
                                "south", "side", "west", "none")));
        assertEquals(GoldPowderShape.STOCK, NaturesAuraRenderRules.goldPowderShape(
                Map.of("north", "side", "east", "side",
                        "south", "none", "west", "none")));
        assertEquals(GoldPowderShape.STOCK, NaturesAuraRenderRules.goldPowderShape(
                Map.of("north", "up", "east", "none",
                        "south", "none", "west", "none")));
    }

    @Test
    void itemProjectionReadsSlotZeroAndRejectsMalformedData() {
        ItemStackView item = NaturesAuraRenderRules.firstItem(Map.of(
                "Size", 1,
                "Items", List.of(Map.of(
                        "Slot", (byte) 0,
                        "id", "minecraft:gold_block",
                        "count", 8
                ))
        ));

        assertEquals(new ItemStackView("minecraft:gold_block", 8, null), item);
        assertNull(NaturesAuraRenderRules.firstItem(Map.of(
                "Items", List.of(Map.of("Slot", 1, "id", "bad", "count", 1))
        )));
    }

    @Test
    void endTimerUsesExactCapacityAndRemainingFraction() {
        ItemStackView item = NaturesAuraRenderRules.firstItem(Map.of(
                "Items", List.of(Map.of(
                        "Slot", 0,
                        "id", "naturesaura:aura_bottle",
                        "count", 1,
                        "components", Map.of(
                                "naturesaura:aura_bottle_data",
                                Map.of("aura_type", "naturesaura:end")
                        )
                ))
        ));
        TimerFill fill = NaturesAuraRenderRules.timerFill(item, 36_000);

        assertEquals(0x302624, fill.color());
        assertEquals(0.5F, fill.fraction());
        assertNull(NaturesAuraRenderRules.timerFill(item, 72_001));
    }

    @Test
    void ownedTopologyIsBoundedToEightRenderersAndFourTints() {
        assertEquals(8, NaturesAuraRenderRules.BER_HOSTS.size());
        assertEquals(4, NaturesAuraRenderRules.TINT_HOSTS.size());
        assertEquals(8, NaturesAuraRenderRules.GENERATOR_TARGETS.size());
        assertEquals(13, NaturesAuraRenderRules.LOWER_LIMITER_TARGETS.size());
    }
}
