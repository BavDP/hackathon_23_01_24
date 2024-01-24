package com.example.hackathon_23_01_24.mvvm

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hackathon_23_01_24.models.FieldCell
import com.example.hackathon_23_01_24.models.GameField
import com.example.hackathon_23_01_24.models.GameFieldSizeErrors
import kotlin.math.max


class GameViewModel(private val repository: GameRepository = GameRepository()): ViewModel() {

    private lateinit var gameField: GameField
    private var playerErrorCount = 0
        set(value) {
            field = value
            playerErrorCountLiveData.value = value
        }

    /**
     * Notification of a change in field size.
     */
    val gameFieldLiveData = MutableLiveData<GameField>()

    /**
     * Notification of an incorrect field size entered by the user.
     */
    val errorMsgLiveData = MutableLiveData<GameFieldSizeErrors>()

    /**
     * Notification of opening a cell in the field.
     */
    val fieldCellOpenLiveData = MutableLiveData<FieldCell>()

    /**
     * Notification of user error and the total number of errors made.
     */
    val playerErrorCountLiveData = MutableLiveData(0)

    /**
     * Notification of user victory.
     */
    val winLiveData = MutableLiveData(false)

    /**
     * * Game start. Accepts the field size specified by the user,
     * checks this size for correctness and if everything is correct, starts the game.
     * Field size checks:
     *  1. must be at least 2 by 2
     *  2. the number of cells must be a multiple of 2
     *  3. the size of the playing field cannot exceed 5 by 5
     * Actions to start the game:
     *  1. display all images on the game field to the user for at least 2 seconds if
     *  the field size is large. Pause calculation: (N*M*300) ms. If this value is less than 2 seconds, then
     *  accept 2 seconds
     *  2. after the pause, close the images and the user must open them from memory.
     */
    fun startGame(rows: Int, cols: Int) {
        playerErrorCount = 0
        if (rows < 2 || cols < 2) {
            errorMsgLiveData.value = GameFieldSizeErrors.MIN_SIZE
        } else if (rows > 5 || cols > 5) {
            errorMsgLiveData.value = GameFieldSizeErrors.MAX_SIZE
        } else if (rows * cols % 2 != 0) {
            errorMsgLiveData.value = GameFieldSizeErrors.MULTIPLE_OF_TWO
        } else {
            gameField = GameField(rows, cols, repository.getFieldPictures(rows, cols))
            gameField.cells.forEach { it.picture.opened = true }
            gameFieldLiveData.value = gameField

            object : CountDownTimer(max(2000, (rows * cols)*300).toLong(), 1000) {
                override fun onTick(p0: Long) {}
                override fun onFinish() {
                    gameField.cells.forEach { it.picture.opened = false }
                    gameFieldLiveData.value = gameField
                }
            }.start()
        }
    }

    /**
     * Opening a new image and notifying the UI of this event via LiveData.
     */
    fun openFieldCell(row: Int, col: Int) {
        if (row > -1 && row < gameField.rows && col > -1 && col < gameField.cols) {
            val cell = gameField.cells.find { fieldCell -> fieldCell.col == col && fieldCell.row == row }
            cell?.let {openingCell ->
                if (!openingCell.picture.opened) {
                    openingCell.picture.opened = true
                    // If the correct paired image is open,
                    // notify the UI via LiveData and check if the user has won, as
                    // this could be the last closed image on the field.
                    if (isPairPictureOpening()) {
                        fieldCellOpenLiveData.value = openingCell
                        winLiveData.value = gameField.cells.filter { it.picture.opened }.size == gameField.cells.size
                    } else {
                        // Processing user error. Actions: close the erroneous pair of images, but
                        // leave those that were opened correctly.
                        gameField.cells
                            .filter { it.picture.opened }
                            .filter { opened -> gameField.cells
                                .filter { !it.picture.opened }
                                .find { opened.picture.name == it.picture.name } != null }
                            .forEach { it.picture.opened = false }
                        gameFieldLiveData.value = gameField
                        playerErrorCount++
                    }
                }
            }
        }
    }

    /**
    * Checks if the user opens the correct image
    * 1. Yes, if the number of open images is odd
    * 2. If the number of open images is not even, then there should be no
    * open images among the closed ones (checked by the image name)
    */
    private fun isPairPictureOpening(): Boolean {
        val opened = gameField.cells.filter{ it.picture.opened }
        if (opened.size % 2 != 0) return true
        val closed = gameField.cells.filter{ !it.picture.opened }
        return !opened.any{o ->
            closed.find { c -> c.picture.name == o.picture.name } != null
        }
    }
}