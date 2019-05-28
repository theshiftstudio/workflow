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
package com.squareup.sample.gameworkflow

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.squareup.sample.gameworkflow.NewGameScreen.Event.CancelNewGame
import com.squareup.sample.gameworkflow.NewGameScreen.Event.StartGame
import com.squareup.sample.tictactoe.R
import com.squareup.workflow.ui.LayoutBinding
import com.squareup.workflow.ui.ViewBinding
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.ViewRunner
import com.squareup.workflow.ui.setBackHandler

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
internal class NewGameViewRunner : ViewRunner<NewGameScreen> {

  private lateinit var rootView: View
  private lateinit var playerX: EditText
  private lateinit var playerO: EditText
  private lateinit var button: Button

  override fun bind(
    view: View,
    registry: ViewRegistry
  ) {
    rootView = view
    playerX = view.findViewById(R.id.player_X)
    playerO = view.findViewById(R.id.player_O)
    button = view.findViewById(R.id.start_game)
  }

  override fun update(newValue: NewGameScreen) {
    if (playerX.text.isBlank()) playerX.setText(newValue.defaultNameX)
    if (playerO.text.isBlank()) playerO.setText(newValue.defaultNameO)

    button.setOnClickListener {
      newValue.onEvent(
          StartGame(
              playerX.text.toString(),
              playerO.text.toString()
          )
      )
    }

    rootView.setBackHandler { newValue.onEvent(CancelNewGame) }
  }

  companion object : ViewBinding<NewGameScreen> by LayoutBinding.of(
      R.layout.new_game_layout, ::NewGameViewRunner
  )
}
