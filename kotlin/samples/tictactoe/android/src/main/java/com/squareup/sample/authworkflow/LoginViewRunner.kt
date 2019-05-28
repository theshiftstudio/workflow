/*
 * Copyright 2017 Square Inc.
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
package com.squareup.sample.authworkflow

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.squareup.sample.authworkflow.LoginScreen.SubmitLogin
import com.squareup.sample.tictactoe.R
import com.squareup.workflow.ui.LayoutBinding
import com.squareup.workflow.ui.ViewBinding
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.ViewRunner

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
internal class LoginViewRunner : ViewRunner<LoginScreen> {
  private lateinit var error: TextView
  private lateinit var email: EditText
  private lateinit var password: EditText
  private lateinit var button: Button

  override fun bind(
    view: View,
    registry: ViewRegistry
  ) {
    error = view.findViewById(R.id.login_error_message)
    email = view.findViewById(R.id.login_email)
    password = view.findViewById(R.id.login_password)
    button = view.findViewById(R.id.login_button)
  }

  override fun update(newValue: LoginScreen) {
    error.text = newValue.errorMessage

    button.setOnClickListener {
      newValue.onEvent(
          SubmitLogin(
              email.text.toString(), password.text.toString()
          )
      )
    }
  }

  companion object : ViewBinding<LoginScreen> by LayoutBinding.of(
      R.layout.login_layout, ::LoginViewRunner
  )
}
