package com.example.hackathon_23_01_24.models

data class GameField (
    val rows: Int,
    val cols: Int,
    val cells: List<FieldCell>
)