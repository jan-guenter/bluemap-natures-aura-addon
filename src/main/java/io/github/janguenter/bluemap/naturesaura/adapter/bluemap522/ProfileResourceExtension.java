/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.naturesaura.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.naturesaura.activation.AddonRuntime;
import io.github.janguenter.bluemap.naturesaura.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.naturesaura.profile.NaturesAura419Profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/** Exact-artifact admission, installed-resource validation and target routing. */
final class ProfileResourceExtension implements ResourcePackExtension {

    static final Key TIMER_AURA = Key.parse("naturesaura:models/aura_timer_aura");
    static final Key PROJECTILE_OVERLAY =
            Key.parse("naturesaura:models/projectile_generator_overlay");
    static final Key GENERATOR_GLINT =
            Key.parse("naturesaura:models/generator_limit_remover_glint");
    static final Key LOWER_GLINT =
            Key.parse("naturesaura:models/lower_limiter_glint");
    static final Key SPRING_FRAME = Key.parse("naturesaura:block/spring");
    static final Key SPRING_WATER = Key.parse("naturesaura:block/spring_water");
    static final Key END_PORTAL = Key.parse("minecraft:environment/end_sky");
    private static final Map<Key, Integer> REQUIRED_TEXTURES = Map.of(
            TIMER_AURA, 64,
            PROJECTILE_OVERLAY, 64,
            GENERATOR_GLINT, 64,
            LOWER_GLINT, 64,
            SPRING_FRAME, 16,
            SPRING_WATER, 16,
            END_PORTAL, 128
    );

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;
    private boolean admitted;

    ProfileResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        admitted = false;
        if (Boolean.getBoolean("bluemap.naturesaura.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactArtifactDetector.matchesAll(roots, NaturesAura419Profile.ARTIFACTS)) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        admitted = true;
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return admitted ? REQUIRED_TEXTURES.keySet() : Set.of();
    }

    @Override
    public void bake() {
        if (!admitted) {
            return;
        }
        try {
            for (Map.Entry<Key, Integer> entry : REQUIRED_TEXTURES.entrySet()) {
                if (!validTexture(entry.getKey(), entry.getValue())) {
                    runtime.inactive("installed-render-resource-invalid");
                    return;
                }
            }
            VariantRendererCatalog variants = VariantRendererCatalog.wrap(
                    resourcePack, renderer
            );
            RendererDataRegistry.install(resourcePack, variants);
            runtime.activate();
            System.out.println("BlueMap Nature's Aura add-on active: wrapped "
                    + variants.size() + " exact variants across 12 blocks.");
        } catch (IOException | RuntimeException exception) {
            runtime.inactive("route-install-" + exception.getClass().getSimpleName());
        }
    }

    private boolean validTexture(Key key, int expectedSize) throws IOException {
        Texture texture = resourcePack.getTextures().get(key);
        if (texture == null) {
            return false;
        }
        BufferedImage image = texture.getTextureImage();
        return image != null && image.getWidth() == expectedSize
                && image.getHeight() >= expectedSize;
    }
}
