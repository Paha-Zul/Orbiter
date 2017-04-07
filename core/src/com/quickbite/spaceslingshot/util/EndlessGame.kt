package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.QueryCallback
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.objects.FuelContainer
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.screens.GameScreen

/**
 * Created by Paha on 9/13/2016.
 * Handles the Endless Game type. Generates the planets/asteroids/obstacles/stations for the players to try and beat.
 */
class EndlessGame(val screen:GameScreen) : IUpdateable, Disposable{
    override var dead: Boolean = false

    var randDist = Pair(200f, 400f)
    var randSize = Pair(25, 75)
    var randGravity = Pair(50f, 200f)
    var randDensity = Pair(0.01f, 0.2f)
    val nextPlanetPosition = Vector2()

    lateinit var nextPlanetToPass:Planet
    var planetCounter = 0

    val randStationDist = Pair(400f, 1000f)
    val nextStationPosition = Vector2()

    var hitObject = false

    val data = screen.data

    val xSpots = arrayOf(68f, 136f, 204f, 272f, 340f, 408f)

    fun start(){
        MyGame.camera.position.set(MyGame.camera.viewportWidth/2f, 0f, 0f)

        val startingPlanet = Planet(Vector2(MyGame.camera.position.x, MyGame.camera.position.y - 100f), 50, 75f, 0.1f, 0f,
                ProceduralPlanetTextureGenerator.getNextTexture(), false)

        nextPlanetToPass = startingPlanet

        data.planetList.add(startingPlanet)
        nextPlanetPosition.set(MyGame.camera.position.x, -100f)
        nextStationPosition.set(0f, MyGame.camera.viewportHeight/2f  +  MathUtils.random(randStationDist.first, randStationDist.second))

        data.ship.setPosition(MyGame.camera.position.x, MyGame.camera.position.y - MyGame.camera.viewportHeight/2f)
        data.ship.setAllFuel(50f)
        data.ship.setVelocity(0f, 0.1f)

//        data.stationList.add(SpaceStation(Vector2(240f, 300f), 50, 100f))

    }

    override fun update(delta: Float) {
        //TODO Add in some asteroid spawners
        //TODO Add in some obstacles?

        if(nextPlanetPosition.y <= MyGame.camera.position.y + (randSize.second + randGravity.second)*2f)
            addPlanet()

        if(nextStationPosition.y <= MyGame.camera.position.y + MyGame.camera.viewportHeight/2f + 75)
            addStation()

        if(data.ship.position.y >= nextPlanetToPass.position.y) {
            nextPlanetToPass = data.planetList[++planetCounter]
            data.currPlanetScore++
            System.out.println("Planet score: ${data.currPlanetScore}")
        }

        //TODO Probably want to adjust this. Game over on leaving camera bounds? Bad idea...
        if(data.ship.position.x <= MyGame.camera.position.x - MyGame.camera.viewportWidth/2f || data.ship.position.x >= MyGame.camera.position.x + MyGame.camera.viewportWidth/2f){
            GameScreen.setGameOver(true)
        }

    }

    override fun fixedUpdate(delta: Float) {

    }

    private fun addPlanet(){
        val randSize = MathUtils.random(randSize.first, randSize.second)
        val randGravity = MathUtils.random(randGravity.first, randGravity.second)
        val randDensity = MathUtils.random(randDensity.first, randDensity.second)
        val randX = MathUtils.random(-MyGame.camera.viewportWidth/2f + (randSize), MyGame.camera.viewportWidth/2f - (randSize))
        val randY = MathUtils.random(randDist.first, randDist.second) + randSize/2f

        nextPlanetPosition.set(MyGame.camera.viewportWidth/2f + randX, nextPlanetPosition.y + randY)

        val planet = Planet(Vector2(nextPlanetPosition.x, nextPlanetPosition.y), randSize, randGravity, randDensity, 0f,
                ProceduralPlanetTextureGenerator.getNextTexture(), false)

        data.planetList.add(planet)
    }

    val callback = QueryCallback { fixture ->
        var result = true
        if(!fixture.isSensor) {
            System.out.println("Hit")
            hitObject = true
            result = false
        }
        result
    }

    private fun addStation(){
        hitObject = false

        GH.shuffleArray(xSpots) //Shuffle the x spots.

        val position:Vector2 = Vector2()
        val physSize = 75*Constants.BOX2D_SCALE

        xSpots.forEach { x ->
            position.set(x, nextStationPosition.y)
            val physPos = Vector2(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE)
            MyGame.world.QueryAABB(callback, physPos.x - physSize/2f, physPos.y - physSize/2f, physPos.x + physSize/2f, physPos.y + physSize/2)

            //If we hit something, we need to try again
            if(hitObject){
                hitObject = false

            //Otherwise, let's move on outta here!
            }else
                return@forEach
        }

        val container = FuelContainer(position, 100)
        data.fuelContainerList.add(container)

        nextStationPosition.set(0f, nextStationPosition.y + MathUtils.random(randStationDist.first, randStationDist.second))
    }

    fun finish(){
        MyGame.actionResolver.submitLeaderboardScore(Constants.LEADERBOARD, data.currPlanetScore)
    }

    fun reset(){
        data.reset()
        start()
    }

    override fun dispose() {

    }
}