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
 *
 * Handles the Endless Game type. Generates the planets/asteroids/obstacles/stations for the players to try and beat.
 */
class EndlessGame(screen:GameScreen) : IUpdateable, Disposable{
    override var dead: Boolean = false

    var randDist = Pair(200f, 400f)
    var randSize = Pair(25, 75)
    var randGravity = Pair(50f, 200f)
    var randDensity = Pair(0.01f, 0.2f)
    val nextPlanetPosition = Vector2()

    lateinit var nextPlanetToPass:Planet
    var planetCounter = 0

    val randFuel = Pair(400f, 1000f)
    val nextFuelContainerPosition = Vector2()

    var hitObject = false

    val data = GameScreen.gameScreenData

    lateinit var xSpots:Array<Float>

    fun start(){
        buildXSpots()

        MyGame.camera.position.set(0f, 0f, 0f)

        val startingPlanet = Planet(Vector2(MyGame.camera.position.x, MyGame.camera.position.y - 100f), 50, 75f, 0.1f, 0f,
                ProceduralPlanetTextureGenerator.getNextTexture(), false)

        nextPlanetToPass = startingPlanet

        data.planetList.add(startingPlanet)
        nextPlanetPosition.set(MyGame.camera.position.x, -100f)
        nextFuelContainerPosition.set(0f, MathUtils.random(randFuel.first, randFuel.second))

        data.ship.reset(Vector2(MyGame.camera.position.x, MyGame.camera.position.y - MyGame.camera.viewportHeight/2f), 50f, Vector2(0f, 0.1f))
        data.ship.setShipRotation(90f)
    }

    private fun buildXSpots(){
        val totalWidth = 400f
        var currCounter = -totalWidth/2f
        val amt = 5
        val stepSize = totalWidth/amt

        xSpots = Array(amt+1, {0f})
        for (i in 0..xSpots.size-1) {
            xSpots[i] = totalWidth + currCounter
            currCounter += -stepSize
        }

        println("done")
    }

    override fun update(delta: Float) {
        //TODO Add in some asteroid spawners
        //TODO Add in some obstacles?

        if(nextPlanetPosition.y <= MyGame.camera.position.y + MyGame.camera.viewportHeight*2f)
            addPlanet()

        if(nextFuelContainerPosition.y <= MyGame.camera.position.y + MyGame.camera.viewportHeight/2f + 75)
            addFuel()

        if(data.ship.position.y >= nextPlanetToPass.position.y) {
            nextPlanetToPass = data.planetList[++planetCounter]
            data.currPlanetScore++
            System.out.println("Planet score: ${data.currPlanetScore}")
        }

        //TODO Probably want to adjust this. Game over on leaving camera bounds? Bad idea...
        //If the ship leaves the bounds we have defined in Constants... plus half of the viewport (camera), game over
        if(data.ship.position.x <= - (Constants.ENDLESS_GAME_SCREEN_BOUND + MyGame.camera.viewportWidth/2f)
                || data.ship.position.x >= MyGame.camera.viewportWidth/2f + Constants.ENDLESS_GAME_SCREEN_BOUND){
            GameScreen.setGameOver(true)
        }

    }

    override fun fixedUpdate(delta: Float) {

    }

    private fun addPlanet(){
        val randSize = MathUtils.random(randSize.first, randSize.second) //Random the planet size
        val randGravity = MathUtils.random(randGravity.first, randGravity.second) //Random planet gravity (well size)
        val randDensity = MathUtils.random(randDensity.first, randDensity.second) //Random planet density
        val randX = MathUtils.random(-MyGame.camera.viewportWidth/2f + (randSize), MyGame.camera.viewportWidth/2f - (randSize)) //Random the X position
        val randY = MathUtils.random(randDist.first, randDist.second) + randSize/2f //Random the Y position

        nextPlanetPosition.set(randX, nextPlanetPosition.y + randY) //Set the next planet position

        //Create the planet
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

    private fun addFuel(){
        hitObject = false

        GH.shuffleArray(xSpots) //Shuffle the x spots.

        val position:Vector2 = Vector2()
        val physSize = 75*Constants.BOX2D_SCALE

        xSpots.forEach { x ->
            position.set(x, nextFuelContainerPosition.y)
            val physPos = Vector2(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE)
            MyGame.world.QueryAABB(callback, physPos.x - physSize, physPos.y - physSize, physPos.x + physSize, physPos.y + physSize)

            //If we hit something, we need to try again
            if(hitObject){
                hitObject = false

            //Otherwise, let's move on outta here!
            }else
                return@forEach
        }

        val container = FuelContainer(position, 100)
        data.fuelContainerList.add(container)

        nextFuelContainerPosition.set(0f, nextFuelContainerPosition.y + MathUtils.random(randFuel.first, randFuel.second))
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