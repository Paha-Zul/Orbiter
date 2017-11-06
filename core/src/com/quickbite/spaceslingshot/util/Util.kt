package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.objects.Ship
import com.quickbite.spaceslingshot.screens.GameScreen

/**
 * Created by Paha on 12/5/2016.
 */
object Util {
    fun runPredictor(){
        Predictor.runPrediction(GameScreen.gameScreenData.ship, { GameScreen.pauseAllPhysicsExceptPredictorShip(GameScreen.gameScreenData) }, { GameScreen.resumeAllPhysicsExceptPredictorShip(GameScreen.gameScreenData) },
                { MyGame.world.step(Constants.PHYSICS_TIME_STEP, Constants.VELOCITY_ITERATIONS, Constants.POSITION_ITERATIONS)})

        GameScreen.predictorLineDrawer.points = Predictor.points.toList()
    }

    fun setShipVelocityAsCurrentPredictorVelocity(ship: Ship){
        ship.velocity.set(Predictor.points[Predictor.currPointIndex].velocity)
    }
}