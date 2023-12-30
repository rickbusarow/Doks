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

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

const val TRIPLE_QUOTES = "\"\"\""

class NamedSamplesTest {

  @Test
  fun `can be serialized as java serializable`() {

    val namedSamples = NamedSamples(DoksPsiFileFactory())

    shouldNotThrowAny {
      ObjectOutputStream(ByteArrayOutputStream()).writeObject(namedSamples)
    }
  }

  @Test
  fun `string template property within a class and bodyOnly = true`() {
    val result = parse(
      fqName = "com.test.MyClass.groovy",
      bodyOnly = true,
      """
      package com.test

      class MyClass {
        val groovy = $TRIPLE_QUOTES
          plugins {
            id 'com.squareup.anvil' version '2.4.4'
          }

          android {
            foo()
          }

          dependencies {
            compileOnly 'javax:inject:1'
          }
        $TRIPLE_QUOTES.trimIndent()
      }
      """
    )

    result.trimIndent() shouldBe """
      plugins {
        id 'com.squareup.anvil' version '2.4.4'
      }

      android {
        foo()
      }

      dependencies {
        compileOnly 'javax:inject:1'
      }
    """.trimIndent()
  }

  @Test
  fun `string template property within a class and bodyOnly = false`() {
    val result = parse(
      fqName = "com.test.MyClass.groovy",
      bodyOnly = false,
      """
      package com.test

      class MyClass {
        val groovy = $TRIPLE_QUOTES
          plugins {
            id 'com.squareup.anvil' version '2.4.4'
          }

          android {
            foo()
          }

          dependencies {
            compileOnly 'javax:inject:1'
          }
        $TRIPLE_QUOTES.trimIndent()
      }
      """
    )

    result.trimIndent() shouldBe """
      val groovy = $TRIPLE_QUOTES
          plugins {
            id 'com.squareup.anvil' version '2.4.4'
          }

          android {
            foo()
          }

          dependencies {
            compileOnly 'javax:inject:1'
          }
        $TRIPLE_QUOTES.trimIndent()
    """.trimIndent()
  }

  @Test
  fun `top level string template property and bodyOnly = true`() {
    val result = parse(
      fqName = "com.test.groovy",
      bodyOnly = true,
      """
      package com.test

      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES
      """
    )

    result.trimIndent() shouldBe """
      plugins {
        id 'com.squareup.anvil' version '2.4.4'
      }

      android {
        foo()
      }

      dependencies {
        compileOnly 'javax:inject:1'
      }
    """.trimIndent()
  }

  @Test
  fun `top level string template property with trimIndent and bodyOnly = true`() {
    val result = parse(
      fqName = "com.test.groovy",
      bodyOnly = true,
      """
      package com.test

      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES.trimIndent()
      """
    )

    result.trimIndent() shouldBe """
      plugins {
        id 'com.squareup.anvil' version '2.4.4'
      }

      android {
        foo()
      }

      dependencies {
        compileOnly 'javax:inject:1'
      }
    """.trimIndent()
  }

  @Test
  fun `top level string template property and bodyOnly = false`() {
    val result = parse(
      fqName = "com.test.groovy",
      bodyOnly = false,
      """
      package com.test

      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES.trimIndent()
      """
    )

    result.trimIndent() shouldBe """
      val groovy = $TRIPLE_QUOTES
        plugins {
          id 'com.squareup.anvil' version '2.4.4'
        }

        android {
          foo()
        }

        dependencies {
          compileOnly 'javax:inject:1'
        }
      $TRIPLE_QUOTES.trimIndent()
    """.trimIndent()
  }

  @Test
  fun `a variable inside a nested function can be resolved`() {

    parse(
      fqName = "com.test.MyClass.foo.bar.variable",
      bodyOnly = true,
      """
      package com.test

      class MyClass {
        fun foo() {
          fun bar() {
            val variable = "a string"
          }
        }
      }
      """
    ) shouldBe "a string"
  }

  @Test
  fun `a function with backticks can be resolved`() {

    parse(
      fqName = "com.test.MyClass.`a function`",
      bodyOnly = true,
      """
      package com.test

      class MyClass {
        fun `a function`() {
          val variable = "a string"
        }
      }
      """
    ) shouldBe "val variable = \"a string\""
  }

  @Test
  fun `a property inside a function with backticks can be resolved`() {

    parse(
      fqName = "com.test.MyClass.`a function`.variable",
      bodyOnly = true,
      """
      package com.test

      class MyClass {
        fun `a function`() {
          val variable = "a string"
        }
      }
      """
    ) shouldBe "a string"
  }

  @Test
  fun `a variable inside an expression function lambda can be resolved`() {

    parse(
      fqName = "com.test.MyClass.foo.variable",
      bodyOnly = true,
      """
      package com.test

      class MyClass {
        fun foo() = test {
          val variable = "a string"
        }
      }
      """
    ) shouldBe "a string"
  }

  @Test
  fun `a member function without bodyOnly has its indentation preserved`() {

    parse(
      fqName = "com.test.MyClass.foo",
      bodyOnly = false,
      """
      package com.test

      class MyClass {
        fun foo() {
          val variable = "a string"
        }
      }
      """
    ) shouldBe """
      |fun foo() {
      |  val variable = "a string"
      |}
    """.trimMargin()
  }

  @Test
  fun `a nested class without bodyOnly has its indentation preserved`() {

    parse(
      fqName = "com.test.Outer.Inner",
      bodyOnly = false,
      """
      package com.test

      class Outer {
        class Inner {
          val variable = "a string"
        }
      }
      """
    ) shouldBe """
      |class Inner {
      |  val variable = "a string"
      |}
    """.trimMargin()
  }

  @Test
  fun `an object can be resolved`() {

    parse(
      fqName = "com.test.Foo",
      bodyOnly = false,
      """
      package com.test

      object Foo
      """
    ) shouldBe """
      |object Foo
      |
    """.trimMargin()
  }

  fun parse(fqName: String, bodyOnly: Boolean, @Language("kotlin") vararg content: String): String {
    val files = content.mapIndexed { index, code ->
      DoksPsiFileFactory()
        .createKotlin(
          name = "Source_$index.kt",
          path = "Source_$index.kt",
          content = code.trimIndent()
        )
    }

    return NamedSamples(DoksPsiFileFactory())
      .findAll(
        ktFiles = files.asSequence(),
        requests = listOf(SampleRequest(fqName, bodyOnly))
      )
      .single()
      .content
  }
}
