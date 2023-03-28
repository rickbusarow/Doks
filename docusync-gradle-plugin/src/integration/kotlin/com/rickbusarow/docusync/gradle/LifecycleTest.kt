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
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test

@Suppress("FunctionName")
internal class LifecycleTest : BaseGradleTest {

  @Test
  fun `the check lifecycle task invokes docusyncCheck`() = test {

    buildFile {
      """
      plugins {
        base
        id("com.rickbusarow.docusync") version "${BuildConfig.version}"
      }

      val CURRENT_VERSION = "1.0.1"

      docusync {
        docSet {
          docs(projectDir) {
            include("**/*.md", "**/*.mdx")
          }
          rule("maven-artifact") {
            regex = maven(group = "com\\.example\\.dino")
            replacement = "${'$'}1:${'$'}2:${'$'}CURRENT_VERSION"
          }
        }
      }
      """
    }

    workingDir.resolve("README.md")
      .markdown(
        """
        <!--docusync maven-artifact:1-->
        ```kotlin
        dependencies {
          id("com.example.dino:sauropod:1.0.1")
        }
        ```
        <!--/docusync-->
        """
      )

    shouldSucceed("check") {
      task(":docusyncCheck")!!.outcome shouldBe SUCCESS
    }
  }
}
