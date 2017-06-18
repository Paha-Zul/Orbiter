package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.objects.Ship

/**
 * Created by Paha on 8/7/2016.
 */
object Predictor : Disposable{
    private val predictorShip = Ship(Vector2(0f,0f), 0f, Vector2(0f, 0f), true)
    val steps = 300
    val stepSize = 10

    private val points: Array<Vector2> = Array(steps/stepSize, { Vector2() })

    val pointsList:List<Vector2>
        get() = points.toList()

    fun runPrediction(ship: Ship, pausePhysics:()->Unit, resumePhysics:()->Unit, physicsStep:()->Unit){
        predictorShip.reset(Vector2(ship.position), ship.fuel, Vector2(ship.velocity))
        predictorShip.setShipRotation(ship.rotation)
        predictorShip.copyThrusters(ship.thrusters)

        pausePhysics()

        Tests.clearPredictorList()

        for(i in 0..steps-1){
            predictorShip.update(0.016f)
            predictorShip.fixedUpdate(0.016f)
            physicsStep()

            Tests.addToPredictorList(Tests.MovementData(Vector2(predictorShip.position), Vector2(predictorShip.velocity), predictorShip.rotation,
                    predictorShip.planetList.size, predictorShip.fuel, 0.016f))

            if(i%stepSize == 0)
                points[i/stepSize].set(predictorShip.position.x, predictorShip.position.y)
        }

        predictorShip.setVelocity(0f, 0f)

        //Resume physics now that we are done
        resumePhysics()
    }

    private fun simulate(data:GameScreenData, ship:Ship){
        data.planetList.forEach { planet ->
            val dst = ship.position.dst(planet.position)
            if(dst <= planet.gravityRange){

            }
        }
    }

    override fun dispose() {
        predictorShip.dispose()
    }
}