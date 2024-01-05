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

package builds

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.toYaml
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class ShartifyPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    target.tasks.register("shart") {

      it.doLast {
        val testYml = target.rootDir.resolve(".github/workflows/test.yml")

        val wf = workflow(
          name = "Test workflow",
          on = listOf(Push()),
          sourceFile = testYml.toPath()
        ) {

          val testJob = job(id = "test_job", runsOn = Ubuntu_4_16) {

            uses(name = "Check out", action = CheckoutV4())
            run(name = "Print greeting", command = "echo 'Hello world!'")
          }
        }

        val yml = wf.toYaml(addConsistencyCheck = false)

        testYml.writeText(yml)

        println("##################\n$yml\n##################")
      }
    }
  }

  val Ubuntu_4_16 = RunnerType.Custom("ubuntu-4-16")
}
