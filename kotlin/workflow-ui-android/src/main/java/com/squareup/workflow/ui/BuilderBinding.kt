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
 * A [ViewBinding] for [View]s that need to be generated from code.
 * (Use [LayoutBinding] to work with XML layout resources.)
 *
 * Typical usage is to have a custom builder or view's `companion object` implement
 * [ViewBinding] by delegating to one of these:
 *
 *    TBD
 *
 * This pattern allows us to assemble a [ViewRegistry] out of the
 * custom classes themselves.
 *
 *    val TicTacToeViewBuilders = ViewRegistry(
 *        MyView, GamePlayViewRunner, GameOverViewRunner
 *    )
 */
@ExperimentalWorkflowUi
class BuilderBinding<T : Any>(
  override val type: KClass<T>,
  private val builder: (
    registry: ViewRegistry,
    initialValue: T,
    contextForNewView: Context,
    container: ViewGroup?
  ) -> View
) : ViewBinding<T> {
  override fun buildView(
    registry: ViewRegistry,
    initialValue: T,
    contextForNewView: Context,
    container: ViewGroup?
  ): View = builder.invoke(registry, initialValue, contextForNewView, container)
}
