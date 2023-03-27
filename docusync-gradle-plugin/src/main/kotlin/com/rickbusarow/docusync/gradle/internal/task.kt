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

package com.rickbusarow.docusync.gradle.internal

import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

/** adds all [objects] as dependencies inside a configuration block, inside a `configure { }` */
fun <T : Task> TaskProvider<T>.dependsOn(vararg objects: Any): TaskProvider<T> {
  return also { provider ->
    provider.configure { task ->
      task.dependsOn(*objects)
    }
  }
}

/**
 * Returns a collection containing the objects in this collection of the given type. Equivalent to
 * calling `withType(type).all(configureAction)`.
 *
 * @param S The type of objects to find.
 * @param configuration The action to execute for each object in the resulting collection.
 * @return The matching objects. Returns an empty collection if there are no such objects in this
 *     collection.
 * @see [DomainObjectCollection.withType]
 */
inline fun <reified S : Any> DomainObjectCollection<in S>.withType(
  noinline configuration: (S) -> Unit
): DomainObjectCollection<S>? = withType(S::class.java, configuration)

/**
 * Returns a collection containing the objects in this collection of the given type. The returned
 * collection is live, so that when matching objects are later added to this collection, they are also
 * visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection if there are no such objects in this
 *     collection.
 * @see [DomainObjectCollection.withType]
 */
inline fun <reified S : Any> DomainObjectCollection<in S>.withType(): DomainObjectCollection<S> =
  withType(S::class.java)

/**
 * Returns a collection containing the objects in this collection of the given type. The returned
 * collection is live, so that when matching objects are later added to this collection, they are also
 * visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection if there are no such objects in this
 *     collection.
 * @see [TaskCollection.withType]
 */
inline fun <reified S : Task> TaskCollection<in S>.withType(): TaskCollection<S> =
  withType(S::class.java)

internal inline fun <reified T : Task> TaskContainer.register(
  name: String,
  vararg constructorArguments: Any,
  noinline configuration: (T) -> Unit
): TaskProvider<T> = register(name, T::class.java, *constructorArguments)
  .apply { configure { configuration(it) } }

@JvmName("registerOnceInline")
internal inline fun <reified T : Task> TaskContainer.registerOnce(
  name: String,
  configurationAction: Action<in T>
): TaskProvider<T> = registerOnce(name, T::class.java, configurationAction)

internal inline fun <reified T : Task> TaskContainer.registerOnce(
  name: String
): TaskProvider<T> = if (names.contains(name)) {
  named(name, T::class.java)
} else {
  register(name, T::class.java)
}

internal fun <T : Task> TaskContainer.registerOnce(
  name: String,
  type: Class<T>,
  configurationAction: Action<in T>
): TaskProvider<T> = if (names.contains(name)) {
  named(name, type, configurationAction)
} else {
  register(name, type, configurationAction)
}
