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

package builds.curator

import com.rickbusarow.kgx.checkProjectIsRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.language.base.plugins.LifecycleBasePlugin

@Suppress("UndocumentedPublicClass")
abstract class CuratorPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    target.checkProjectIsRoot()

    target.tasks.register("curatorDump", CuratorDumpTask::class.java)
    val artifactsCheck = target.tasks.register("curatorCheck", CuratorCheckTask::class.java)

    target.plugins.apply("base")

    target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { task ->
      task.dependsOn(artifactsCheck)
    }

    target.allprojects {
      it.tasks.withType(AbstractPublishToMaven::class.java).configureEach { task ->
        task.dependsOn(artifactsCheck)
      }
    }
  }
}
