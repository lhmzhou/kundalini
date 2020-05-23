package com.kundalini.gui

import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import tornadofx.cellFormat

fun <S, T> TableColumn<S, T>.centeredCellFormat() = cellFormat {
    textFill = Color.BLACK
    text = item.toString()
    textAlignment = TextAlignment.CENTER
    alignment = Pos.CENTER
}

fun <S, T> TableColumn<S, T>.booleanCellFormat() = cellFormat {
    val value = item as? Boolean ?: return@cellFormat
    val (color, textString) = when {
        value -> Pair(Color.GREEN, "✓")
        else -> Pair(Color.RED, "✗")
    }
    textFill = color
    text = textString
    with (font) {
        this@cellFormat.font = Font("$family Bold", size)
    }
    textAlignment = TextAlignment.CENTER
    alignment = Pos.CENTER
}

