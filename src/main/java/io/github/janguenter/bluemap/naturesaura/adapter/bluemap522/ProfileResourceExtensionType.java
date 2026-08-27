/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.naturesaura.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.naturesaura.activation.AddonRuntime;

/** Resource extension factory installed before resource-pack construction. */
final class ProfileResourceExtensionType
        implements ResourcePack.Extension<ProfileResourceExtension> {

    private static final Key KEY = Key.parse("bluemap_natures_aura:exact_profile");
    private final AddonRuntime runtime;

    ProfileResourceExtensionType(AddonRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public ProfileResourceExtension create(ResourcePack pack) {
        return new ProfileResourceExtension(pack, runtime);
    }
}
