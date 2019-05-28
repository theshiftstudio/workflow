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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.squareup.sample.gameworkflow.Ending.Draw
import com.squareup.sample.gameworkflow.Ending.Quitted
import com.squareup.sample.gameworkflow.Ending.Victory
import com.squareup.sample.gameworkflow.GameOverScreen.Event.Exit
import com.squareup.sample.gameworkflow.GameOverScreen.Event.PlayAgain
import com.squareup.sample.gameworkflow.GameOverScreen.Event.TrySaveAgain
import com.squareup.sample.gameworkflow.SyncState.SAVED
import com.squareup.sample.gameworkflow.SyncState.SAVE_FAILED
import com.squareup.sample.gameworkflow.SyncState.SAVING
import com.squareup.sample.tictactoe.R
import com.squareup.workflow.ui.LayoutBinding
import com.squareup.workflow.ui.ViewBinding
import com.squareup.workflow.ui.ViewRegistry
import com.squareup.workflow.ui.ViewRunner
import com.squareup.workflow.ui.setBackHandler

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
internal class GameOverViewRunner : ViewRunner<GameOverScreen> {

  private lateinit var root: View
  private lateinit var boardView: ViewGroup
  private lateinit var toolbar: Toolbar
  private lateinit var saveItem: MenuItem
  private lateinit var exitItem: MenuItem

  override fun bind(
    view: View,
    registry: ViewRegistry
  ) {
    root = view

    boardView = view.findViewById(R.id.game_play_board)
    toolbar = view.findViewById(R.id.game_play_toolbar)

    with(toolbar.menu) {
      saveItem = add("")
      exitItem = add(R.string.exit)
    }

    saveItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    exitItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
  }

  override fun update(newValue: GameOverScreen) {
    exitItem.setOnMenuItemClickListener {
      newValue.onEvent(PlayAgain)
      true
    }
    root.setBackHandler { newValue.onEvent(Exit) }

    when (newValue.endGameState.syncState) {
      SAVING -> {
        saveItem.isEnabled = false
        saveItem.title = "saving…"
        saveItem.setOnMenuItemClickListener(null)
      }
      SAVE_FAILED -> {
        saveItem.isEnabled = true
        saveItem.title = "Unsaved"
        saveItem.setOnMenuItemClickListener {
          newValue.onEvent(TrySaveAgain)
          true
        }
      }
      SAVED -> {
        saveItem.isVisible = false
        saveItem.setOnMenuItemClickListener(null)
      }
    }

    renderGame(
        boardView, toolbar, newValue.endGameState.completedGame, newValue.endGameState.playerInfo
    )
  }

  private fun renderGame(
    boardView: ViewGroup,
    toolbar: Toolbar,
    completedGame: CompletedGame,
    playerInfo: PlayerInfo
  ) {
    renderResult(toolbar, completedGame, playerInfo)
    completedGame.lastTurn.board.render(boardView)
  }

  private fun renderResult(
    toolbar: Toolbar,
    completedGame: CompletedGame,
    playerInfo: PlayerInfo
  ) {
    val symbol = completedGame.lastTurn.playing.symbol
    val playerName = completedGame.lastTurn.playing.name(playerInfo)

    toolbar.title = if (playerName.isEmpty()) {
      when (completedGame.ending) {
        Victory -> "$symbol wins!"
        Draw -> "It's a draw."
        Quitted -> "$symbol is a quitter!"
      }
    } else {
      when (completedGame.ending) {
        Victory -> "The $symbol's have it, $playerName wins!"
        Draw -> "It's a draw."
        Quitted -> "$playerName ($symbol) is a quitter!"
      }
    }
  }

  /** Note how easily  we're sharing this layout with [GamePlayViewRunner]. */
  companion object : ViewBinding<GameOverScreen> by LayoutBinding.of(
      R.layout.game_play_layout, ::GameOverViewRunner
  )
}
