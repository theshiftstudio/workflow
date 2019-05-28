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
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.reflect.KClass

/**
 * A [ViewBinding] built from a [layoutId] and a [runnerConstructor] function.
 * (Use [BuilderBinding] to create views from code.)
 *
 * Typical usage is to have a [ViewRunner]'s `companion object` implement
 * [ViewBinding] by delegating to one of these, tied to the layout resource
 * it typically expects to drive.
 *
 *    TBD
 *
 * This pattern allows us to assemble a [ViewRegistry] out of the
 * [ViewRunner] classes themselves.
 *
 *    val TicTacToeViewBuilders = ViewRegistry(
 *        NewGameViewRunner, GamePlayViewRunner, GameOverViewRunner
 *    )
 */
@ExperimentalWorkflowUi
class LayoutBinding<T : Any>(
  override val type: KClass<T>,
  @LayoutRes private val layoutId: Int,
  private val runnerConstructor: () -> ViewRunner<T>
) : ViewBinding<T> {
  override fun buildView(
    registry: ViewRegistry,
    initialValue: T,
    contextForNewView: Context,
    container: ViewGroup?
  ): View {
    return LayoutInflater.from(container?.context ?: contextForNewView)
        .cloneInContext(contextForNewView)
        .inflate(layoutId, container, false)
        .apply {
          bindRunner(registry, initialValue, runnerConstructor.invoke())
        }
  }

  companion object {
    inline fun <reified T : Any> of(
      @LayoutRes layoutId: Int,
      noinline runnerConstructor: () -> ViewRunner<T>
    ): LayoutBinding<T> {
      return LayoutBinding(T::class, layoutId, runnerConstructor)
    }
  }
}
