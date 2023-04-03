[![Maven Central](https://img.shields.io/maven-central/v/com.rickbusarow.doks/doks-gradle-plugin?style=flat-square)](https://search.maven.org/search?q=com.rickbusarow.doks)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.rickbusarow.doks?style=flat-square)](https://plugins.gradle.org/plugin/com.rickbusarow.doks)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.rickbusarow.doks/doks-gradle-plugin?label=snapshots&server=https%3A%2F%2Foss.sonatype.org&style=flat-square)](https://oss.sonatype.org/#nexus-search;quick~com.rickbusarow.doks)
[![License](https://img.shields.io/badge/license-apache2.0-blue?style=flat-square.svg)](https://opensource.org/licenses/Apache-2.0)

Doks is a lightweight documentation tool that makes it easy to manage and maintain
documentation for your project. Built as a Gradle plugin, Doks allows you to define custom
regex-based search-and-replace rules and extract code samples from source code,
all within your markdown (or other supported language) documentation.

- [Installation](#installation)
- [Usage](#usage)
  - [Defining search-and-replace rules](#defining-search-and-replace-rules)
  - [Sample Code Extraction from Kotlin Files](#sample-code-extraction-from-kotlin-files)
  - [Gradle Tasks](#gradle-tasks)
    - [doks](#doks)
    - [doksCheck](#dokscheck)
- [Contributing](#contributing)
- [License](#license)

## Installation

To use Doks, you'll need to add it as a dependency in your Gradle build script:

<!--doks plugin-with-version:1-->

```kotlin
// build.gradle.kts
plugins {
  id("com.rickbusarow.doks") version "0.1.3-SNAPSHOT"
}
```

<!--/doks-->

For snapshots, Doks uses the older Sonatype host (without "s01"):

```kotlin
// settings.gradle.kts
pluginManagement {
  repositories {
    gradlePluginPortal()
    // Add for SNAPSHOT builds
    maven {
      url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      content {
        includeGroup("com.rickbusarow.doks")
      }
    }
  }
}
```

## Usage

### Defining search-and-replace rules

To define a search-and-replace rule, you'll need to add it to your Gradle build script using the
following syntax:

<details open>
<summary>Kotlin</summary>
<!--doks kotlin-dsl-config-simple,dollar-raw-string:1,buildConfig-version:1-->

```kotlin title="build.gradle.kts"
// build.gradle.kts
plugins {
  id("com.rickbusarow.doks") version "0.1.3-SNAPSHOT"
}

doks {
  // Define a set of documents with rules.
  dokSet {
    // Set the files which will be synced
    docs(projectDir) {
      include("**/*.md", "**/*.mdx")
    }

    // Define a rule used in updating.
    // This rule's name corresponds to the name used in documentation.
    rule("maven-artifact") {
      regex = maven(group = "com\\.example\\.dino")
      // replace any maven coordinate string with one using the current version,
      // where '$1' is the group id, '$2' is the artifact id,
      // and 'CURRENT_VERSION' is just some variable.
      replacement = "$1:$2:$CURRENT_VERSION"
    }
  }
}
```

<!--/doks-->
</details>
<details>
<summary>Groovy</summary>
<!--doks groovy-dsl-config-simple,dollar-raw-string:1,buildConfig-version:1-->

```groovy title="build.gradle"
// build.gradle
plugins {
  id 'com.rickbusarow.doks' version '0.1.3-SNAPSHOT'
}

doks {
  // Define a set of documents with rules.
  dokSet {
    // Set the files which will be synced
    docs(projectDir) {
      include '**/*.md', '**/*.mdx'
    }

    // Define a rule used in updating.
    // This rule's name corresponds to the name used in documentation.
    rule('maven-artifact') {
      regex = maven('com\\.example\\.dino')
      // replace any maven coordinate string with one using the current version,
      // where '$1' is the group id, '$2' is the artifact id,
      // and 'CURRENT_VERSION' is just some variable.
      replacement = "\$1:\$2:$CURRENT_VERSION"
    }
  }
}
```

<!--/doks-->
</details>

Here, `ruleName` is the ID you'll use to reference the rule in your documentation, `regex` is the
regular expression you want to search for, and `replacement` is the replacement text that should be
inserted in its place.

To use the rule in your documentation, simply add a comment tag that references the rule name:

```markdown
<!--doks maven-artifact-->

The plugin is available as a maven artifact at 'com.example.dino:dino-gradle-plugin:0.0.0'.

<!--/doks-->
```

If you'd like to ensure that the rule always works, you can add an expected count to the rule id:

```markdown
<!--doks maven-artifact:1-->
```

If the rule has a count _n_, Doks will assert that the rule's regex has exactly _n_ matches within
the text. This can help protect against silent failures in case formatting or refactoring breaks a
rule.

### Sample Code Extraction from Kotlin Files

Doks supports the extraction of code samples from Kotlin files.
Here's an example of how to use it in your doks configuration block:

<details open>
<summary>Kotlin</summary>
<!--doks kotlin-dsl-config-code-->

```kotlin title="build.gradle.kts"
doks {
  // Define a set of documents with rules.
  dokSet {
    // Set the files which will be synced
    docs(projectDir) {
      include("**/*.md", "**/*.mdx")
    }

    sampleCodeSource("src/kotlin/com/example/dino/sauropod/samples")

    // Define a rule used in updating.
    // This rule's name corresponds to the name used in documentation.
    rule("brachiosaurus") {
      replacement = sourceCode(
        fqName = "com.example.dino.sauropod.samples.BrachiosaurusSample.doTheDino",
        bodyOnly = false,
        codeBlockLanguage = "kotlin"
      )
    }
  }
}
```

<!--/doks-->
</details>
<details>
<summary>Groovy</summary>
<!--doks groovy-dsl-config-code-->

```groovy title="build.gradle"
doks {
  // Define a set of documents with rules.
  dokSet {
    // Set the files which will be synced
    docs(projectDir) {
      include '**/*.md', '**/*.mdx'
    }

    sampleCodeSource 'src/kotlin/com/example/dino/sauropod/samples'

    // Define a rule used in updating.
    // This rule's name corresponds to the name used in documentation.
    rule('brachiosaurus') {
      replacement = sourceCode(
          "com.example.dino.sauropod.samples.BrachiosaurusSample.doTheDino",
          false,
          "kotlin"
          )
    }
  }
}
```

<!--/doks-->
</details>

This will extract the source code from a property named `config` defined
inside `com.example.dino.DinoPluginSample` in the `src/test/kotlin` directory. That code will be
included in the documentation between the opening and closing doks tags, replacing any text in between.

To reference the extracted code in your documentation, you would use the same doks tags as normal:

    Here's an example of how to use the dino plugin:

    <!--doks dino-config-sample-->
    <!--doks END-->

Note, however, that there is no text in between the tags. By default, for sample code, Doks will
replace everything in between the tags will the current sample. There are several reasons for this, but
suffice it to say that it's typically easier to read the raw markdown this way, and it's certainly
easier to write the replacement regex.

Going back to our sample, if the actual code looks like this:

```kotlin
package com.example.dino

class DinoPluginSample {
  val config = """
    plugins {
      id("com.example.dino-plugin") version "0.0.1-SNAPSHOT"
    }

    dinoPlugin {
      favoriteDinosaur.set("Parasaurolophus")
    }
  """.trimIndent()
}
```

then after running `./gradlew doks`, the above snippet will be changed to this:

    Here's an example of how to use the dino plugin:

    <!--doks dino-config-sample-->

    ```kotlin title=build.gradle.kts
    plugins {
      id("com.example.dino-plugin") version "0.0.1-SNAPSHOT"
    }

    dinoPlugin {
      favoriteDinosaur.set("Parasaurolophus")
    }
    ```
    <!--doks END-->

### Gradle Tasks

The Doks plugin adds two main tasks to your Gradle build:

#### doks

The doks task automatically fixes any differences found between your documentation files and
your rules. This is useful for quickly updating your documentation without having to manually edit it.
However, be careful when using this task, as it may overwrite changes which are not accounted for in
your rules.

#### doksCheck

The doksCheck task checks your documentation files for any differences between the actual content
and the content which would be generated by your defined rules. If any discrepancies are found, the
task will fail and display an error message indicating where the differences were found. This is useful
for ensuring that your documentation remains up-to-date and accurate.

## Contributing

If you'd like to contribute to Doks, please submit a pull request with your changes. Bug reports or
feature requests are also welcome in the issue tracker.

## License

```text
Copyright (C) 2023 Rick Busarow
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
     https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
