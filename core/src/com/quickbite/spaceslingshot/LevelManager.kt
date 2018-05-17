package com.quickbite.spaceslingshot

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.data.GameScreenData
import com.quickbite.spaceslingshot.data.json.JsonLevelData
import com.quickbite.spaceslingshot.objects.gamescreenobjects.AsteroidSpawner
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Obstacle
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Planet
import com.quickbite.spaceslingshot.objects.gamescreenobjects.SpaceStation
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.util.Predictor
import com.quickbite.spaceslingshot.util.ProceduralPlanetTextureGenerator

/**
 * Created by Paha on 6/18/2017.
 *
 * Managers level switching and reloading
 */
object LevelManager {
    lateinit var levels:Array<JsonLevelData>

    fun getOrderedLevels() = levels.toList().sortedBy { it.level }

    fun reloadLevel(screen:GameScreen):Boolean{
        return loadLevel(GameScreen.gameScreenData.currLevel, screen)
    }

    fun loadLevel(level:Int, screen:GameScreen):Boolean{
        screen.reset()

        //TODO Need to load these achievements from playerprefs (if they have already beat it before)
        for(i in 0 until GameScreen.gameScreenData.achievementFlags.size)
            GameScreen.gameScreenData.achievementFlags[i] = false

        val success:Boolean
        if(GameScreen.gameScreenData.endlessGame == null) {
            success = loadLevel(level, GameScreen.gameScreenData)
            GameScreen.gameScreenData.currLevel = level
            GameScreen.gui.fuelBar.setAmounts(GameScreen.gameScreenData.ship.fuel, 0f, GameScreen.gameScreenData.ship.fuel)
        }else{
            GameScreen.gameScreenData.endlessGame?.reset()
            success = true
        }

        Predictor.queuePrediction = true

        return success
    }

    fun loadNextLevel(screen:GameScreen):Boolean{
        return loadLevel(++GameScreen.gameScreenData.currLevel, screen)
    }

    private fun loadLevel(level:Int, data: GameScreenData):Boolean{
        if(level >= levels.size) return false

        val levelData = levels.first { it.level == level }
        data.disposeAndClearLists()

        var planetTexture: Texture
        val planetRotation:() -> Float = { MathUtils.random(360f)}

        levelData.planets.forEach { pd ->
            planetTexture = ProceduralPlanetTextureGenerator.getNextTexture()
            data.planetList.add(Planet(Vector2(pd.pos[0].toFloat(), pd.pos[1].toFloat()), pd.radius, pd.gravityRange, pd.density, 0f, planetTexture))
        }

        levelData.obstacles.forEach { od ->
            data.obstacleList.add(Obstacle(Rectangle(od.rect[0].toFloat(), od.rect[1].toFloat(), od.rect[2].toFloat(), od.rect[3].toFloat())))
        }

        levelData.stations.forEach { sd ->
            planetTexture = ProceduralPlanetTextureGenerator.getNextTexture()
            data.stationList.add(SpaceStation(Vector2(sd.pos[0].toFloat(), sd.pos[1].toFloat()), sd.size, sd.fuelStorage, sd.rotation, true))
        }

        levelData.asteroidSpawners.forEach { spawner ->
            data.asteroidSpawnerList.add(AsteroidSpawner(Vector2(spawner.pos[0], spawner.pos[1]), Vector2(spawner.spawnDir[0], spawner.spawnDir[1]),
                    Pair(spawner.spawnFreq[0], spawner.spawnFreq[1]), Pair(spawner.speedRange[0], spawner.speedRange[1]), data, spawner.immediate))
        }

        //TODO Add rotation to station here too
        val hs = levelData.homeStation
        planetTexture = ProceduralPlanetTextureGenerator.getNextTexture()
        data.stationList.add(SpaceStation(Vector2(hs.pos[0].toFloat(), hs.pos[1].toFloat()), hs.size, hs.fuelStorage, hs.rotation, true))

        val sd = levelData.ship
        data.ship.reset(Vector2(sd.pos[0].toFloat(), sd.pos[1].toFloat()), sd.fuel, Vector2(sd.velocity[0], sd.velocity[1]))
        Predictor.points[0].apply {
            position.set(sd.pos[0].toFloat(), sd.pos[1].toFloat())
            fuel = sd.fuel
            velocity = Vector2(sd.velocity[0], sd.velocity[1])
        }

        ProceduralPlanetTextureGenerator.generatePlanetTexturesFromDataThreaded(10) //Generate the next set of textures

        MyGame.camera.position.set(data.ship.position.x, data.ship.position.y, 0f)

        return true
    }
}