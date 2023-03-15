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

package com.rickbusarow.docusync.internal

/**
 * A simpler SEMVER which does not include any suffix like `-SNAPSHOT`, `-beta01` or `-RC`
 *
 * matches simple semantic versions like `1.0.0` or `10.52.1028`. It does not match if the version is
 * non-semantic, like `1` or `1.2`.
 */
val SEMVER_REGEX_STABLE: String = buildString {
  append("(?:0|[1-9]\\d*)\\.")
  append("(?:0|[1-9]\\d*)\\.")
  append("(?:0|[1-9]\\d*)")
}

/**
 * from here: https://ihateregex.io/expr/semver/ but no capturing groups
 */
val SEMVER_REGEX: String = buildString {
  append(SEMVER_REGEX_STABLE)
  append("(?:-(?:(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)")
  append("(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?")
  append("(?:\\+(?:[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?")
}

/**
 * shorthand for `replace(___, "")` against multiple tokens
 */
fun String.remove(vararg regex: Regex): String = regex.fold(this) { acc, reg ->
  acc.replace(reg, "")
}
