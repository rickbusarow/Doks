/*
 * Copyright (C) 2024 Rick Busarow
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
import com.rickbusarow.kase.gradle.DslLanguage.GroovyDsl
import com.rickbusarow.kase.gradle.GradleProjectBuilder
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.TestFactory
import java.io.File

@Suppress("FunctionName")
internal class ConfigTest : DoksGradleTest() {

  @TestFactory
  fun `groovy dsl config simple`() = testFactory(GroovyDsl()) {

    //language=kts
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

    rootProject.buildFileAsFile.createSafely(
      // Groovy requires that the variable is declared before it is referenced,
      // so we have to cheat a little and insert it into the middle of the config.
      config.replace(
        "doks {",
        """
        def CURRENT_VERSION = "1.0.1"

        doks {
        """.trimIndent()
      )
    )

    val readme = rootProject.readme(sauropodVersion("1.0.0"))

    shouldSucceed("doks")

    readme shouldExistWithText sauropodVersion("1.0.1")
  }

  @TestFactory
  fun `kotlin dsl config simple`() = testFactory {

    //language=kts
    val config = """
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

    rootProject.buildFile(
      """
        |$config
        |
        |val CURRENT_VERSION = "1.0.1"
      """.trimMargin()
    )

    val readme = rootProject.readme(sauropodVersion("1.0.0"))

    shouldSucceed("doks")

    readme shouldExistWithText sauropodVersion("1.0.1")
  }

  @TestFactory
  fun `a default docs collection picks up markdown files from the project root `() = testFactory {

    rootProject.buildFile(
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

    val readme = rootProject.file("README.md", sauropodVersion("1.0.0"))

    shouldSucceed("doks")

    readme shouldExistWithText sauropodVersion("1.0.1")
  }

  @TestFactory
  fun `groovy dsl config code`() = testFactory(GroovyDsl()) {

    val config =
      """
      doks {
        // Define a set of documents with rules.
        dokSet {
          // Set the files which will be synced
          docs(projectDir) {
            include '**/*.md', '**/*.mdx'
          }

          sampleCodeSource 'src/main/kotlin/com/dino/samples'

          // Define a rule used in updating.
          // This rule's name corresponds to the name used in documentation.
          rule('brachiosaurus') {
            replacement = sourceCode(
              "com.dino.samples.BrachiosaurusSample",
              false,
              "kotlin"
            )
          }
        }
      }
      """.trimIndent()

    rootProject {

      buildFileAsFile.appendText("\n$config\n")

      readme(emptyDoksBlock(ruleId = "brachiosaurus", count = 1))

      dir("src/main/kotlin") {
        dir("com/dino/samples") {
          file(
            "BrachiosaurusSample.kt",
            """
            package com.dino.samples

            class BrachiosaurusSample : Dinosaur {

              override fun stomp() { /* stomp stomp stomp */ }
            }
            """.trimIndent()
          )
        }
      }
    }

    shouldSucceed("doks")

    rootProject.readme shouldExistWithText """
      <!--doks brachiosaurus:1-->

      ```kotlin
      class BrachiosaurusSample : Dinosaur {
        override fun stomp() { /* stomp stomp stomp */ }
      }
      ```

      <!--doks END-->
    """.trimIndent()
  }

  @TestFactory
  fun `kotlin dsl config code`() = testFactory {

    //language=kts
    val config = """
      doks {
        // Define a set of documents with rules.
        dokSet {
          docs(projectDir) {
            include("**/*.md", "**/*.mdx")
          }

          sampleCodeSource("src/main/kotlin/com/dino/samples")

          rule("brachiosaurus") {
            replacement = sourceCode(
              fqName = "com.dino.samples.BrachiosaurusSample",
              bodyOnly = false,
              codeBlockLanguage = "kotlin"
            )
          }
        }
      }
    """.trimIndent()

    //language=kotlin
    val dinoPluginSampleContent = """
      package com.dino.samples

      class BrachiosaurusSample : Dinosaur {

        override fun stomp() { /* stomp stomp stomp */ }
      }
    """.trimIndent()

    rootProject {
      buildFileAsFile.appendText("\n$config\n")

      readme(emptyDoksBlock(ruleId = "brachiosaurus", count = 1))

      dir("src/main/kotlin") {
        dir("com/dino/samples") {
          file("BrachiosaurusSample.kt", dinoPluginSampleContent)
        }
      }
    }

    shouldSucceed("doks")

    rootProject.readme shouldExistWithText """
      <!--doks brachiosaurus:1-->

      ```kotlin
      class BrachiosaurusSample : Dinosaur {
        override fun stomp() { /* stomp stomp stomp */ }
      }
      ```

      <!--doks END-->
    """.trimIndent()
  }

  @TestFactory
  fun `a default samples collection picks up kotlin files in src`() = testFactory {

    rootProject {
      buildFileAsFile.appendText(
        """
        doks {
          dokSet {
            docs(projectDir) {
              include("**/*.md", "**/*.mdx")
            }
            rule("brachiosaurus") {
              replacement = sourceCode(
                fqName = "com.dino.samples.BrachiosaurusSample",
                bodyOnly = false,
                codeBlockLanguage = "kotlin"
              )
            }
          }
        }
        """.trimIndent()
      )

      readme(emptyDoksBlock(ruleId = "brachiosaurus"))

      dir("src/test/kotlin") {
        dir("com/dino/samples") {
          file(
            "BrachiosaurusSample.kt",
            """
            package com.dino.samples

            class BrachiosaurusSample : Dinosaur {

              override fun stomp() { /* stomp stomp stomp */ }
            }
            """.trimIndent()
          )
        }
      }

      shouldSucceed("doks")

      readme.readText() shouldBe """
      <!--doks brachiosaurus-->

      ```kotlin
      class BrachiosaurusSample : Dinosaur {
        override fun stomp() { /* stomp stomp stomp */ }
      }
      ```

      <!--doks END-->
      """.trimIndent()
    }
  }

  val GradleProjectBuilder.readme: File
    get() = path.resolve("README.md")

  fun GradleProjectBuilder.readme(@Language("markdown") content: String): File {
    return file("README.md", content)
  }

  fun emptyDoksBlock(ruleId: String): String = """
    <!--doks $ruleId-->
    <!--doks END-->
  """.trimIndent()

  fun emptyDoksBlock(ruleId: String, count: Int): String = """
    <!--doks $ruleId:$count-->
    <!--doks END-->
  """.trimIndent()

  fun contentDoksBlock(ruleId: String, count: Int, content: String): String = """
    |<!--doks $ruleId:$count-->
    |$content
    |<!--doks END-->
  """.trimMargin()

  fun contentDoksBlock(ruleId: String, content: String): String =
    contentDoksBlock(ruleId = ruleId, count = 1, content = content)

  fun sauropodVersion(version: String): String = contentDoksBlock(
    ruleId = "maven-artifact",
    count = 1,
    content = """
      ```kotlin
      dependencies {
        implementation("com.example.dino:sauropod:$version")
      }
      ```
    """.trimIndent()
  )
}
