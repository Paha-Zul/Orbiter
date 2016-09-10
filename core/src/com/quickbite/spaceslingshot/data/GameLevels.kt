package com.quickbite.spaceslingshot.data

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.objects.Obstacle
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.Ship
import com.quickbite.spaceslingshot.util.JsonLevelLoader

/**
 * Created by Paha on 8/8/2016.
 */
object GameLevels {
    fun loadLevel(level:Int, data: GameScreenData):Boolean{
        val levels = JsonLevelLoader.levels;

        if(level >= levels.size) return false

        System.out.println("Loading level $level")

        val levelData = levels[level]
        data.planetList.clear()
        data.obstacleList.clear()

        var planetTexture: Texture?
        val planetRotation:() -> Float = { MathUtils.random(360f)}
        var textureCounter = 0

        levelData.planets.forEach { pd ->
            planetTexture = ProceduralPlanetTextureGenerator.textureArray[textureCounter++]
            data.planetList.add(Planet(Vector2(pd.pos[0].toFloat(), pd.pos[1].toFloat()), pd.radius, pd.gravityRange, pd.density, planetRotation(), planetTexture!!))
        }

        levelData.obstacles.forEach { od ->
            data.obstacleList.add(Obstacle(Rectangle(od.rect[0].toFloat(), od.rect[1].toFloat(), od.rect[2].toFloat(), od.rect[3].toFloat())))
        }

        val hp = levelData.homePlanet
        planetTexture = ProceduralPlanetTextureGenerator.textureArray[textureCounter++]
        data.planetList.add(Planet(Vector2(hp.pos[0].toFloat(), hp.pos[1].toFloat()), hp.radius, hp.gravityRange, hp.density, planetRotation(), planetTexture!!, true))

        val sd = levelData.ship
        data.ship = Ship(Vector2(sd.pos[0].toFloat(), sd.pos[1].toFloat()), sd.fuel, Vector2(sd.velocity[0].toFloat(), sd.velocity[1].toFloat()))

        ProceduralPlanetTextureGenerator.generatePlanetTexturesThreaded(10) //Generate the next set of textures

        return true
    }

    private fun getPlanetType(name:String):PlanetData.PlanetType{
        when(name){
            "Earth" -> return PlanetData.PlanetType.Earth
            "Ice" -> return PlanetData.PlanetType.Ice
            "Desert" -> return PlanetData.PlanetType.Desert
            "Lava" -> return PlanetData.PlanetType.Lava
            else -> return PlanetData.PlanetType.Earth
        }
    }
}