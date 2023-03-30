# Changelog

## 0.1.2-SNAPSHOT (unreleased)

### Added

- Implemented a path-based sorting algorithm to prioritize file parsing when searching for sample code.
  This prioritizes the most likely files first, potentially reducing the number of files that need to
  be parsed.

## [0.1.1] - 2023-03-28

### Added

- invoke `docusyncCheck` when invoking `check` by @RBusarow
  in https://github.com/RBusarow/Docusync/pull/68
- hook `docusync` into `fix`, use `mustRunAfter` to make each check task run after its fix task by
  @RBusarow in https://github.com/RBusarow/Docusync/pull/69

**Full Changelog**: https://github.com/RBusarow/Docusync/compare/0.1.0...0.1.1

## [0.1.0] - 2023-03-27

Hello World

[0.1.0]: https://github.com/rbusarow/docusync/releases/tag/0.1.0
[0.1.1]: https://github.com/rbusarow/docusync/releases/tag/0.1.1
