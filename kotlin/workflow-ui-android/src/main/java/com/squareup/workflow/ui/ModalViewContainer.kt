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

import android.app.Dialog
import android.content.Context
import android.support.annotation.IdRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import kotlin.reflect.KClass

/**
 * Class returned by [ModalContainer.forContainerScreen], qv for details.
 */
@ExperimentalWorkflowUi
@PublishedApi
internal class ModalViewContainer
@JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  @StyleRes private val dialogThemeResId: Int = 0,
  private val modalDecorator: (View) -> View = { it }
) : ModalContainer<Any>(context, attributeSet) {

  override fun buildDialog(
    initialValue: Any,
    viewRegistry: ViewRegistry
  ): DialogRef<Any> {
    val view = viewRegistry.buildView(initialValue, this)

    return Dialog(context, dialogThemeResId)
        .apply {
          setCancelable(false)
          setContentView(modalDecorator(view))
          window!!.setLayout(WRAP_CONTENT, WRAP_CONTENT)

          if (dialogThemeResId == 0) {
            // If we don't set or clear the background drawable, the window cannot go full bleed.
            window!!.setBackgroundDrawable(null)
          }
        }
        .run {
          DialogRef(initialValue, this, view)
        }
  }

  override fun updateDialog(dialogRef: DialogRef<Any>): DialogRef<Any> {
    return dialogRef.apply {
      (extra as View).updateRunner(value)
    }
  }

  private class Runner<H : HasModals<*, *>> : ViewRunner<H> {
    private lateinit var view: ModalViewContainer

    override fun bind(
      view: View,
      registry: ViewRegistry
    ) {
      this.view = (view as ModalViewContainer)
      this.view.registry = registry
    }

    override fun update(newValue: H) {
      view.update(newValue)
    }
  }

  class Binding<H : HasModals<*, *>>(
    @IdRes id: Int,
    type: KClass<H>,
    @StyleRes dialogThemeResId: Int = 0,
    modalDecorator: (View) -> View = { it }
  ) : ViewBinding<H>
  by BuilderBinding(
      type = type,
      builder = { viewRegistry, initialValue, context, _ ->
        ModalViewContainer(
            context,
            modalDecorator = modalDecorator,
            dialogThemeResId = dialogThemeResId
        )
            .apply {
              this.id = id
              layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
              bindRunner(viewRegistry, initialValue, Runner())
            }
      }
  )
}
