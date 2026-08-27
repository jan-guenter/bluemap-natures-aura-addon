#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated Nature's Aura gallery without starting Minecraft."""

from __future__ import annotations

import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import cases
import generate


ROOT = Path(__file__).resolve().parent
EXPECTED_CASES = 27


def main() -> int:
    for relative, payload in generate.generated_files().items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            raise ValueError(f"generated file differs: {relative}")

    json.loads((ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8"))
    load_tag = json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )
    if load_tag != {"values": [f"{cases.NAMESPACE}:load"]}:
        raise ValueError("load tag differs from the exact namespace")
    if len(cases.PLACEMENTS) != EXPECTED_CASES:
        raise ValueError(f"gallery must contain exactly {EXPECTED_CASES} cases")
    if len({placement.case_id for placement in cases.PLACEMENTS}) != EXPECTED_CASES:
        raise ValueError("gallery case identifiers must be unique")

    minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z = (
        cases.ENVELOPE
    )
    for placement in cases.PLACEMENTS:
        if not (
            minimum_x <= placement.x <= maximum_x
            and minimum_y <= placement.y <= maximum_y
            and minimum_z <= placement.z <= maximum_z
        ):
            raise ValueError(f"placement escaped envelope: {placement.case_id}")

    states = {placement.block_state.split("[", 1)[0] for placement in cases.PLACEMENTS}
    required = {
        "naturesaura:nature_altar", "naturesaura:offering_table",
        "naturesaura:wood_stand", "naturesaura:ender_crate",
        "naturesaura:aura_timer", "naturesaura:projectile_generator",
        "naturesaura:generator_limit_remover", "naturesaura:lower_limiter",
        "naturesaura:ancient_leaves", "naturesaura:golden_leaves",
        "naturesaura:gold_powder", "naturesaura:spring",
    }
    if not required.issubset(states):
        raise ValueError("gallery omits an owned renderer host")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    if len(re.findall(r"^setblock ", functions, re.MULTILINE)) != EXPECTED_CASES:
        raise ValueError("generated setblock count differs from cases")
    expected_merges = sum(placement.nbt is not None for placement in cases.PLACEMENTS)
    if len(re.findall(r"^data merge block ", functions, re.MULTILINE)) != expected_merges:
        raise ValueError("generated block-entity merge count differs from cases")
    lowered = functions.lower()
    for forbidden in ("summon ", "op ", "deop ", "stop "):
        if forbidden in lowered:
            raise ValueError(f"forbidden gallery command: {forbidden}")
    print(
        f"Nature's Aura gallery lint passed: {EXPECTED_CASES} bounded cases, "
        f"{expected_merges} block-entity fixtures"
    )
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
