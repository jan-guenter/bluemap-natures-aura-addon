/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.naturesaura.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** BlueNBT projection of the three 41.9 fields needed by the static renderer. */
public final class NaturesAuraBlockEntityData extends MCABlockEntity {

    private Object items;
    private Integer timer;

    @NBTName("next_side")
    private Integer nextSide;

    public NaturesAuraBlockEntityData() {
    }

    Object items() {
        return items;
    }

    Integer timer() {
        return timer;
    }

    Integer nextSide() {
        return nextSide;
    }
}
