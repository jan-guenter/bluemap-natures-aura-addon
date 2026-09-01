# BlueMap Nature's Aura Add-on

A Java 21 BlueMap add-on for the exact `naturesaura-41.9-mc1.21.1` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Version `0.1.0-alpha.2` is the owner-accepted native BlueMap 5.23 release
candidate. It preserves the owner-accepted `0.1.0-alpha.1` rendering contract.
It preserves BlueMap's stock models and adds installed-resource projections
for eight block-entity render hosts plus the four audited tint defects.

## Build

Clone with `--recurse-submodules`, or initialize an existing checkout with
`git submodule update --init --recursive -- tooling/bluemap-addon-toolkit
modules/bluemap-addon-adapter-api`, before invoking Gradle.

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PnaturesAuraJar=/path/to/NaturesAura-41.9.jar \
  -PreleaseTag=v0.1.0-alpha.2 clean prototypeCheck build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the bounded visual
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

The add-on compiles the four helpers from the exact Adapter API source-module
gitlink. Its standalone JAR is neither installed nor nested.

## Install

Place the production JAR in BlueMap's add-on pack directory and restart the
BlueMap JVM. Removal plus one restart restores stock
behavior; the add-on creates no custom world state.

Set `-Dbluemap.naturesaura.disabled=true` to leave the exact profile inactive.

## Scope boundary

The initial implementation is limited to the exact audited 41.9 renderer
hosts. It uses representative static phases for item poses and glint overlays;
particles, time-dependent animation, and unsupported states stay stock or are
omitted without replacing the stock base.

No Nature's Aura binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
