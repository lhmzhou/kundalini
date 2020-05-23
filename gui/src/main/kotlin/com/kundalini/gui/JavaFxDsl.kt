package com.kundalini.gui

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.Stage

//fun scene(width: Double = 600.0, height: Double = 400.0, buildRoot: () -> Parent) = Scene(buildRoot(), width, height)
//
//fun Scene.show(stage: Stage?) {
//    stage?.scene = this
//    stage?.show()
//}
//
//fun vbox(init: VBox.() -> Unit): VBox = VBox().apply(init)
//fun hbox(init: HBox.() -> Unit): HBox = HBox().apply(init)
//
//fun Pane.button(text: String, onClick: (MouseEvent) -> Unit) = doInit(this, Button())
//    .apply {
//        onMouseClicked = EventHandler {
//            onClick(it)
//        }
//        setText(text)
//    }
//
//fun <T : Pane, R : Node> T.doInit(parent: T, child: R, buildChild: (R.() -> Unit)? = null): R {
//    buildChild?.invoke(child)
//    parent.children.add(child)
//    return child
//}