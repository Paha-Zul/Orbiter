package com.quickbite.spaceslingshot

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.Ship

/**
 * Created by Paha on 8/7/2016.
 */
object Predictor {
    private val predictorShip = Ship(Vector2(0f,0f), 0f, Vector2(0f, 0f), true)
    private val points:Array<Vector2> = Array()

    val steps = 200

    val pointsList:List<Vector2>
        get() = points.toList()

    fun runPrediction(planets:Array<Planet>, ship:Ship, stepFunction:(planets:Array<Planet>, ship:Ship) -> Unit){
        points.clear()

        predictorShip.position.set(ship.position.x, ship.position.y)
        predictorShip.fuel = ship.fuel
        predictorShip.velocity.set(ship.velocity.x, ship.velocity.y)
        predictorShip.rotation = ship.rotation
        predictorShip.burnTime = ship.burnTime
        predictorShip.setBurnForceAndPerTick(ship.burnForce, ship.burnPerTick)

        for(i in 0..steps){
            stepFunction(planets, predictorShip)
            predictorShip.update(0.016f)

            if(i%10 == 0)
                points.add(Vector2(predictorShip.position.x, predictorShip.position.y))
        }
    }

}