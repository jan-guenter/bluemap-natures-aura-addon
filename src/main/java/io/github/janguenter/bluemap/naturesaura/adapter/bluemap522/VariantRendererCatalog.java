/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.naturesaura.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.naturesaura.model.NaturesAuraRenderRules;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Keeps each owned variant's original renderer for atomic stock fallback. */
final class VariantRendererCatalog {

    private final Map<Variant, BlockRendererType> originals;

    private VariantRendererCatalog(Map<Variant, BlockRendererType> originals) {
        this.originals = Collections.unmodifiableMap(originals);
    }

    static VariantRendererCatalog wrap(ResourcePack pack, BlockRendererType wrapper) {
        Set<String> targets = new LinkedHashSet<>(NaturesAuraRenderRules.BER_HOSTS);
        targets.addAll(NaturesAuraRenderRules.TINT_HOSTS);
        IdentityHashMap<Variant, BlockRendererType> originals = new IdentityHashMap<>();
        for (String target : targets) {
            var state = pack.getBlockStates().get(Key.parse(target));
            if (state == null) {
                throw new IllegalArgumentException("installed target blockstate is missing");
            }
            int before = originals.size();
            state.forEach(variant -> {
                if (variant.getRenderer() != wrapper) {
                    originals.put(variant, variant.getRenderer());
                    variant.setRenderer(wrapper);
                }
            });
            if (originals.size() == before) {
                throw new IllegalArgumentException("installed target has no variants");
            }
        }
        return new VariantRendererCatalog(originals);
    }

    BlockRendererType original(Variant variant) {
        return originals.getOrDefault(variant, BlockRendererType.DEFAULT);
    }

    int size() {
        return originals.size();
    }
}
