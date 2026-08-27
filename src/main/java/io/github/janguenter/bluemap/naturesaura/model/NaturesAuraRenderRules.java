/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.naturesaura.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact 41.9 renderer constants and bounded block-entity projections. */
public final class NaturesAuraRenderRules {

    public static final int ANCIENT_LEAVES = 0xE55B97;
    public static final int GOLD_POWDER = 0xF4CB42;
    public static final int GOLDEN_LEAVES = 0xF2FF00;

    public static final Set<String> BER_HOSTS = Set.of(
            id("nature_altar"), id("offering_table"), id("wood_stand"),
            id("ender_crate"), id("aura_timer"), id("projectile_generator"),
            id("generator_limit_remover"), id("lower_limiter")
    );
    public static final Set<String> TINT_HOSTS = Set.of(
            id("ancient_leaves"), id("golden_leaves"),
            id("gold_powder"), id("spring")
    );
    public static final Set<String> GENERATOR_TARGETS = Set.of(
            id("flower_generator"), id("potion_generator"),
            id("projectile_generator"), id("moss_generator"),
            id("oak_generator"), id("animal_generator"),
            id("firework_generator"), id("slime_split_generator")
    );
    public static final Set<String> LOWER_LIMITER_TARGETS = Set.of(
            id("chunk_loader"), id("ender_crate"), id("weather_changer"),
            id("field_creator"), id("spring"), id("snow_creator"),
            id("placer"), id("time_changer"), id("blast_furnace_booster"),
            id("furnace_heater"), id("hopper_upgrade"), id("spawn_lamp"),
            id("animal_spawner")
    );

    private static final Map<String, AuraType> AURA_TYPES = Map.of(
            id("overworld"), new AuraType(0x89CC37, 20),
            id("nether"), new AuraType(0x871C0C, 1_200),
            id("end"), new AuraType(0x302624, 72_000)
    );

    private NaturesAuraRenderRules() {
    }

    public static int goldenLeavesTint(int foliage, int stage) {
        if (stage < 0 || stage > 3) {
            throw new IllegalArgumentException("golden-leaves stage is outside 0..3");
        }
        return blend(foliage, GOLDEN_LEAVES, stage / 3F);
    }

    public static GoldPowderShape goldPowderShape(Map<String, String> properties) {
        String north = properties.get("north");
        String east = properties.get("east");
        String south = properties.get("south");
        String west = properties.get("west");
        if (!powderSide(north) || !powderSide(east)
                || !powderSide(south) || !powderSide(west)
                || "up".equals(north) || "up".equals(east)
                || "up".equals(south) || "up".equals(west)) {
            return GoldPowderShape.STOCK;
        }
        boolean northSouth = "side".equals(north) || "side".equals(south);
        boolean eastWest = "side".equals(east) || "side".equals(west);
        if (!northSouth && !eastWest) {
            return GoldPowderShape.DOT;
        }
        if (northSouth && !eastWest) {
            return GoldPowderShape.NORTH_SOUTH;
        }
        if (eastWest && !northSouth) {
            return GoldPowderShape.EAST_WEST;
        }
        return GoldPowderShape.STOCK;
    }

    public static int projectileQuarterTurns(Integer ordinal) {
        if (ordinal == null) {
            return 3;
        }
        return switch (ordinal) {
            case 2 -> 3; // north
            case 5 -> 2; // east
            case 3 -> 1; // south
            case 4 -> 0; // west
            default -> -1;
        };
    }

    public static ItemStackView firstItem(Object rawItems) {
        if (!(rawItems instanceof Map<?, ?> inventory)) {
            return null;
        }
        Object rawList = inventory.get("Items");
        if (!(rawList instanceof List<?> list)) {
            return null;
        }
        for (Object rawEntry : list) {
            if (!(rawEntry instanceof Map<?, ?> entry)
                    || number(entry.get("Slot"), -1) != 0) {
                continue;
            }
            Object rawId = entry.get("id");
            int count = number(entry.containsKey("count")
                    ? entry.get("count") : entry.get("Count"), 0);
            if (!(rawId instanceof String itemId)
                    || !validId(itemId) || count <= 0 || count > 64) {
                return null;
            }
            return new ItemStackView(itemId, count, auraType(entry.get("components")));
        }
        return null;
    }

    public static TimerFill timerFill(ItemStackView item, Integer timer) {
        if (item == null || !id("aura_bottle").equals(item.itemId())
                || item.auraType() == null) {
            return null;
        }
        AuraType type = AURA_TYPES.get(item.auraType());
        if (type == null) {
            return null;
        }
        long totalLong = (long) type.ticksPerBottle() * item.count();
        if (totalLong <= 0 || totalLong > Integer.MAX_VALUE) {
            return null;
        }
        int total = (int) totalLong;
        int elapsed = timer == null ? 0 : timer;
        if (elapsed < 0 || elapsed > total) {
            return null;
        }
        return new TimerFill(type.color(), 1F - elapsed / (float) total);
    }

    private static String auraType(Object rawComponents) {
        if (!(rawComponents instanceof Map<?, ?> components)) {
            return null;
        }
        Object rawData = components.get("naturesaura:aura_bottle_data");
        if (!(rawData instanceof Map<?, ?> data)) {
            return null;
        }
        Object rawType = data.get("aura_type");
        return rawType instanceof String type && validId(type) ? type : null;
    }

    private static int blend(int first, int second, float amount) {
        int red = Math.round(channel(first, 16) * (1F - amount)
                + channel(second, 16) * amount);
        int green = Math.round(channel(first, 8) * (1F - amount)
                + channel(second, 8) * amount);
        int blue = Math.round(channel(first, 0) * (1F - amount)
                + channel(second, 0) * amount);
        return red << 16 | green << 8 | blue;
    }

    private static int channel(int color, int shift) {
        return color >>> shift & 0xFF;
    }

    private static boolean powderSide(String value) {
        return "none".equals(value) || "side".equals(value) || "up".equals(value);
    }

    private static int number(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static boolean validId(String value) {
        return value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    private static String id(String path) {
        return "naturesaura:" + path;
    }

    public record ItemStackView(String itemId, int count, String auraType) {
    }

    public record TimerFill(int color, float fraction) {

        public TimerFill {
            if (fraction < 0F || fraction > 1F) {
                throw new IllegalArgumentException("timer fill is outside 0..1");
            }
        }
    }

    public enum GoldPowderShape {
        DOT,
        NORTH_SOUTH,
        EAST_WEST,
        STOCK
    }

    private record AuraType(int color, int ticksPerBottle) {
    }
}
