package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.data.ProceduralPlanetTextureGenerator
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.objects.Planet

/**
 * Created by Paha on 9/13/2016.
 */
class EndlessGame(val data:GameScreenData) : IUpdateable{
    var counter = 0
    var randDist = Pair(100f, 300f)
    var randSize = Pair(25, 75)
    var randGravity = Pair(50f, 200f)
    var randDensity = Pair(0.05f, 0.3f)
    val nextPosition = Vector2()

    fun start(){
        var planet = Planet(Vector2(MyGame.camera.position.x, MyGame.camera.position.y), 25, 50f, 0.1f, 0f,
                ProceduralPlanetTextureGenerator.getNextTexture(), false)

        data.planetList.add(planet)
        nextPosition.set(MyGame.camera.position.x, MyGame.camera.position.y)

        addPlanet()
        addPlanet()
        addPlanet()

        data.ship.setPosition(MyGame.camera.position.x, MyGame.camera.position.y - MyGame.camera.viewportHeight/2f)
        data.ship.fuel = 50f
        data.ship.setVelocity(0f, 0.1f)
    }

    override fun update(delta: Float) {

    }

    override fun fixedUpdate() {

    }

    private fun addPlanet(){
        val randSize = MathUtils.random(randSize.first, randSize.second)
        val randGravity = MathUtils.random(randGravity.first, randGravity.second)
        val randDensity = MathUtils.random(randDensity.first, randDensity.second)
        val randX = MathUtils.random(-MyGame.camera.viewportWidth/2f, MyGame.camera.viewportHeight/2f)
        val randY = MathUtils.random(randDist.first, randDist.second) + randSize/2f

        nextPosition.set(MyGame.camera.position.x + randX, nextPosition.y + randY)

        val planet = Planet(Vector2(nextPosition.x, nextPosition.y), randSize, randGravity, randDensity, 0f,
                ProceduralPlanetTextureGenerator.getNextTexture(), false)

        data.planetList.add(planet)
    }
}