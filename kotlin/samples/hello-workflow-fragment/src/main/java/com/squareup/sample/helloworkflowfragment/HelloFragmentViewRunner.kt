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
package com.squareup.sample.helloworkflowfragment

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import com.squareup.sample.helloworkflowfragment.HelloWorkflow.Rendering
import com.squareup.workflow.ui.LayoutBinding
import com.squareup.workflow.ui.ViewBinding
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.ViewRunner

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
class HelloFragmentViewRunner : ViewRunner<Rendering> {
  private lateinit var messageView: TextView

  override fun bind(
    view: View,
    registry: ViewRegistry
  ) {
    messageView = view.findViewById(R.id.hello_message)
  }

  @SuppressLint("SetTextI18n")
  override fun update(newValue: HelloWorkflow.Rendering) {
    messageView.text = newValue.message + " Fragment!"
    messageView.setOnClickListener { newValue.onClick(Unit) }
  }

  companion object : ViewBinding<Rendering> by LayoutBinding.of(
      R.layout.hello_goodbye_layout, ::HelloFragmentViewRunner
  )
}
