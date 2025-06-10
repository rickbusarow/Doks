# Changelog

## 0.3.1-SNAPSHOT (unreleased)

## [0.3.0] - 2025-06-10

### Fixed

- `kotlin-compiler-embeddable` is no longer leaked into the plugin classpath in https://github.com/rickbusarow/Doks/pull/422

**Full Changelog**: https://github.com/rickbusarow/Doks/compare/0.2.0...0.3.0

## [0.2.0] - 2024-02-22

### Added

- Give useful exceptions when a requested psi body is null by @RBusarow in https://github.com/rickbusarow/Doks/pull/267
- support parsing an entire Kotlin file using its jvm file facade name by @RBusarow in https://github.com/rickbusarow/Doks/pull/316

**Full Changelog**: https://github.com/rickbusarow/Doks/compare/0.1.4...0.1.5

## [0.1.4] - 2023-04-13

### Fixed

- combine all console output for a markdown update into a single `println()`

### Added

- nicer formatting of a markdown file diff

## [0.1.3] - 2023-04-06

### Fixed

- added default parameter support in Groovy by adding `@JvmOverloads` to DSL functions
- Made the parsing of `<!--doks ___-->` tags in Markdown more lenient, by allowing optional whitespace
  characters between words.

### Changed

- Changed the format of the closing tag from `<!--/doks-->` to `<!--doks END-->`. This change may not
  be the final decision and is subject to further discussion.

### Added

- added Groovy config samples to integration tests and README
- print a colorized diff to console when a file has been updated

## [0.1.2] - 2023-03-30

### Changed

- The entire project has been renamed from `Docusync` to `Doks`

### Added

- Implemented a path-based sorting algorithm to prioritize file parsing when searching for sample code.
  This prioritizes the most likely files first, potentially reducing the number of files that need to
  be parsed.

## [0.1.1] - 2023-03-28

### Added

- invoke `docusyncCheck` when invoking `check` by @RBusarow
  in https://github.com/RBusarow/Doks/pull/68
- hook `docusync` into `fix`, use `mustRunAfter` to make each check task run after its fix task by
  @RBusarow in https://github.com/RBusarow/Doks/pull/69

**Full Changelog**: https://github.com/RBusarow/Doks/compare/0.1.0...0.1.1

## [0.1.0] - 2023-03-27

Hello World

[0.1.0]: https://github.com/rbusarow/doks/releases/tag/0.1.0
[0.1.1]: https://github.com/rbusarow/doks/releases/tag/0.1.1
[0.1.2]: https://github.com/rbusarow/doks/releases/tag/0.1.2
[0.1.3]: https://github.com/rbusarow/doks/releases/tag/0.1.3
[0.1.4]: https://github.com/rbusarow/doks/releases/tag/0.1.4
[0.2.0]: https://github.com/rbusarow/doks/releases/tag/0.2.0
[0.3.0]: https://github.com/rbusarow/doks/releases/tag/0.3.0
