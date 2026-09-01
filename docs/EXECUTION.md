# Add-on execution

This repository starts inactive and stock-safe. Implement only the smallest
observed Nature's Aura rendering defect before staging.

Before running Gradle gates, initialize both exact source submodules, activate
a Python 3.11 or newer virtual environment, and install the development-only
toolkit:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
```

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

The migration candidate records the production JAR, sources JAR, POM, and
Gradle module identities under `candidate_artifacts`. After visual acceptance,
change the provenance status to `owner-accepted-release-candidate` and record
the exact integration run and accepted JAR under `owner_accepted_staging`.

Promote `addon_version` through a pull request, remove every
scaffold implementation marker, and run with all exact candidate properties:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  <exact-candidate-properties> -PreleaseTag=v0.1.0-alpha.2 \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge only after owner acceptance and final-head CI pass this gate. Create an annotated
`v<version>` tag at reviewed `main`; the release workflow independently checks
the tag, exact BlueMap checkout, accepted bytes and draft assets before making
the prerelease public. Publication never deploys to production.
