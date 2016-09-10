package com.quickbite.spaceslingshot

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.spaceslingshot.screens.GameScreen

/**
 * Created by Paha on 8/7/2016.
 */

class GameScreenInputListener(val screen: GameScreen) : InputProcessor{
    var draggingRotateShip = false
    var draggingShipBurnTime = false
    var draggingScreen = false

    val startDragPos:Vector2 = Vector2()
    val offset:Vector2 = Vector2()
    val originalCameraPos = Vector2()

    var tapTime:Long = 0

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        var runPredictor = false
        if(draggingRotateShip)
            runPredictor = true

        val timeDiff = (TimeUtils.millis() - tapTime)/1000f
        if(timeDiff <= 0.2f){
            if(draggingShipBurnTime) {
                screen.toggleShipBurn()
                runPredictor = true
            }
        }

        draggingRotateShip = false
        draggingShipBurnTime = false
        draggingScreen = false

        if(runPredictor)
            screen.runPredictor()

        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val world = MyGame.camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val worldPos = Vector2(world.x, world.y) //Temp for now
        originalCameraPos.set(MyGame.camera.position.x, MyGame.camera.position.y)

        startDragPos.set(screenX.toFloat(), screenY.toFloat())

        if(screen.paused) {
            val clicked = screen.data.ship.clickOnShip(worldPos.x, worldPos.y)
            when (clicked) {
                1 -> draggingRotateShip = true
                2 -> draggingShipBurnTime = true
                0 -> draggingScreen = true
            }
        }else{
            draggingScreen = true
        }

        tapTime = TimeUtils.millis()

        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if(keycode == Input.Keys.SPACE)
            screen.toggleGamePause()


        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val world = MyGame.camera.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0f))
        val worldPos = Vector2(world.x, world.y) //Temp for now
        val ship = screen.data.ship

        //TODO Figure out dragging camera

        if(draggingRotateShip) {
            ship.setRotationTowardsMouse(worldPos.x, worldPos.y)
            screen.runPredictor()
        }else if(draggingShipBurnTime){
            ship.dragBurn(worldPos.x, worldPos.y)
            screen.gui.fuelBar.setAmounts(ship.fuel, ship.burnAmount)
            screen.runPredictor()
        }else if(draggingScreen){
            offset.set(screenX - startDragPos.x, screenY - startDragPos.y)
            screen.scrollScreen(originalCameraPos.x - offset.x, originalCameraPos.y + offset.y)
//            MyGame.camera.position.set(originalCameraPos.x - offset.x, originalCameraPos.y + offset.y, 0f)
//            System.out.println("Dragging, offset: $offset, camera: ${MyGame.camera.position}, worldPos: $worldPos, screen: X: $screenX, Y: $screenY")
        }

        return false
    }
}