package com.example.hackathon_23_01_24

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.viewModels
import com.example.hackathon_23_01_24.databinding.ActivityMainBinding
import com.example.hackathon_23_01_24.models.FieldCell
import com.example.hackathon_23_01_24.mvvm.GameViewModel
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding
    private val images = mutableListOf<ImageView>()
    private val binding by lazy { _binding }
    private val viewModel by viewModels<GameViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
            setContentView(_binding.root)
            init()
            initListeners()

    }

    private fun init() {
        binding.gameField.visibility = View.INVISIBLE
        lookupImages(){_, _, _, img ->
            images.add(img)
        }
        initVisibilityImagesOfGameField()
        initObserves()
    }

    private fun initListeners() {
        binding.startGameBtn.setOnClickListener {
            startGameBtnClick()
        }
        lookupImages() {r,c,_,img ->
            img.setOnClickListener{ imageClick(r,c,img) }
        }
    }

    private fun lookupImages(fieldSize: Int = 5, callback: (row: Int, col: Int, tRow: TableRow, img: ImageView) -> Unit) {
        for (i in 0..<fieldSize) {
            val row = findViewById<TableRow>(resources.getIdentifier("row$i", "id", packageName))
            for (j in 0..<fieldSize) {
                val img = row.findViewById<ImageView>(resources.getIdentifier("img$j", "id", packageName))
                callback(i, j, row, img)
            }
        }
    }

    private fun getImageView(row: Int, col: Int): ImageView? {
        var img: ImageView? = null;
        val row = findViewById<TableRow>(resources.getIdentifier("row$row", "id", packageName))
        if (row != null) {
            img = row.findViewById<ImageView>(resources.getIdentifier("img$col", "id", packageName))
        }
        return img
    }

    private fun initVisibilityImagesOfGameField() {
        images.forEachIndexed {_, img ->
            img.visibility = View.INVISIBLE
        }
    }

    private fun initObserves() {
        binding.gameField.visibility = View.VISIBLE

        viewModel.gameFieldLiveData.observe(this) {gameField ->
            initVisibilityImagesOfGameField()
            lookupImages() { r, c, _, img ->
                if (r < gameField.rows && c < gameField.cols) {
                    img.visibility = View.VISIBLE
                    setImageViewPicture(img, gameField.cells.find { it.row == r && it.col == c })
                }
            }
        }

        viewModel.errorMsgLiveData.observe(this) {errMsg ->
            if (errMsg != "") {
                Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.fieldCellOpenLiveData.observe(this) {cell->
            getImageView(cell.row, cell.col)?.let{imageView ->
                setImageViewPicture(imageView, cell)
            }
        }

        viewModel.playerErrorCountLiveData.observe(this){
            binding.playerErrors.text = resources.getString(R.string.player_error_count, it)
        }

        viewModel.winLiveData.observe(this) {
            if (it) {
                Toast.makeText(this, getString(R.string.win), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setImageViewPicture(imageView: ImageView, fieldCell: FieldCell?) {
        if (fieldCell?.picture?.opened == true) {
            imageView.setImageResource(resources.getIdentifier(fieldCell.picture.name, "drawable", packageName))
        } else {
            imageView.setImageResource(resources.getIdentifier(getString(R.string.question_mark), "drawable", packageName))
        }
    }

    private fun imageClick(row: Int, col: Int, image: ImageView) {
        viewModel.openFieldCell(row, col)
    }

    private fun startGameBtnClick() {
        val rows = binding.rowsAmount.text.toString()
        val cols = binding.colsAmount.text.toString()
        viewModel.startGame(rows.toIntOrNull()?:0, cols.toIntOrNull()?:0)
    }
}