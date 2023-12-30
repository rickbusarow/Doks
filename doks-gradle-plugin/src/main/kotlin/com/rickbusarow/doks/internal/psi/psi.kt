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

package com.rickbusarow.doks.internal.psi

import com.rickbusarow.doks.internal.trees.breadthFirstTraversal
import com.rickbusarow.doks.internal.trees.depthFirstTraversal
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType

internal fun PsiElement.childrenDepthFirst(): Sequence<PsiElement> {
  return depthFirstTraversal { children.toList() }
}

internal inline fun PsiElement.childrenDepthFirst(
  crossinline predicate: (PsiElement) -> Boolean
): Sequence<PsiElement> = depthFirstTraversal { children.filter(predicate) }

internal fun PsiElement.childrenBreadthFirst(): Sequence<PsiElement> {
  return breadthFirstTraversal { children.toList() }
}

internal inline fun PsiElement.childrenBreadthFirst(
  crossinline predicate: (PsiElement) -> Boolean
): Sequence<PsiElement> = breadthFirstTraversal { children.filter(predicate) }

internal inline fun <reified T : PsiElement> PsiElement.isPartOf(): Boolean =
  getNonStrictParentOfType<T>() != null
