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

package com.rickbusarow.docusync.gradle

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@Suppress("FunctionName")
internal class ConfigTest : BaseGradleTest {

  @Test
  fun `kotlin dsl config simple`() = test {

    val config =
      //language=kotlin
      """
      // build.gradle.kts
      plugins {
        id("com.rickbusarow.docusync") version "${BuildConfig.version}"
      }

      docusync {
        // Define a set of documents with rules.
        docSet {
          // Set the files which will be synced
          docs(projectDir) {
            include("**/*.md", "**/*.mdx")
          }

          // Define a rule used in updating.
          // This rule's name corresponds to the name used in documentation.
          rule("maven-artifact") {
            regex = maven(group = "com\\.example\\.dino")
            // replace any maven coordinate string with one using the current version,
            // where '${'$'}1' is the group id, '${'$'}2' is the artifact id,
            // and 'CURRENT_VERSION' is just some variable.
            replacement = "${'$'}1:${'$'}2:${'$'}CURRENT_VERSION"
          }
        }
      }
      """.trimIndent()

    buildFile.writeText("$config\n\nval CURRENT_VERSION = \"1.0.1\"\n")

    workingDir.resolve("README.md")
      .markdown(
        """
        <!--docusync maven-artifact:1-->
        ```kotlin
        dependencies {
          implementation("com.example.dino:sauropod:1.0.0")
        }
        ```
        <!--/docusync-->
        """
      )

    shouldSucceed("docusync")
  }

  @Test
  fun `a default docs collection picks up markdown files from the project root `() = test {

    buildFile.writeText(
      """
      plugins {
        id("com.rickbusarow.docusync") version "${BuildConfig.version}"
      }

      val CURRENT_VERSION = "1.0.1"

      docusync {
        docSet {
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
        <!--docusync maven-artifact:1-->
        ```kotlin
        dependencies {
          implementation("com.example.dino:sauropod:1.0.0")
        }
        ```
        <!--/docusync-->
        """
      )

    shouldSucceed("docusync")

    readme.readText() shouldBe """
      <!--docusync maven-artifact:1-->
      ```kotlin
      dependencies {
        implementation("com.example.dino:sauropod:1.0.1")
      }
      ```
      <!--/docusync-->
    """.trimIndent()
  }

  @Test
  fun `kotlin dsl config code`() = test {

    val config =
      //language=kotlin
      """
      docusync {
        // Define a set of documents with rules.
        docSet {
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
        <!--docusync brachiosaurus-->
        <!--/docusync-->
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

    shouldSucceed("docusync")
  }

  @Test
  fun `a default samples collection picks up kotlin files in src`() = test {

    buildFile.writeText(
      """
      ${buildFile.readText()}

      docusync {
        docSet {
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
        <!--docusync brachiosaurus-->
        <!--/docusync-->
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

    shouldSucceed("docusync") {
    }
  }
}
