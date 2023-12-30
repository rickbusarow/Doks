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

package com.rickbusarow.doks.internal.gradle

import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

/**
 * adds all [objects] as dependencies inside a configuration block, inside a `configure { }`
 *
 * @since 0.1.0
 */
internal fun <T : Task> TaskProvider<T>.dependsOn(vararg objects: Any): TaskProvider<T> {
  return also { provider ->
    provider.configure { task ->
      task.dependsOn(*objects)
    }
  }
}

/**
 * adds all [objects] as dependencies to every task in the collection, inside a `configureEach { }`
 *
 * @since 0.1.1
 */
internal fun <T : Task> TaskCollection<T>.dependOn(vararg objects: Any): TaskCollection<T> {
  return also { taskCollection ->
    taskCollection.configureEach { task -> task.dependsOn(*objects) }
  }
}

/**
 * adds all [objects] as `mustRunAfter` inside a configuration block, inside a `configure { }`
 *
 * @since 0.1.1
 */
internal fun <T : Task> TaskProvider<T>.mustRunAfter(vararg objects: Any): TaskProvider<T> {
  return also { provider ->
    provider.configure { task ->
      task.mustRunAfter(*objects)
    }
  }
}

/**
 * code golf for `matching { it.name == taskName }`
 *
 * @since 0.1.1
 */
internal fun TaskContainer.matchingName(taskName: String): TaskCollection<Task> =
  matching { it.name == taskName }

/**
 * Returns a collection containing the objects in this collection of the
 * given type. Equivalent to calling `withType(type).all(configureAction)`.
 *
 * @param S The type of objects to find.
 * @param configuration The action to execute for each object in the resulting collection.
 * @return The matching objects. Returns an empty collection
 *   if there are no such objects in this collection.
 * @see [DomainObjectCollection.withType]
 * @since 0.1.0
 */
internal inline fun <reified S : Any> DomainObjectCollection<in S>.withType(
  noinline configuration: (S) -> Unit
): DomainObjectCollection<S>? = withType(S::class.java, configuration)

/**
 * Returns a collection containing the objects in this collection of the given
 * type. The returned collection is live, so that when matching objects are later
 * added to this collection, they are also visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection
 *   if there are no such objects in this collection.
 * @see [DomainObjectCollection.withType]
 * @since 0.1.0
 */
internal inline fun <reified S : Any> DomainObjectCollection<in S>.withType(): DomainObjectCollection<S> =
  withType(S::class.java)

/**
 * Returns a collection containing the objects in this collection of the given
 * type. The returned collection is live, so that when matching objects are later
 * added to this collection, they are also visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection
 *   if there are no such objects in this collection.
 * @see [TaskCollection.withType]
 * @since 0.1.0
 */
internal inline fun <reified S : Task> TaskCollection<in S>.withType(): TaskCollection<S> =
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

internal inline fun <reified T : Task> TaskContainer.registerOnce(name: String): TaskProvider<T> =
  if (names.contains(name)) {
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

internal fun <T> NamedDomainObjectContainer<T>.registerOnce(
  name: String,
  configurationAction: Action<in T>
): NamedDomainObjectProvider<T> = if (names.contains(name)) {
  named(name, configurationAction)
} else {
  register(name, configurationAction)
}
