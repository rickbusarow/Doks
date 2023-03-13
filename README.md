[![Maven Central](https://img.shields.io/maven-central/v/com.rickbusarow.docusync/docusync-gradle-plugin?style=flat-square)](https://search.maven.org/search?q=com.rickbusarow.docusync)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.rickbusarow.docusync?style=flat-square)](https://plugins.gradle.org/plugin/com.rickbusarow.docusync)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.rickbusarow.docusync/docusync-gradle-plugin?label=snapshots&server=https%3A%2F%2Foss.sonatype.org&style=flat-square)](https://oss.sonatype.org/#nexus-search;quick~com.rickbusarow.docusync)
[![License](https://img.shields.io/badge/license-apache2.0-blue?style=flat-square.svg)](https://opensource.org/licenses/Apache-2.0)

Docusync is a lightweight documentation tool that makes it easy to manage and maintain
documentation for your project. Built as a Gradle plugin, Docusync allows you to define custom
regex-based search-and-replace rules and extract code samples from source code,
all within your markdown (or other supported language) documentation.

### Installation

To use Docusync, you'll need to add it as a dependency in your Gradle build script:

```kotlin
// build.gradle.kts
plugins {
  id("com.rickbusarow.docusync") version "0.0.1-SNAPSHOT"
}
```

### Usage

#### Defining search-and-replace rules

To define a search-and-replace rule, you'll need to add it to your Gradle build script using the
following syntax:

```kotlin
// build.gradle.kts
plugins {
  id("com.rickbusarow.docusync") version "0.0.1-SNAPSHOT"
}

docusync {
  // Define a set of documents with rules.
  docsSet {
    // Set the files which will be synced
    docs(
      fileTree(projectDir) {
        include("**/*.md", "**/*.mdx")
      }
    )

    // Define a rule used in updating.
    // This rule's name corresponds to the name used in documentation.
    rule("maven-artifact") {
      regex = """(com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)$SEMVER_REGEX"""
      // replace any maven coordinate string with one using the current version,
      // where '$1' is the capture group of everything except the version
      // and 'CURRENT_VERSION' is just some variable.
      replacement = "$1$CURRENT_VERSION"
    }
  }
}
```

Here, ruleName is the ID you'll use to reference the rule in your documentation, regex is the regular
expression you want to search for, and replacement is the replacement text that should be inserted in
its place.

To use the rule in your documentation, simply add a comment tag that references the rule name:

```markdown
<!--docusync maven-artifact-->

The plugin is available as a maven artifact at 'com.rickbusarow.docusync:docusync-gradle-plugin:0.0.0'.

<!--/docusync-->
```

If you'd like to ensure that the rule always works, you can add an expected count to the rule id:

```markdown
<!--docusync maven-artifact:1-->
```

If the rule has a count _n_, Docusync will assert that the rule's regex has exactly _n_ matches within
the text. This can help protect against silent failures in case formatting or refactoring breaks a
rule.

### Contributing

If you'd like to contribute to Docusync, please submit a pull request with your changes. Bug reports or
feature requests are also welcome in the issue tracker.

### License

```text
Copyright (C) 2023 Rick Busarow
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
