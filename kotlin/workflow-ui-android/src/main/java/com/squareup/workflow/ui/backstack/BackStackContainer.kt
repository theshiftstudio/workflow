/*
 * Copyright 2018 Square Inc.
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
package com.squareup.workflow.ui.backstack

import android.content.Context
import android.os.Parcelable
import android.support.transition.Fade
import android.support.transition.Scene
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.squareup.workflow.ui.BackStackScreen
import com.squareup.workflow.ui.ExperimentalWorkflowUi
import com.squareup.workflow.ui.HandlesBack
import com.squareup.workflow.ui.R
import com.squareup.workflow.ui.backstack.ViewStateStack.Direction.PUSH
import com.squareup.workflow.ui.backstack.ViewStateStack.SavedState
import com.squareup.workflow.ui.BuilderBinding
import com.squareup.workflow.ui.ViewBinding
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.ViewRunner
import com.squareup.workflow.ui.bindRunner
import com.squareup.workflow.ui.hasRunnerFor
import com.squareup.workflow.ui.updateRunner

/**
 * A container view that can display a stream of [BackStackScreen] instances.
 *
 * This view is back button friendly -- it implements [HandlesBack], delegating
 * to displayed views that implement that interface themselves.
 */
@ExperimentalWorkflowUi
class BackStackContainer(
  context: Context,
  attributeSet: AttributeSet?
) : FrameLayout(context, attributeSet), HandlesBack {
  constructor(context: Context) : this(context, null)

  private var restored: ViewStateStack? = null
  private val viewStateStack by lazy { restored ?: ViewStateStack() }

  private val showing: View? get() = if (childCount > 0) getChildAt(0) else null

  private lateinit var registry: ViewRegistry

  private fun update(newValue: BackStackScreen<*>) {
    // Existing view is of the right type, just update it.
    showing
        ?.takeIf { it.hasRunnerFor(newValue.wrapped) }
        ?.updateRunner(newValue.wrapped)
        ?.also { return }

    val updateTools = viewStateStack.prepareToUpdate(newValue.key)
    val newView = registry.buildView(newValue.wrapped, this)
        .apply { updateTools.setUpNewView(this) }

    // Showing something already, transition with push or pop effect.
    showing
        ?.let { oldView ->
          updateTools.saveOldView(oldView)

          val newScene = Scene(this, newView)

          val (outEdge, inEdge) = when (PUSH) {
            updateTools.direction -> Pair(Gravity.START, Gravity.END)
            else -> Pair(Gravity.END, Gravity.START)
          }

          val outSet = TransitionSet()
              .addTransition(Slide(outEdge).addTarget(oldView))
              .addTransition(Fade(Fade.OUT))

          val fullSet = TransitionSet()
              .addTransition(outSet)
              .addTransition(Slide(inEdge).excludeTarget(oldView, true))

          TransitionManager.go(newScene, fullSet)
        }
        ?.also { return }

    // This is the first view, just show it.
    addView(newView)
  }

  override fun onBackPressed(): Boolean {
    return showing
        ?.let { HandlesBack.Helper.onBackPressed(it) }
        ?: false
  }

  override fun onSaveInstanceState(): Parcelable {
    showing?.let { viewStateStack.save(it) }
    return SavedState(super.onSaveInstanceState(), viewStateStack)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    (state as? SavedState)
        ?.let {
          restored = it.viewStateStack
          super.onRestoreInstanceState(state.superState)
        }
        ?: super.onRestoreInstanceState(state)
  }

  private class Runner : ViewRunner<BackStackScreen<*>> {
    private lateinit var view: BackStackContainer

    override fun bind(
      view: View,
      registry: ViewRegistry
    ) {
      this.view = (view as BackStackContainer)
      this.view.registry = registry
    }

    override fun update(newValue: BackStackScreen<*>) {
      view.update(newValue)
    }
  }

  companion object : ViewBinding<BackStackScreen<*>>
  by BuilderBinding(
      type = BackStackScreen::class,
      builder = { viewRegistry, initialValue, context, _ ->
        BackStackContainer(context)
            .apply {
              id = R.id.workflow_back_stack_container
              layoutParams = (ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
              bindRunner(viewRegistry, initialValue, Runner())
            }
      }
  )
}
