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

package com.rickbusarow.docusync

import io.kotest.assertions.throwables.shouldNotThrowAny
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class JavaSerializationTest {

  @Test
  fun `Rule serializes and deserializes without issue`() {

    val rule = Rule(
      name = "some-rule",
      regex = "(com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)",
      replacement = "foo"
    )

    shouldNotThrowAny {
      ObjectOutputStream(ByteArrayOutputStream()).writeObject(rule)
    }
  }

  @Test
  fun `RuleCache serializes and deserializes without issue`() {

    val rule = Rule(
      name = "some-rule",
      regex = "(com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)",
      replacement = "foo"
    )

    val cache = RuleCache(listOf(rule))

    shouldNotThrowAny {
      ObjectOutputStream(ByteArrayOutputStream()).writeObject(cache)
    }
  }

  @Test
  fun `DocusyncEngine serializes and deserializes without issue`() {

    val rule = Rule(
      name = "some-rule",
      regex = "(com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)",
      replacement = "foo"
    )

    val cache = RuleCache(listOf(rule))

    val outStream = ObjectOutputStream(ByteArrayOutputStream())

    shouldNotThrowAny {
      outStream.writeObject(DocusyncEngine(cache, true))
    }
  }
}
