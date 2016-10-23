package com.quickbite.spaceslingshot.data

import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.objects.*

/**
 * Created by Paha on 8/6/2016.
 */
class GameScreenData{

    var currPlanetScore = 0
    var levelTimer = 0f
    var currLevel = 0

    lateinit var ship: Ship
    val planetList: Array<Planet> = Array()
    val obstacleList: Array<Obstacle> = Array()
    val asteroidSpawnerList: Array<AsteroidSpawner> = Array()
    val asteroidList: Array<Asteroid> = Array()
    val stationList: Array<SpaceStation> = Array()

    fun reset(){
        planetList.forEach { it.dispose() }
        obstacleList.forEach { it.dispose() }
        asteroidSpawnerList.forEach { it.dispose() }
        asteroidList.forEach { it.dispose() }
        stationList.forEach { it.dispose() }

        planetList.clear()
        obstacleList.clear()
        asteroidSpawnerList.clear()
        asteroidList.clear()
        stationList.clear()
    }
}