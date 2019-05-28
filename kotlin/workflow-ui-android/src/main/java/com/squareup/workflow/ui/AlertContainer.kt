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
import android.content.DialogInterface
import android.support.annotation.StyleRes
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.squareup.workflow.ui.AlertScreen.Button
import com.squareup.workflow.ui.AlertScreen.Button.NEGATIVE
import com.squareup.workflow.ui.AlertScreen.Button.NEUTRAL
import com.squareup.workflow.ui.AlertScreen.Button.POSITIVE
import com.squareup.workflow.ui.AlertScreen.Event.ButtonClicked
import com.squareup.workflow.ui.AlertScreen.Event.Canceled

/**
 * Class returned by [ModalContainer.forAlertContainerScreen], qv for details.
 */
@ExperimentalWorkflowUi
internal class AlertContainer
@JvmOverloads constructor(
  context: Context,
  attributeSet: AttributeSet? = null,
  @StyleRes private val dialogThemeResId: Int = 0
) : ModalContainer<AlertScreen>(context, attributeSet) {

  override fun buildDialog(
    initialValue: AlertScreen,
    viewRegistry: ViewRegistry
  ): DialogRef<AlertScreen> {
    return AlertDialog.Builder(context, dialogThemeResId)
        .create()
        .run {
          updateDialog(DialogRef(initialValue, this))
        }
  }

  override fun updateDialog(dialogRef: DialogRef<AlertScreen>): DialogRef<AlertScreen> {
    val dialog = dialogRef.dialog as AlertDialog
    val value = dialogRef.value

    if (value.cancelable) {
      dialog.setOnCancelListener { value.onEvent(Canceled) }
      dialog.setCancelable(true)
    } else {
      dialog.setCancelable(false)
    }

    for (button in Button.values()) {
      value.buttons[button]
          ?.let { name ->
            dialog.setButton(button.toId(), name) { _, _ ->
              value.onEvent(ButtonClicked(button))
            }
          }
          ?: run {
            dialog.getButton(button.toId())
                ?.visibility = View.INVISIBLE
          }
    }

    dialog.setMessage(value.message)
    dialog.setTitle(value.title)

    return dialogRef
  }

  private fun Button.toId(): Int = when (this) {
    POSITIVE -> DialogInterface.BUTTON_POSITIVE
    NEGATIVE -> DialogInterface.BUTTON_NEGATIVE
    NEUTRAL -> DialogInterface.BUTTON_NEUTRAL
  }

  private class Runner : ViewRunner<AlertContainerScreen<*>> {
    private lateinit var view: AlertContainer

    override fun bind(
      view: View,
      registry: ViewRegistry
    ) {
      this.view = (view as AlertContainer)
      this.view.registry = registry
    }

    override fun update(newValue: AlertContainerScreen<*>) {
      view.update(newValue)
    }
  }

  class Binding(
    @StyleRes private val dialogThemeResId: Int = 0
  ) : ViewBinding<AlertContainerScreen<*>>
  by BuilderBinding(
      type = AlertContainerScreen::class,
      builder = { viewRegistry, initialValue, context, _ ->
        AlertContainer(context, dialogThemeResId = dialogThemeResId)
            .apply {
              id = R.id.workflow_alert_container
              layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
              bindRunner(viewRegistry, initialValue, Runner())
            }
      }
  )
}
