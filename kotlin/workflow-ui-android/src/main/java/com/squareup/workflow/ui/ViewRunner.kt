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

import android.view.View
import kotlin.reflect.KClass

/**
 * Implemented by objects that drive views to render values of type [T].
 * Use [LayoutBinding] to pair a [ViewRunner] with a layout resource
 * to be inflated via [ViewRegistry.buildView].
 */
@ExperimentalWorkflowUi
interface ViewRunner<in T : Any> {
  fun bind(
    view: View,
    registry: ViewRegistry
  )

  fun update(newValue: T)
}

@ExperimentalWorkflowUi
internal data class ViewRunnerTag<T : Any>(
  val runner: ViewRunner<T>,
  val type: KClass<T>
)

@ExperimentalWorkflowUi
fun <T : Any> View.bindRunner(
  registry: ViewRegistry,
  initialValue: T,
  runner: ViewRunner<T>
) {
  setTag(R.id.view_runner, ViewRunnerTag(runner, initialValue::class))
  runner.bind(this, registry)
  runner.update(initialValue)
}

@ExperimentalWorkflowUi
fun <T : Any> View.hasRunnerFor(value: T): Boolean {
  return viewRunnerTag?.type?.isInstance(value) == true
}

@ExperimentalWorkflowUi
fun <T : Any> View.updateRunner(newValue: T) {
  viewRunnerTag
      ?.apply {
        check(type.isInstance(newValue)) {
          "Expected instance of ${type.qualifiedName} got $newValue"
        }

        @Suppress("UNCHECKED_CAST")
        (runner as ViewRunner<T>).update(newValue)
      }
      ?: throw IllegalStateException("Runner not found on $this.")
}

@ExperimentalWorkflowUi
private val View.viewRunnerTag: ViewRunnerTag<*>?
  get() = getTag(R.id.view_runner) as? ViewRunnerTag<*>
