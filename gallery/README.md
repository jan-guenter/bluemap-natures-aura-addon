# Nature's Aura visual gallery

This deterministic gallery contains 27 cases in the bounded envelope
`(172, 99, 172)` through `(204, 103, 192)`. It covers empty and filled item
hosts, both Wood Stand item branches, the Ender Crate and half-filled End Aura
Timer, all four projectile directions, active limiter arrangements, all four
owned tint families, every golden-leaves stage, connected gold powder, and a
stock stone control.

Run it with:

```text
/function naturesaura_gallery:build
/function naturesaura_gallery:teleport
```

The teleport target is `(188, 110, 182)`. Generate and audit the archive with:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/naturesaura-gallery.zip
```

The datapack contains no candidate assets, captured meshes, entities, or
privilege-changing commands.
