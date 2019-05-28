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

import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import com.squareup.sample.gameworkflow.GamePlayScreen.Event.Quit
import com.squareup.sample.gameworkflow.GamePlayScreen.Event.TakeSquare
import com.squareup.sample.tictactoe.R
import com.squareup.workflow.ui.LayoutBinding
import com.squareup.workflow.ui.ViewBinding
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.ViewRunner
import com.squareup.workflow.ui.setBackHandler

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
internal class GamePlayViewRunner : ViewRunner<GamePlayScreen> {
  private lateinit var rootView: View
  private lateinit var boardView: ViewGroup
  private lateinit var toolbar: Toolbar

  override fun bind(
    view: View,
    registry: ViewRegistry
  ) {
    rootView = view
    boardView = view.findViewById(R.id.game_play_board)
    toolbar = view.findViewById(R.id.game_play_toolbar)
  }

  override fun update(newValue: GamePlayScreen) {
    renderBanner(newValue.gameState, newValue.playerInfo)
    newValue.gameState.board.render(boardView)

    setCellClickListeners(boardView, newValue.gameState) { newValue.onEvent(it) }
    rootView.setBackHandler { newValue.onEvent(Quit) }
  }

  private fun setCellClickListeners(
    viewGroup: ViewGroup,
    turn: Turn,
    takeSquareHandler: (TakeSquare) -> Unit
  ) {
    for (i in 0..8) {
      val cell = viewGroup.getChildAt(i)

      val row = i / 3
      val col = i % 3
      val box = turn.board[row][col]

      val cellClickListener =
        if (box != null) null
        else View.OnClickListener { takeSquareHandler(TakeSquare(row, col)) }

      cell.setOnClickListener(cellClickListener)
    }
  }

  private fun renderBanner(
    turn: Turn,
    playerInfo: PlayerInfo
  ) {
    val mark = turn.playing.symbol
    val playerName = turn.playing.name(playerInfo)
        .trim()

    toolbar.title = when {
      playerName.isEmpty() -> "Place your $mark"
      else -> "$playerName, place your $mark"
    }
  }

  companion object : ViewBinding<GamePlayScreen> by LayoutBinding.of(
      R.layout.game_play_layout, ::GamePlayViewRunner
  )
}
