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

import com.rickbusarow.doks.internal.psi.LazyMap.Companion.toLazyMap
import com.rickbusarow.doks.internal.stdlib.joinToStringConcat
import com.rickbusarow.doks.internal.stdlib.requireNotNull
import com.rickbusarow.doks.internal.stdlib.trimIndentAfterFirstLine
import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiModifiableCodeBlock
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.parentOrNull
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclarationContainer
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import java.io.File
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.io.path.Path

@Serializable
internal data class SampleRequest(
  val fqName: String,
  val bodyOnly: Boolean
) : java.io.Serializable

@Serializable
internal data class SampleResult(
  val request: SampleRequest,
  val content: String
) : java.io.Serializable

internal class NamedSamples(
  private val psiFactory: DoksPsiFileFactory
) : java.io.Serializable {

  private val nameCache = LazyMap<KtNamedDeclaration, FqName> { psi ->

    val fqName = psi.fqName

    val identifierText = psi.identifyingElement?.text
      ?: psi.name.requireNotNull {
        """
        |This psi element does not have a name: $psi
        |================
        |${psi.text}
        |================
        """.replaceIndentByMargin()
      }

    val identifierName by lazy(NONE) { Name.identifier(identifierText) }

    if (fqName != null) {
      return@LazyMap when {
        // An object's identifier is the "object" keyword, but named objects can't be declared inside a
        // function, so they don't need the identifier workaround.
        psi is KtObjectDeclaration -> fqName
        // If the identifier's text is different from the parsed `name`, it's probably because Kotlin
        // removed backticks wrapping the name. In this case, we want to retain the backticks.
        identifierText != psi.name -> fqName.parentOrNull()?.child(identifierName) ?: fqName
        else -> fqName
      }
    }

    val parentFqName = psi.parents
      .filterIsInstance<KtNamedDeclaration>()
      .first { it !is KtFunctionLiteral }
      .fqNameIncludingMembers()

    parentFqName.child(identifierName)
  }

  fun findAll(files: Collection<File>, requests: List<SampleRequest>): List<SampleResult> {
    val ktFiles = files
      .asSequence()
      .map { psiFactory.createKotlin(it) }

    return findAll(
      ktFiles = ktFiles,
      requests = requests
    )
  }

  fun findAll(ktFiles: Sequence<KtFile>, requests: List<SampleRequest>): List<SampleResult> {

    val cache = createDeclarationCache(ktFiles, requests)

    return requests.map { request ->

      val content =
        when (val namedDeclaration = cache[request.fqName]) {
          is KtClassOrObject -> if (request.bodyOnly) {
            namedDeclaration.body!!.textInScope()
          } else {
            namedDeclaration.text.trimIndentAfterFirstLine()
          }

          is KtProperty -> if (request.bodyOnly) {
            namedDeclaration.initializer
              .requireNotNull {
                "${namedDeclaration.containingKtFile} > " +
                  "A property must have an initializer when using 'bodyOnly = true'."
              }
              .let { (it as? KtStringTemplateExpression) ?: it.getChildOfType() }
              .requireNotNull {
                "${namedDeclaration.containingKtFile} > " +
                  "A property initializer must be a string template."
              }
              .textInScope()
          } else {
            namedDeclaration.text
          }

          is KtNamedFunction -> if (request.bodyOnly) {
            namedDeclaration.bodyBlockExpression!!.textInScope()
          } else {
            namedDeclaration.text.trimIndentAfterFirstLine()
          }

          null -> {
            error("could not find a psi element with the name of `${request.fqName}`")
          }

          else -> error("Unsupported psi element -- ${namedDeclaration.text}")
        }

      SampleResult(request, content)
    }
  }

  private fun createDeclarationCache(
    ktFiles: Sequence<KtFile>,
    requests: List<SampleRequest>
  ): LazyMap<String?, KtNamedDeclaration?> {

    val requestedNames = requests.map { FqName(it.fqName) }

    val namesAndParentNames = requestedNames
      .flatMap { requestedName ->
        generateSequence(requestedName) { if (it.isRoot) null else it.parent() }
      }
      .toSet()

    return ktFiles
      .sortByPathSimilarityTo(requestedNames)
      .flatMap<KtFile, KtNamedDeclaration> { ktFile ->

        ktFile
          .getChildrenOfTypeRecursive { element ->

            when (element) {
              is KtFunctionLiteral -> true

              is KtNamedDeclaration -> element.fqNameIncludingMembers() in namesAndParentNames

              else -> element.couldHaveNamedChildren()
            }
          }
      }
      .distinct()
      .map { it.fqNameIncludingMembers().asString() to it }
      .toLazyMap()
  }

  private fun Sequence<KtFile>.sortByPathSimilarityTo(fqNames: List<FqName>): Sequence<KtFile> {

    val fqNamePaths = fqNames.map { Path(it.asPathString()) }

    return map { file ->

      val filePath = Path(file.absolutePath())
      var score = 0

      fqNamePaths.forEach { fqNamePath ->
        var matchingSegments = 0

        while (matchingSegments < fqNamePath.nameCount && matchingSegments < filePath.nameCount &&
          fqNamePath.getName(matchingSegments) == filePath.getName(matchingSegments)
        ) {
          matchingSegments++
        }

        score += matchingSegments
      }

      Pair(file, score)
    }
      .sortedByDescending { it.second }
      .map { it.first }
  }

  private fun FqName.asPathString(): String = pathSegments().joinToString(File.separator)

  private fun KtElement.textInScope() = getChildrenOfType<PsiElement>()
    .drop(1)
    .dropLast(1)
    .joinToStringConcat { it.text }
    .trimIndent()

  private fun KtNamedDeclaration.fqNameIncludingMembers(): FqName = nameCache[this]
}

internal fun PsiElement.couldHaveNamedChildren(): Boolean {
  return when (this) {
    is LeafPsiElement -> false

    is KtFunctionLiteral,
    is KtDeclarationWithBody,
    is KtCallExpression,
    is KtDeclarationContainer,
    is KtLambdaArgument,
    is PsiModifiableCodeBlock -> true

    else -> false
  }
}
