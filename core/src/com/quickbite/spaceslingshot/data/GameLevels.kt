package com.quickbite.spaceslingshot.data

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.objects.AsteroidSpawner
import com.quickbite.spaceslingshot.objects.Obstacle
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.SpaceStation
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
        data.planetList.forEach { planet -> planet.dispose() }
        data.asteroidList.forEach { a -> a.dispose() }

        data.planetList.clear()
        data.obstacleList.clear()
        data.asteroidList.clear()

        var planetTexture: Texture?
        val planetRotation:() -> Float = { MathUtils.random(360f)}

        levelData.planets.forEach { pd ->
            planetTexture = ProceduralPlanetTextureGenerator.getNextTexture()
//            data.planetList.add(Planet(Vector2(pd.pos[0].toFloat(), pd.pos[1].toFloat()), pd.radius, pd.gravityRange, pd.density, planetRotation(), planetTexture!!))
            data.planetList.add(Planet(Vector2(pd.pos[0].toFloat(), pd.pos[1].toFloat()), pd.radius, pd.gravityRange, pd.density, 0f, planetTexture!!))
        }

        levelData.obstacles.forEach { od ->
            data.obstacleList.add(Obstacle(Rectangle(od.rect[0].toFloat(), od.rect[1].toFloat(), od.rect[2].toFloat(), od.rect[3].toFloat())))
        }

        levelData.stations.forEach { sd ->
            planetTexture = ProceduralPlanetTextureGenerator.getNextTexture()
            data.stationList.add(SpaceStation(Vector2(sd.pos[0].toFloat(), sd.pos[1].toFloat()), sd.size, sd.fuelStorage, true))
        }

        levelData.asteroidSpawners.forEach { spawner ->
            data.asteroidSpawnerList.add(AsteroidSpawner(Vector2(spawner.pos[0], spawner.pos[1]), Vector2(spawner.spawnDir[0], spawner.spawnDir[1]),
                    Pair(spawner.spawnFreq[0], spawner.spawnFreq[1]), Pair(spawner.speedRange[0], spawner.speedRange[1]), data, spawner.immediate))
        }

        val hs = levelData.homeStation
        planetTexture = ProceduralPlanetTextureGenerator.getNextTexture(true)
        data.stationList.add(SpaceStation(Vector2(hs.pos[0].toFloat(), hs.pos[1].toFloat()), hs.size, hs.fuelStorage, true))

        val sd = levelData.ship
        data.ship.reset(Vector2(sd.pos[0].toFloat(), sd.pos[1].toFloat()), sd.fuel, Vector2(sd.velocity[0].toFloat(), sd.velocity[1].toFloat()))

        ProceduralPlanetTextureGenerator.generatePlanetTexturesFromData() //Generate the next set of textures

        MyGame.camera.position.set(data.ship.position.x, data.ship.position.y, 0f)

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