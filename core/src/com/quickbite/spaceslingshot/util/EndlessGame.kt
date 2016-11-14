package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.QueryCallback
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.ProceduralPlanetTextureGenerator
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.SpaceStation
import com.quickbite.spaceslingshot.screens.GameScreen

/**
 * Created by Paha on 9/13/2016.
 * Handles the Endless Game type. Generates the planets/asteroids/obstacles/stations for the players to try and beat.
 */
class EndlessGame(val screen:GameScreen) : IUpdateable, Disposable{
    var randDist = Pair(200f, 400f)
    var randSize = Pair(25, 75)
    var randGravity = Pair(50f, 200f)
    var randDensity = Pair(0.01f, 0.2f)
    val nextPlanetPosition = Vector2()

    lateinit var nextPlanetToPass:Planet
    var planetCounter = 0

    val randStationDist = Pair(800f, 1600f)
    val nextStationPosition = Vector2()

    var hitObject = false

    val data = screen.data

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
        //TODO Add in stations?

        if(nextPlanetPosition.y <= MyGame.camera.position.y + (randSize.second + randGravity.second)*2f)
            addPlanet()

        if(nextStationPosition.y <= MyGame.camera.position.y + MyGame.camera.viewportHeight/2f + 75)
            addStation()

        if(data.ship.position.y >= nextPlanetToPass.position.y) {
            nextPlanetToPass = data.planetList[++planetCounter]
            data.currPlanetScore++
            System.out.println("Planet score: ${data.currPlanetScore}")
        }

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

        nextPlanetPosition.set(MyGame.camera.position.x + randX, nextPlanetPosition.y + randY)

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

        val position:Vector2
        val size = 75
        val physSize = 75*Constants.BOX2D_SCALE
        var rotation = 0f

        var left:Boolean = MathUtils.random() >= 0.5f

        when(left){
            true -> {
                position = Vector2(50f, nextStationPosition.y)
                rotation = 0f
            }
            else -> {
                position = Vector2(MyGame.camera.viewportWidth - size/2f, nextStationPosition.y)
                rotation = 180f
            }
        }

        val physPos = Vector2(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE)

        MyGame.world.QueryAABB(callback, physPos.x - physSize/2f, physPos.y - physSize/2f, physPos.x + physSize/2f, physPos.y + physSize/2)

        //If we hit an object (can't be placed there), flip the sides
        if(hitObject){
            hitObject = false
            left = !left

            when(left){
                true -> {
                    position.set(50f, nextStationPosition.y)
                    rotation = 0f
                }
                else -> {
                    position.set(MyGame.camera.viewportWidth - size/2f, nextStationPosition.y)
                    rotation = 180f
                }
            }

            physPos.set(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE)

            MyGame.world.QueryAABB(callback, physPos.x - physSize/2, physPos.y - physSize/2, physPos.x + physSize/2, physPos.y + physSize/2)

            if(hitObject) {
                nextStationPosition.set(0f, nextStationPosition.y + MathUtils.random(randStationDist.first, randStationDist.second))
                return
            }
        }

        val station = SpaceStation(position, size, 100f, rotation)
        data.stationList.add(station)

        nextStationPosition.set(0f, nextStationPosition.y + MathUtils.random(randStationDist.first, randStationDist.second))
    }

    private fun addStationLeft(){

    }

    private fun addStationRight(){

    }

    fun reset(){
        data.reset()
        start()
    }

    override fun dispose() {

    }
}