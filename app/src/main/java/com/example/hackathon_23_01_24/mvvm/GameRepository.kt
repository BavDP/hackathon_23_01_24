package com.example.hackathon_23_01_24.mvvm

import com.example.hackathon_23_01_24.models.FieldCell
import com.example.hackathon_23_01_24.models.Picture
import java.util.Random

class GameRepository {
    object IMAGES {
        val imgNames =  listOf("favorite", "home", "key", "star", "timer", "wb_sunny", "filter", "phone", "person")
    }
    fun getFieldPictures(rows: Int, cols: Int): List<FieldCell> {
        val images = IMAGES.imgNames.subList(0, rows * cols / 2).toMutableList()
        images.addAll(images)
        images.shuffle()
        val res = mutableListOf<FieldCell>()
        var k = 0
        for (r in 0..< rows) {
            for (c in 0..< cols) {
                res.add(FieldCell(r, c, Picture(images[k])))
                k++
            }
        }
        return res.toList()
    }
}