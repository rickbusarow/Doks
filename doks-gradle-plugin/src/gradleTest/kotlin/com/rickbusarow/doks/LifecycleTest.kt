/*
 * Copyright (C) 2025 Rick Busarow
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
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome.SKIPPED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.TestFactory

@Suppress("FunctionName")
internal class LifecycleTest : DoksGradleTest() {

  @TestFactory
  fun `the check lifecycle task invokes doksCheck`() = testFactory {

    rootProject {
      buildFile(
        """
        plugins {
          base
          id("com.rickbusarow.doks") version "$doksVersion"
        }

        val CURRENT_VERSION = "1.0.1"

        doks {
          dokSet {
            docs(projectDir) {
              include("**/*.md", "**/*.mdx")
            }
            rule("maven-artifact") {
              regex = maven(group = "com\\.example\\.dino")
              replacement = "$1:$2:${'$'}CURRENT_VERSION"
            }
          }
        }
        """
      )

      path.resolve("README.md").createSafely(
        """
        <!--doks maven-artifact:1-->
        ```kotlin
        dependencies {
          implementation("com.example.dino:sauropod:1.0.1")
        }
        ```
        <!--/doks-->
        """
      )
    }

    shouldSucceed("check") {
      task(":doksCheckAll")!!.outcome shouldBe SUCCESS
      task(":doksCheck")!!.outcome shouldBe SKIPPED
    }
  }

  @TestFactory
  fun `the fix lifecycle task invokes doks`() = testFactory {

    rootProject {
      buildFile(
        """
      plugins {
        base
        id("com.rickbusarow.doks") version "$doksVersion"
      }

      val CURRENT_VERSION = "1.0.1"

      doks {
        dokSet {
          docs(projectDir) {
            include("**/*.md", "**/*.mdx")
          }
          rule("maven-artifact") {
            regex = maven(group = "com\\.example\\.dino")
            replacement = "${'$'}1:${'$'}2:${'$'}CURRENT_VERSION"
          }
        }
      }

      val fix by tasks.registering
      """
      )

      path.resolve("README.md").createSafely(
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
    }
    shouldSucceed("fix") {
      task(":doksAll")!!.outcome shouldBe SUCCESS
      task(":doks")!!.outcome shouldBe SKIPPED
    }
  }

  @TestFactory
  fun `the doksCheck must run after doks`() = testFactory {

    rootProject {
      buildFile(
        """
        plugins {
          base
          id("com.rickbusarow.doks") version "$doksVersion"
        }

        val CURRENT_VERSION = "1.0.1"

        doks {
          dokSet {
            docs(projectDir) {
              include("**/*.md", "**/*.mdx")
            }
            rule("maven-artifact") {
              regex = maven(group = "com\\.example\\.dino")
              replacement = "${'$'}1:${'$'}2:${'$'}CURRENT_VERSION"
            }
          }
        }

        val fix by tasks.registering
        """
      )

      path.resolve("README.md")
        .createSafely(
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
    }

    shouldSucceed("doksCheck", "doks") {
      tasks.map { it.path } shouldContainInOrder listOf(":doksAll", ":doksCheckAll")
    }
  }
}
