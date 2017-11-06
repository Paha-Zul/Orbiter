package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.data.ShipData
import com.quickbite.spaceslingshot.objects.Ship
import com.quickbite.spaceslingshot.objects.ShipDataHolder
import com.quickbite.spaceslingshot.objects.Thruster

/**
 * Created by Paha on 8/7/2016.
 */
object Predictor : Disposable{
    val predictorShip = Ship(Vector2(0f,0f), 0f, Vector2(0f, 0f), true)
    private val steps = 300
    private val stepSizeToRecord = 1

    val points: Array<ShipDataHolder> = Array(steps / stepSizeToRecord, {
        ShipDataHolder(Vector2(), 0f, Vector2(), 0f, listOf(
                Thruster(0.005f, 0.1f, Vector2(1f, 0f), Ship.ShipLocation.Rear, 0f), //Rear
                Thruster(0.005f, 0.1f, Vector2(0f, -1f), Ship.ShipLocation.Left, -90f), //Left
                Thruster(0.005f, 0.1f, Vector2(0f, 1f), Ship.ShipLocation.Right, 90f) //Right
        ))
    })

    var currPointIndex = 0

    fun runPrediction(ship: Ship, pausePhysics:()->Unit, resumePhysics:()->Unit, physicsStep:()->Unit){
        val pointToStartFrom = points[currPointIndex]
        predictorShip.reset(Vector2(pointToStartFrom.position), ship.fuel, Vector2(ship.velocity))
        predictorShip.setShipRotation(ship.rotation)
        predictorShip.copyThrusters(ship.thrusters)

        pausePhysics()

        Tests.clearPredictorList()

        currPointIndex = 0

        //Run the predictor ship simulation
        for(i in 0 until steps){
            predictorShip.update(0.016f)
            predictorShip.fixedUpdate(0.016f)
            physicsStep()

            Tests.addToPredictorList(Tests.MovementData(Vector2(predictorShip.position), Vector2(predictorShip.velocity), predictorShip.rotation,
                    predictorShip.planetList.size, predictorShip.fuel, 0.016f))

            if(i% stepSizeToRecord == 0){
                //Record a bunch of stuff
                points[i/ stepSizeToRecord].apply {
                    position.set(predictorShip.position.x, predictorShip.position.y)
                    fuel = predictorShip.fuel
                    velocity = Vector2(predictorShip.velocity) //Copy this!!
                    rotation = predictorShip.rotation
                    thrusterData.forEachIndexed{i, thruster ->
                        thruster.burnForce = predictorShip.thrusters[i].burnForce
                        thruster.fuelBurnedPerTick = predictorShip.thrusters[i].fuelBurnedPerTick
                    }
                }
            }
                points[i/ stepSizeToRecord].position.set(predictorShip.position.x, predictorShip.position.y)
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