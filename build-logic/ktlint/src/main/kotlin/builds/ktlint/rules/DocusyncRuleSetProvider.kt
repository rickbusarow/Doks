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

@file:Suppress("stuff")

package builds.ktlint.rules

import com.google.auto.service.AutoService
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2

@AutoService(RuleSetProviderV2::class)
class DocusyncRuleSetProvider : RuleSetProviderV2(
  id = "build-logic",
  about = NO_ABOUT
) {

  override fun getRuleProviders(): Set<RuleProvider> {
    return setOf(
      RuleProvider { KDocWrappingRule() },
      RuleProvider { NoDuplicateCopyrightHeaderRule() },
      RuleProvider { NoLeadingBlankLinesRule() },
      RuleProvider { NoSinceInKDocRule(BuildConfig.currentVersion) },
      RuleProvider { NoSpaceInTargetedAnnotationRule() },
      RuleProvider { NoTrailingSpacesInRawStringLiteralRule() },
      RuleProvider { NoUselessConstructorKeywordRule() }
    )
  }
}
