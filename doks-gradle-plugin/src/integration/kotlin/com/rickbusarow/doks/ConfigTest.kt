/*
 * Copyright (C) 2023 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rickbusarow.doks

import com.rickbusarow.doks.internal.stdlib.createSafely
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@Suppress("FunctionName")
internal class ConfigTest : BaseGradleTest {

  @Test
  fun `groovy dsl config simple`() = test {

    val config =
      """
      // build.gradle
      plugins {
        id 'com.rickbusarow.doks' version '${BuildConfig.version}'
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
            replacement = "\$1:\$2:${'$'}CURRENT_VERSION"
          }
        }
      }
      """.trimIndent()

    buildFile.resolveSibling("build.gradle")
      .createSafely(config.replace("doks {", "def CURRENT_VERSION = \"1.0.1\"\n\ndoks {"))

    buildFile.delete()

    workingDir.resolve("README.md")
      .markdown(
        """
        <!--doks maven-artifact:1-->
        ```kotlin
        dependencies {
          implementation("com.example.dino:sauropod:1.0.0")
        }
        ```
        <!--/doks-->
        """
      )

    shouldSucceed("doks")
  }

  @Test
  fun `kotlin dsl config simple`() = test {

    val config =
      //language=kotlin
      """
      // build.gradle.kts
      plugins {
        id("com.rickbusarow.doks") version "${BuildConfig.version}"
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
            replacement = "$1:$2:${'$'}CURRENT_VERSION"
          }
        }
      }
      """.trimIndent()

    workingDir.resolve("README.md")
      .markdown(
        """
        <!--doks maven-artifact:1-->
        ```kotlin
        dependencies {
          implementation("com.example.dino:sauropod:1.0.0")
        }
        ```
        <!--/doks-->
        """
      )

    shouldSucceed("doks")
  }

  @Test
  fun `a default docs collection picks up markdown files from the project root `() = test {

    buildFile.writeText(
      """
      plugins {
        id("com.rickbusarow.doks") version "${BuildConfig.version}"
      }

      val CURRENT_VERSION = "1.0.1"

      doks {
        dokSet {
          rule("maven-artifact") {
            regex = maven(group = "com\\.example\\.dino")
            replacement = "${'$'}1:${'$'}2:${'$'}CURRENT_VERSION"
          }
        }
      }
      """.trimIndent()
    )

    val readme = workingDir.resolve("README.md")
      .markdown(
        """
        <!--doks maven-artifact:1-->
        ```kotlin
        dependencies {
          implementation("com.example.dino:sauropod:1.0.0")
        }
        ```
        <!--/doks-->
        """
      )

    shouldSucceed("doks")

    readme.readText() shouldBe """
      <!--doks maven-artifact:1-->
      ```kotlin
      dependencies {
        implementation("com.example.dino:sauropod:1.0.1")
      }
      ```
      <!--/doks-->
    """.trimIndent()
  }

  @Test
  fun `groovy dsl config code`() = test {

    val config =
      //language=groovy
      """
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
      """.trimIndent()

    buildFile.resolveSibling("build.gradle")
      .createSafely(
        """
        |${buildFile.readText()}
        |
        |$config
        """.replaceIndentByMargin()
      )

    buildFile.delete()

    workingDir.resolve("README.md")
      .markdown(
        """
        <!--doks brachiosaurus-->
        <!--/doks-->
        """
      )

    workingDir.resolve("src/kotlin/com/example/dino/sauropod/samples/BrachiosaurusSample.kt")
      .kotlin(
        """
        package com.example.dino.sauropod.samples

        class BrachiosaurusSample {

          fun doTheDino() {
            stomp()
          }
        }
        """
      )

    shouldSucceed("doks")
  }

  @Test
  fun `kotlin dsl config code`() = test {

    val config =
      //language=kotlin
      """
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
      """.trimIndent()

    buildFile.writeText(
      """
      ${buildFile.readText()}

      $config

      val CURRENT_VERSION = "1.0.1"
      """.trimIndent()
    )

    workingDir.resolve("README.md")
      .markdown(
        """
        <!--doks brachiosaurus-->
        <!--/doks-->
        """
      )

    workingDir.resolve("src/kotlin/com/example/dino/sauropod/samples/BrachiosaurusSample.kt")
      .kotlin(
        """
        package com.example.dino.sauropod.samples

        class BrachiosaurusSample {

          fun doTheDino() {
            stomp()
          }
        }
        """
      )

    shouldSucceed("doks")
  }

  @Test
  fun `a default samples collection picks up kotlin files in src`() = test {

    buildFile.writeText(
      """
      ${buildFile.readText()}

      doks {
        dokSet {
          docs(projectDir) {
            include("**/*.md", "**/*.mdx")
          }
          rule("brachiosaurus") {
            replacement = sourceCode(
              fqName = "com.example.dino.sauropod.samples.BrachiosaurusSample.doTheDino",
              bodyOnly = false,
              codeBlockLanguage = "kotlin"
            )
          }
        }
      }
      """.trimIndent()
    )

    workingDir.resolve("README.md")
      .markdown(
        """
        <!--doks brachiosaurus-->
        <!--/doks-->
        """
      )

    workingDir.resolve("src/kotlin/com/example/dino/sauropod/samples/BrachiosaurusSample.kt")
      .kotlin(
        """
        package com.example.dino.sauropod.samples

        class BrachiosaurusSample {

          fun doTheDino() {
            stomp()
          }
        }
        """
      )

    shouldSucceed("doks") {
    }
  }
}
