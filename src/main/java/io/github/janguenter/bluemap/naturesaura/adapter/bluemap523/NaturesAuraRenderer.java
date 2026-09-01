/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.naturesaura.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.map.hires.block.color.BlockColorCalculator;
import de.bluecolored.bluemap.core.map.hires.block.color.BlockColorCalculatorType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.naturesaura.activation.AddonRuntime;
import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules;
import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules.ItemStackView;
import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules.TimerFill;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Stock-preserving renderer for the eight BER hosts and four tint defects. */
final class NaturesAuraRenderer implements BlockRenderer {

    private static final ThreadLocal<Boolean> STOCK_FALLBACK =
            ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Set<String> DIAGNOSTICS = ConcurrentHashMap.newKeySet();
    private static final PrimitiveEmitter.Bounds FULL_CUBE =
            new PrimitiveEmitter.Bounds(0F, 0F, 0F, 1F, 1F, 1F);

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final AddonRuntime runtime;
    private final VariantRendererCatalog variants;
    private final PrimitiveEmitter primitives;
    private final InstalledItemEmitter items;
    private final BlockColorCalculator foliage;
    private final BlockColorCalculator water;
    private final Map<BlockRendererType, BlockRenderer> stockRenderers =
            new IdentityHashMap<>();

    NaturesAuraRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
        this.runtime = runtime;
        variants = RendererDataRegistry.get(resourcePack);
        primitives = new PrimitiveEmitter(resourcePack, textures, settings);
        items = new InstalledItemEmitter(resourcePack, primitives);
        foliage = BlockColorCalculatorType.FOLIAGE.create(resourcePack);
        water = BlockColorCalculatorType.WATER.create(resourcePack);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        Color initialColor = new Color().set(mapColor);
        try {
            if (!runtime.active() || variants == null) {
                stock(block, variant, target, mapColor);
                return;
            }
            String id = block.getBlockState().getId().getFormatted();
            if (NaturesAuraRenderRules.TINT_HOSTS.contains(id)) {
                renderTint(id, block, variant, target, mapColor);
            } else if (NaturesAuraRenderRules.BER_HOSTS.contains(id)) {
                stock(block, variant, target, mapColor);
                renderOverlay(id, block, target);
            } else {
                stock(block, variant, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            reset(target, start, mapColor, initialColor);
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            reset(target, start, mapColor, initialColor);
            diagnose(block.getBlockState().getId().getFormatted(),
                    exception.getClass().getSimpleName());
            runtime.inactive("renderer-" + exception.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start, initialColor);
        }
    }

    private void renderTint(
            String id,
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if ("naturesaura:spring".equals(id)) {
            renderSpring(block, target, mapColor);
            return;
        }
        int start = target.getTileModel().size();
        stock(block, variant, target, mapColor);
        int tint = switch (id) {
            case "naturesaura:ancient_leaves" -> NaturesAuraRenderRules.ANCIENT_LEAVES;
            case "naturesaura:gold_powder" -> NaturesAuraRenderRules.GOLD_POWDER;
            case "naturesaura:golden_leaves" -> goldenTint(block);
            default -> throw new IllegalArgumentException("unknown tint host");
        };
        applyTint(target, start, tint);
        multiplyMapColor(mapColor, tint);
        if (!NaturesAuraRenderRules.contributesLowResolutionColor(id)) {
            mapColor.set(0F, 0F, 0F, 0F, true);
        }
    }

    private void renderSpring(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        int waterTint = water.getBlockColor(block, new Color()).straight().getInt();
        if (!primitives.cube(block, target, ProfileResourceExtension.SPRING_FRAME,
                FULL_CUBE, 0xFFFFFF, false, false, 0)
                || !primitives.cube(block, target, ProfileResourceExtension.SPRING_WATER,
                FULL_CUBE, waterTint, false, false, 0)) {
            throw new IllegalStateException("spring resources unavailable");
        }
        Texture frame = resourcePack.getTextures().get(ProfileResourceExtension.SPRING_FRAME);
        Texture liquid = resourcePack.getTextures().get(ProfileResourceExtension.SPRING_WATER);
        mapColor.set(frame.getColorPremultiplied());
        Color waterColor = new Color().set(liquid.getColorPremultiplied());
        multiplyMapColor(waterColor, waterTint);
        mapColor.add(waterColor).flatten().straight();
    }

    private void renderOverlay(
            String id,
            BlockNeighborhood block,
            TileModelView target
    ) {
        NaturesAuraBlockEntityData data = block.getBlockEntity()
                instanceof NaturesAuraBlockEntityData found ? found : null;
        ItemStackView item = data == null
                ? null : NaturesAuraRenderRules.firstItem(data.items());
        boolean emitted = switch (id) {
            case "naturesaura:nature_altar" -> items.emit(
                    InstalledItemEmitter.Host.ALTAR, item, block, target
            );
            case "naturesaura:offering_table" -> items.emit(
                    InstalledItemEmitter.Host.OFFERING, item, block, target
            );
            case "naturesaura:wood_stand" -> items.emit(
                    InstalledItemEmitter.Host.STAND, item, block, target
            );
            case "naturesaura:ender_crate" -> primitives.horizontalPlane(
                    block, target, ProfileResourceExtension.END_PORTAL,
                    0.0625F, 0.9375F, 1.001F,
                    0.0625F, 0.9375F, 0xFFFFFF, true
            );
            case "naturesaura:aura_timer" -> emitTimer(data, item, block, target);
            case "naturesaura:projectile_generator" -> emitProjectile(
                    data, block, target
            );
            case "naturesaura:generator_limit_remover" -> emitGeneratorLimiter(
                    block, target
            );
            case "naturesaura:lower_limiter" -> emitLowerLimiter(block, target);
            default -> false;
        };
        if (!emitted) {
            diagnose(id, "overlay-omitted");
        }
    }

    private boolean emitTimer(
            NaturesAuraBlockEntityData data,
            ItemStackView item,
            BlockNeighborhood block,
            TileModelView target
    ) {
        TimerFill fill = data == null
                ? null : NaturesAuraRenderRules.timerFill(item, data.timer());
        if (fill == null || fill.fraction() <= 0F) {
            return true;
        }
        float top = 0.1250625F + 0.40625F * fill.fraction();
        return primitives.cube(
                block, target, ProfileResourceExtension.TIMER_AURA,
                new PrimitiveEmitter.Bounds(
                        0.25F, 0.1250625F, 0.25F,
                        0.75F, top, 0.75F
                ),
                fill.color(), true, false, 0
        );
    }

    private boolean emitProjectile(
            NaturesAuraBlockEntityData data,
            BlockNeighborhood block,
            TileModelView target
    ) {
        int turns = NaturesAuraRenderRules.projectileQuarterTurns(
                data == null ? null : data.nextSide()
        );
        return turns < 0 || primitives.cube(
                block, target, ProfileResourceExtension.PROJECTILE_OVERLAY,
                new PrimitiveEmitter.Bounds(
                        -0.002F, 0F, 0F, 0.998F, 1F, 1F
                ),
                0xFFFFFF, true, true, turns
        );
    }

    private boolean emitGeneratorLimiter(
            BlockNeighborhood block,
            TileModelView target
    ) {
        ExtendedBlock above = block.getNeighborBlock(0, 1, 0);
        if (!NaturesAuraRenderRules.GENERATOR_TARGETS.contains(
                above.getBlockState().getId().getFormatted())) {
            return true;
        }
        return shell(block, target, ProfileResourceExtension.GENERATOR_GLINT,
                0, 0, 0, 0xFFFFFF)
                && shell(block, target, ProfileResourceExtension.GENERATOR_GLINT,
                0, 1, 0, 0xFFFFFF);
    }

    private boolean emitLowerLimiter(
            BlockNeighborhood block,
            TileModelView target
    ) {
        int[][] offsets = {
                {0, -1, 0}, {0, 1, 0}, {0, 0, -1},
                {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}
        };
        boolean found = false;
        boolean emitted = true;
        for (int[] offset : offsets) {
            ExtendedBlock neighbor = block.getNeighborBlock(
                    offset[0], offset[1], offset[2]
            );
            if (!NaturesAuraRenderRules.LOWER_LIMITER_TARGETS.contains(
                    neighbor.getBlockState().getId().getFormatted())) {
                continue;
            }
            found = true;
            int tint = neighbor.getProperties().isOccluding() ? 0xFFFFFF : 0x888888;
            emitted &= shell(block, target, ProfileResourceExtension.LOWER_GLINT,
                    offset[0], offset[1], offset[2], tint);
        }
        return !found || emitted && shell(
                block, target, ProfileResourceExtension.LOWER_GLINT,
                0, 0, 0, 0xFFFFFF
        );
    }

    private boolean shell(
            BlockNeighborhood block,
            TileModelView target,
            Key texture,
            int x,
            int y,
            int z,
            int tint
    ) {
        return primitives.cube(
                block, target, texture,
                new PrimitiveEmitter.Bounds(
                        x - 0.002F, y - 0.002F, z - 0.002F,
                        x + 1.002F, y + 1.002F, z + 1.002F
                ),
                tint, true, true, 0
        );
    }

    private int goldenTint(BlockNeighborhood block) {
        String rawStage = block.getBlockState().getProperties().get("stage");
        int stage;
        try {
            stage = Integer.parseInt(rawStage);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("golden-leaves stage missing", exception);
        }
        int foliageColor = foliage.getBlockColor(block, new Color()).straight().getInt();
        return NaturesAuraRenderRules.goldenLeavesTint(foliageColor, stage);
    }

    private void stock(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (STOCK_FALLBACK.get()) {
            return;
        }
        STOCK_FALLBACK.set(Boolean.TRUE);
        try {
            BlockRendererType type = variants == null
                    ? BlockRendererType.DEFAULT : variants.original(variant);
            stockRenderers.computeIfAbsent(
                    type,
                    found -> found.create(resourcePack, textures, settings)
            ).render(block, variant, target, mapColor);
        } finally {
            STOCK_FALLBACK.set(Boolean.FALSE);
        }
    }

    private void stockSafely(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int start,
            Color initialColor
    ) {
        try {
            stock(block, variant, target, mapColor);
        } catch (RuntimeException exception) {
            reset(target, start, mapColor, initialColor);
            runtime.inactive("stock-fallback-" + exception.getClass().getSimpleName());
        }
    }

    private static void applyTint(TileModelView target, int start, int color) {
        float red = (color >>> 16 & 0xFF) / 255F;
        float green = (color >>> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;
        for (int index = start; index < target.getTileModel().size(); index++) {
            target.getTileModel().setColor(index, red, green, blue);
        }
    }

    private static void multiplyMapColor(Color color, int tint) {
        color.r *= (tint >>> 16 & 0xFF) / 255F;
        color.g *= (tint >>> 8 & 0xFF) / 255F;
        color.b *= (tint & 0xFF) / 255F;
    }

    private static void reset(
            TileModelView target,
            int start,
            Color mapColor,
            Color initialColor
    ) {
        target.getTileModel().reset(start);
        target.initialize(start);
        mapColor.set(initialColor);
    }

    private static void diagnose(String id, String outcome) {
        String key = id + ':' + outcome;
        if (DIAGNOSTICS.add(key)) {
            System.out.println("BlueMap Nature's Aura diagnostic: "
                    + id + " -> " + outcome);
        }
    }
}
