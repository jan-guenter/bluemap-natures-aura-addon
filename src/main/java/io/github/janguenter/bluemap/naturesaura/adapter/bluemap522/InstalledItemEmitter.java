/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.naturesaura.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules.ItemStackView;

/** Small installed-resource item projection for the three item-display hosts. */
final class InstalledItemEmitter {

    private static final int WHITE = 0xFFFFFF;

    private final ResourcePack resourcePack;
    private final PrimitiveEmitter primitives;

    InstalledItemEmitter(ResourcePack resourcePack, PrimitiveEmitter primitives) {
        this.resourcePack = resourcePack;
        this.primitives = primitives;
    }

    boolean emit(
            Host host,
            ItemStackView item,
            BlockNeighborhood block,
            TileModelView target
    ) {
        if (item == null) {
            return true;
        }
        Key itemId = Key.parse(item.itemId());
        Key blockTexture = Key.parse(
                itemId.getNamespace() + ":block/" + itemId.getValue()
        );
        if (resourcePack.getTextures().get(blockTexture) != null) {
            return emitBlock(host, item.count(), blockTexture, block, target);
        }
        Key itemTexture = Key.parse(
                itemId.getNamespace() + ":item/" + itemId.getValue()
        );
        if (resourcePack.getTextures().get(itemTexture) == null) {
            return false;
        }
        return emitSprite(host, item.count(), itemTexture, block, target);
    }

    private boolean emitBlock(
            Host host,
            int count,
            Key texture,
            BlockNeighborhood block,
            TileModelView target
    ) {
        return switch (host) {
            case ALTAR -> primitives.cube(
                    block, target, texture,
                    centeredCube(0.32F, 1.08F), WHITE, false, false, 0
            );
            case STAND -> primitives.cube(
                    block, target, texture,
                    centeredCube(0.55F, 0.78F), WHITE, false, false, 0
            );
            case OFFERING -> {
                int amount = Math.min(8, (count + 1) / 2);
                boolean emitted = true;
                for (int index = 0; index < amount; index++) {
                    float x = 0.36F + ((index * 37) % 29) / 100F;
                    float z = 0.36F + ((index * 53) % 29) / 100F;
                    float size = 0.18F;
                    emitted &= primitives.cube(
                            block, target, texture,
                            new PrimitiveEmitter.Bounds(
                                    x - size / 2F, 0.90F + index * 0.002F,
                                    z - size / 2F,
                                    x + size / 2F, 0.90F + size + index * 0.002F,
                                    z + size / 2F
                            ),
                            WHITE, false, false, index & 3
                    );
                }
                yield emitted;
            }
        };
    }

    private boolean emitSprite(
            Host host,
            int count,
            Key texture,
            BlockNeighborhood block,
            TileModelView target
    ) {
        if (host == Host.ALTAR) {
            return primitives.horizontalPlane(
                    block, target, texture, 0.31F, 0.69F,
                    1.20F, 0.31F, 0.69F, WHITE, false
            );
        }
        if (host == Host.STAND) {
            return primitives.verticalPlane(
                    block, target, texture, 0.28F, 0.72F,
                    0.82F, 1.26F, 0.40F, WHITE
            );
        }
        int amount = Math.min(8, (count + 1) / 2);
        boolean emitted = true;
        for (int index = 0; index < amount; index++) {
            float offset = index * 0.003F;
            emitted &= primitives.horizontalPlane(
                    block, target, texture,
                    0.35F, 0.65F, 0.91F + offset,
                    0.35F, 0.65F, WHITE, false
            );
        }
        return emitted;
    }

    private static PrimitiveEmitter.Bounds centeredCube(float size, float bottom) {
        float margin = (1F - size) / 2F;
        return new PrimitiveEmitter.Bounds(
                margin, bottom, margin,
                1F - margin, bottom + size, 1F - margin
        );
    }

    enum Host {
        ALTAR,
        OFFERING,
        STAND
    }
}
