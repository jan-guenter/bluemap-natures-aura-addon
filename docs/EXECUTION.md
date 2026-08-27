# Add-on execution

This repository starts inactive and stock-safe. Implement only the smallest
observed Nature's Aura rendering defect before staging.

## Prototype

Acquire and verify the exact candidate JARs outside Git. Their Gradle
properties are:

- `-PnaturesAuraJar=/path/to/NaturesAura-41.9.jar`

Then run:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  <exact-candidate-properties> clean prototypeCheck build
bash gallery/package.sh /tmp/naturesaura-gallery.zip
```

Deploy that JAR and gallery only to disposable staging, verify the intended
BlueMap link loads, and compare it with the matching client. Iterate from
observed defects until the owner explicitly accepts one exact staging JAR.

## Acceptance and release

Freeze that accepted JAR's functional entries once; the writer refuses to
overwrite an existing acceptance record:

```bash
python tools/verify_staged_equivalence.py \
  --jar /absolute/path/accepted-staging.jar \
  --entries provenance/accepted-staging-entries.sha256 --write
```

Record the manifest in `provenance/release.json` as
`accepted_staging_entries` with exact `path`, `entry_count`, and `sha256`.
Record `visual_acceptance: true` under `owner_accepted_staging`, and record the
production JAR, sources JAR, POM and Gradle module file names, sizes and hashes
under `final_release_artifacts`.

Promote `addon_version` through a pull request, remove every
scaffold implementation marker, and run with all exact candidate properties:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  <exact-candidate-properties> -PreleaseTag=v<version> \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge only after final-version CI passes this gate. Create an annotated
`v<version>` tag at reviewed `main`; the release workflow independently checks
the tag, exact BlueMap checkout, accepted bytes and draft assets before making
the prerelease public. Publication never deploys to production.
