#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Compact exact-profile cases for the Nature's Aura visual gallery."""

from __future__ import annotations

from dataclasses import dataclass


NAMESPACE = "naturesaura_gallery"
ENVELOPE = (172, 99, 172, 204, 103, 192)
TELEPORT = (188, 110, 182)


@dataclass(frozen=True)
class Placement:
    case_id: str
    label: str
    x: int
    y: int
    z: int
    block_state: str
    expected: str
    nbt: str | None = None


ITEM_GOLD = (
    '{items:{Size:1,Items:[{Slot:0b,id:"minecraft:gold_block",count:1}]}}'
)
ITEM_GOLD_EIGHT = (
    '{items:{Size:1,Items:[{Slot:0b,id:"minecraft:gold_block",count:8}]}}'
)
ITEM_STICK = '{items:{Size:1,Items:[{Slot:0b,id:"minecraft:stick",count:1}]}}'
END_TIMER = (
    '{items:{Size:1,Items:[{Slot:0b,id:"naturesaura:aura_bottle",count:1,'
    'components:{"naturesaura:aura_bottle_data":'
    '{aura_type:"naturesaura:end"}}}]},timer:36000}'
)


PLACEMENTS = (
    Placement("stock-control", "stone stock control", 174, 100, 174,
              "minecraft:stone", "stock-visible"),
    Placement("altar-empty", "empty Nature Altar", 176, 100, 174,
              "naturesaura:nature_altar", "stock-base-no-item"),
    Placement("altar-item", "Nature Altar with block item", 178, 100, 174,
              "naturesaura:nature_altar", "gold-block-above", ITEM_GOLD),
    Placement("offering-empty", "empty Offering Table", 180, 100, 174,
              "naturesaura:offering_table", "stock-base-no-item"),
    Placement("offering-items", "Offering Table with eight items", 182, 100, 174,
              "naturesaura:offering_table", "four-gold-blocks", ITEM_GOLD_EIGHT),
    Placement("stand-block", "Wood Stand block-item branch", 184, 100, 174,
              "naturesaura:wood_stand", "solid-gold-block", ITEM_GOLD),
    Placement("stand-sprite", "Wood Stand flat-item branch", 186, 100, 174,
              "naturesaura:wood_stand", "flat-stick", ITEM_STICK),
    Placement("ender-crate", "Ender Crate portal surface", 188, 100, 174,
              "naturesaura:ender_crate", "portal-top"),
    Placement("timer-end-half", "half-filled End Aura Timer", 190, 100, 174,
              "naturesaura:aura_timer", "half-dark-aura", END_TIMER),

    Placement("projectile-north", "Projectile overlay north", 174, 100, 180,
              "naturesaura:projectile_generator", "overlay-north", "{next_side:2}"),
    Placement("projectile-east", "Projectile overlay east", 176, 100, 180,
              "naturesaura:projectile_generator", "overlay-east", "{next_side:5}"),
    Placement("projectile-south", "Projectile overlay south", 178, 100, 180,
              "naturesaura:projectile_generator", "overlay-south", "{next_side:3}"),
    Placement("projectile-west", "Projectile overlay west", 180, 100, 180,
              "naturesaura:projectile_generator", "overlay-west", "{next_side:4}"),
    Placement("generator-limiter", "active generator limiter", 183, 100, 180,
              "naturesaura:generator_limit_remover", "glint-self-and-above"),
    Placement("generator-target", "limiter projectile target", 183, 101, 180,
              "naturesaura:projectile_generator", "glint-shell", "{next_side:2}"),
    Placement("lower-target-nonfull", "non-full lower-limiter target", 186, 100, 180,
              "naturesaura:spawn_lamp", "dim-glint-shell"),
    Placement("lower-limiter", "lower limiter between targets", 187, 100, 180,
              "naturesaura:lower_limiter", "glint-self"),
    Placement("lower-target-full", "full-cube lower-limiter target", 188, 100, 180,
              "naturesaura:ender_crate", "bright-glint-shell"),

    Placement("ancient-leaves", "fixed pink ancient leaves", 174, 100, 186,
              "naturesaura:ancient_leaves", "pink-tint"),
    Placement("golden-stage-0", "golden leaves biome stage", 176, 100, 186,
              "naturesaura:golden_leaves[stage=0]", "biome-foliage-tint"),
    Placement("golden-stage-1", "golden leaves blend one", 177, 100, 186,
              "naturesaura:golden_leaves[stage=1]", "one-third-gold-tint"),
    Placement("golden-stage-2", "golden leaves blend two", 178, 100, 186,
              "naturesaura:golden_leaves[stage=2]", "two-thirds-gold-tint"),
    Placement("golden-stage-3", "golden leaves final color", 179, 100, 186,
              "naturesaura:golden_leaves[stage=3]", "fixed-yellow-tint"),
    Placement("gold-powder-west", "connected gold powder west", 182, 100, 186,
              "naturesaura:gold_powder", "gold-tint-connected"),
    Placement("gold-powder-center", "connected gold powder center", 183, 100, 186,
              "naturesaura:gold_powder", "gold-tint-connected"),
    Placement("gold-powder-east", "connected gold powder east", 184, 100, 186,
              "naturesaura:gold_powder", "gold-tint-connected"),
    Placement("spring-water", "Spring water-tinted layer", 187, 100, 186,
              "naturesaura:spring", "frame-and-water-visible"),
)
