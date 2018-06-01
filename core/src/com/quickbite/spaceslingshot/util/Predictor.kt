package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.objects.*
import com.quickbite.spaceslingshot.objects.gamescreenobjects.PlayerShip
import com.quickbite.spaceslingshot.objects.gamescreenobjects.ShipBase
import com.quickbite.spaceslingshot.objects.gamescreenobjects.TestShip
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Thruster

/**
 * Created by Paha on 8/7/2016.
 */
object Predictor : Disposable{
    private var queuePrediction = false

    val predictorShip = TestShip(Vector2(0f, 0f), 0f)
    private val steps = 300
    private val stepSizeToRecord = 1

    var currPointIndex = 0

    var ship: PlayerShip? = null
    private lateinit var pausePhysicsFunc:()->Unit
    private lateinit var resumePhysicsFunc:()->Unit
    private lateinit var physicsStep:()->Unit

    private var lineDrawer = LineDraw(Vector2(), Vector2(), GH.createPixel(Color.WHITE))

    fun init(ship: PlayerShip, pausePhysicsFunc:()->Unit, resumePhysicsFunc:()->Unit, physicsStep:()->Unit){
        this.ship = ship
        this.pausePhysicsFunc = pausePhysicsFunc
        this.resumePhysicsFunc = resumePhysicsFunc
        this.physicsStep = physicsStep
    }

    val points: Array<ShipDataHolder> = Array(steps / stepSizeToRecord, {
        ShipDataHolder(Vector2(), 0f, Vector2(), 0f, listOf(
                Thruster(0.005f, 0.1f, Vector2(1f, 0f), ShipBase.ShipLocation.Rear, 0f), //Rear
                Thruster(0.005f, 0.1f, Vector2(0f, -1f), ShipBase.ShipLocation.Left, -90f), //Left
                Thruster(0.005f, 0.1f, Vector2(0f, 1f), ShipBase.ShipLocation.Right, 90f) //Right
        ))
    })

    fun update(){
        if(queuePrediction)
            predict()
    }

    /**
     * Queues the prediction for the next frame
     */
    fun queuePrediction() {
        queuePrediction = true
    }

    private fun predict() {
        runPrediction()
        lineDrawer?.points = Predictor.points.toList()
        queuePrediction = false
    }


    fun setShipVelocityAsCurrentPredictorVelocity() {
        ship?.velocity?.set(Predictor.points[Predictor.currPointIndex].velocity)
    }

    fun setPredictorShipToPlayerShip(ship:PlayerShip){
        points[0].position.set(ship.position)
        currPointIndex = 0
    }

    private fun runPrediction(){
        if(ship == null)
            return

        val pointToStartFrom = points[currPointIndex]
        predictorShip.reset(pointToStartFrom.position, ship!!.fuel, Vector2(ship!!.velocity))
        predictorShip.setShipRotation(ship!!.rotation)
        predictorShip.copyThrusters(ship!!.thrusters)

        pausePhysicsFunc()

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
        resumePhysicsFunc()
    }

    private fun simulate(data:GameScreenData, ship: PlayerShip){
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