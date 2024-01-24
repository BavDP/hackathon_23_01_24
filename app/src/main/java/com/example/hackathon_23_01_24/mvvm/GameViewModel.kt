package com.example.hackathon_23_01_24.mvvm

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hackathon_23_01_24.models.FieldCell
import com.example.hackathon_23_01_24.models.GameField


class GameViewModel(private val repository: GameRepository = GameRepository()): ViewModel() {

    private lateinit var gameField: GameField
    private var playerErrorCount = 0
        set(value) {
            field = value
            playerErrorCountLiveData.value = value
        }

    val gameFieldLiveData = MutableLiveData<GameField>()
    val errorMsgLiveData = MutableLiveData<String>()
    val fieldCellOpenLiveData = MutableLiveData<FieldCell>()
    val playerErrorCountLiveData = MutableLiveData<Int>(0)
    val winLiveData = MutableLiveData<Boolean>(false)

    fun startGame(rows: Int, cols: Int) {
        playerErrorCount = 0
        if (rows < 2 || cols < 2) {
            errorMsgLiveData.value = "indicate the playing field size is at least 2 by 2!"
        } else if (rows > 5 || cols > 5) {
            errorMsgLiveData.value = "Max size of game field is 5!"
        } else if (rows * cols % 2 != 0) {
            errorMsgLiveData.value = "Еhe number of cells on the field must be a multiple of 2"
        } else {
            gameField = GameField(rows, cols, repository.getFieldPictures(rows, cols))
            gameField.cells.forEach { it.picture.opened = true }
            gameFieldLiveData.value = gameField

            object : CountDownTimer(2000, 1000) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    gameField.cells.forEach { it.picture.opened = false }
                    gameFieldLiveData.value = gameField
                }
            }.start()
        }
    }

    fun openFieldCell(row: Int, col: Int) {
        if (row > -1 && row < gameField.rows && col > -1 && col < gameField.cols) {
            val cell = gameField.cells.find { fieldCell -> fieldCell.col == col && fieldCell.row == row }
            cell?.let {
                if (!it.picture.opened) {
                    it.picture.opened = true
                    if (canContinueGame()) {
                        fieldCellOpenLiveData.value = it
                        winLiveData.value = gameField.cells.filter { it.picture.opened }.size == gameField.cells.size
                    } else {
                        gameField.cells
                            .filter { it.picture.opened }
                            .filter { opened -> gameField.cells.filter { !it.picture.opened }.find { opened.picture.name == it.picture.name } != null }
                            .forEach { it.picture.opened = false }
                        gameFieldLiveData.value = gameField
                        playerErrorCount++
                    }
                }
            }
        }
    }

    private fun canContinueGame(): Boolean {
        val opened = gameField.cells.filter{ it.picture.opened }
        if (opened.size % 2 != 0) return true
        val closed = gameField.cells.filter{ !it.picture.opened }
        return !opened.any{o ->
            closed.find { c -> c.picture.name == o.picture.name } != null
        }
    }
}