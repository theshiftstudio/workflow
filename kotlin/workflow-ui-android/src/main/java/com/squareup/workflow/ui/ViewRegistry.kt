/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.workflow.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import kotlin.reflect.KClass

/**
 * A collection of [ViewBinding]s that can be used to render
 * the stream of screen models emitted by a workflow (via [ViewBinding]).
 *
 * Two concrete [ViewBinding] implementations are provided:
 *
 *  - [LayoutBinding], allowing the easy pairing of Android XML layout resources with
 *    [ViewRunner]s to drive them.
 *
 *  - [BuilderBinding], which can build views from code.
 *
 *  Registries can be assembled via concatenation, making it easy to snap together screen sets.
 *  For example:
 *
 *     val AuthViewBindings = ViewRegistry(
 *         AuthorizingViewRunner, LoginViewRunner, SecondFactorViewRunner
 *     )
 *
 *     val TicTacToeViewBindings = ViewRegistry(
 *         NewGameViewRunner, GamePlayViewRunner, GameOverViewRunner
 *     )
 *
 *     val ApplicationViewBindings = ViewRegistry(ApplicationViewRunner) +
 *         AuthViewBindings + TicTacToeViewBindings
 *
 * In the above example, note that the `companion object`s of the various [ViewRunner] classes
 * honor a convention of implementing [ViewBinding], in aid of this kind of assembly. See the
 * class doc on [LayoutBinding] for details.
 */
@ExperimentalWorkflowUi
class ViewRegistry private constructor(
  private val bindings: Map<KClass<*>, ViewBinding<*>>
) {
  constructor(vararg bindings: ViewBinding<*>) : this(
      bindings.map { it.type to it }.toMap().apply {
        check(keys.size == bindings.size) {
          "${bindings.map { it.type }} must not have duplicate entries."
        }
      }
  )

  constructor(vararg registries: ViewRegistry) : this(
      registries.map { it.bindings }
          .reduce { left, right ->
            val duplicateKeys = left.keys.intersect(right.keys)
            check(duplicateKeys.isEmpty()) { "Must not have duplicate entries: $duplicateKeys." }
            left + right
          }
  )

  fun <T : Any> buildView(
    initialValue: T,
    contextForNewView: Context,
    container: ViewGroup? = null
  ): View {
    @Suppress("UNCHECKED_CAST")
    return (bindings[initialValue::class] as? ViewBinding<T>)
        ?.buildView(this, initialValue, contextForNewView, container)
        ?: throw IllegalArgumentException(
            "No view binding found for $initialValue (${initialValue::class.simpleName})"
        )
  }

  fun <T : Any> buildView(
    initialValue: T,
    container: ViewGroup
  ): View {
    return buildView(initialValue, container.context, container)
  }

  operator fun <T : Any> plus(binding: ViewBinding<T>): ViewRegistry {
    check(binding.type !in bindings.keys) {
      "Already registered ${bindings[binding.type]} for ${binding.type}, cannot accept $binding."
    }
    return ViewRegistry(bindings + (binding.type to binding))
  }

  operator fun plus(registry: ViewRegistry): ViewRegistry {
    return ViewRegistry(this, registry)
  }
}
