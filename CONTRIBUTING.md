# Contributing Guidelines

Thank you for considering contributing to this project! This document outlines the guidelines for contributing to this project.

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
