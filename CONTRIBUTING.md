# Contributing Guidelines

Thank you for considering contributing to this project! This document outlines the guidelines for contributing to this project.

## AI

Please don't exclusively rely on AI to generate code. If using AI, double check your code. Generally I wouldn't recommend using AI for logic code.

## How to contribute

1. Fork the repository
2. Clone the forked repository
3. Create a new branch
4. Make your changes \
If making changes, please make sure to follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). \
4 spaces for indentation and no `*` imports should be used.
5. Submit a pull request with your new branch

The target branch for pull requests is `master`.

## How to try out the changes

You can do one of two things:
1. Run the [samples](/sample) in the repository, optionally modifying them to test your changes, but remember to revert them before submitting your pull request.
2. Publish your changes to a local Maven repository and use them in your own project. To do this, run the following command in the root directory of the project:
```shell
./gradlew -DLibrariesOnly=true -DDisableSigning=true -DSupabaseVersion="customVersion" publishToMavenLocal
```
Replace `customVersion` with a unique version like `2.5.0-local-1` (to prevent version clashing with offical versions, if using the maven local repository). Then, in your project, add the following to your `build.gradle.kts` file:
```kotlin
repositories {
    mavenLocal()
}
```
And add the following to your dependencies:
```kotlin
implementation("io.github.jan-tennert.supabase:[module]:customVersion")
```

## Public API changes

Every published module keeps a committed dump of its public ABI (Application Binary Interface) under
`<module>/api/` — one `.api` file per JVM/Android target and one `.klib.api` covering all Kotlin
Native, JS and Wasm targets. CI verifies your branch against those dumps.

If your change touches the public surface, the `Check public ABI` job will fail until you re-record
the dump:

```shell
./gradlew -DLibrariesOnly=true apiDump
```

Then commit the updated files under `*/api/` **as part of the same pull request**.

### Reading the diff before you commit it

The dump diff is the review artefact — read it rather than accepting it blindly:

- **Only `+` lines** — the change is additive. Safe in a minor release.
- **Any `-` line, or a line replaced by a differently-shaped one** — the change is
  binary-incompatible. Code already compiled against the previous release will fail at runtime with
  `NoSuchMethodError` / `NoSuchFieldError` even though it still compiles from source. Per
  [SemVer 2.0.0](https://semver.org/#spec-item-8) this requires a major version bump, or a
  deprecation cycle first (`@Deprecated(level = WARNING, replaceWith = ...)` for at least one minor,
  then `ERROR`, then removal).

Two changes look harmless in source but are binary-breaking, and the dump is the only thing that
catches them:

- **Adding a parameter to a `data class`** rewrites `copy`, `copy$default` and adds a `componentN`.
- **Changing the body of an `inline` function or a `@PublishedApi internal` declaration** — these are
  inlined into consumer bytecode, so consumers keep running the *old* body until they recompile.

To check without re-recording:

```shell
./gradlew -DLibrariesOnly=true apiCheck
```

Run `apiDump` on macOS where possible so the Apple targets are genuinely compiled. On other hosts the
unbuildable targets fall back to their previously recorded declarations rather than being dropped
(`keepLocallyUnsupportedTargets`), which keeps the dump correct but leaves those targets unverified.
