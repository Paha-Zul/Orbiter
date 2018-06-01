package com.quickbite.spaceslingshot.inputprocessor

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.screens.EditorScreen
import com.quickbite.spaceslingshot.screens.MainMenuScreen
import com.quickbite.spaceslingshot.util.Constants

class EditorScreenInputProcessor(val editorScreen: EditorScreen) : InputProcessor {
    val initialCameraPostion = Vector2()
    val mouseClickPosition = Vector2()

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        when(keycode){
            Input.Keys.ESCAPE -> {
                editorScreen.dispose()
                editorScreen.game.screen = MainMenuScreen(editorScreen.game)}
            Input.Keys.F7 -> {
                editorScreen.placedThings.forEach { it.dispose() } //Dispose each thing
                editorScreen.placedThings.clear() //Clear the list
                editorScreen.editorGUI.clickedOn(null) //Close the editor gui clicked on thingy
            }
        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val offsetX =  mouseClickPosition.x - screenX
        val offsetY = screenY - mouseClickPosition.y
        MyGame.camera.position.set(initialCameraPostion.x + offsetX, initialCameraPostion.y + offsetY, 0f)
        val camPos = MyGame.camera.position
        MyGame.Box2dCamera.position.set(camPos.x* Constants.BOX2D_SCALE, camPos.y* Constants.BOX2D_SCALE, 0f)
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        mouseClickPosition.set(screenX.toFloat(), screenY.toFloat())
        initialCameraPostion.set(MyGame.camera.position.x, MyGame.camera.position.y)
        return false
    }
}