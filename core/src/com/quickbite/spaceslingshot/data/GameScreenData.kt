package com.quickbite.spaceslingshot.data

import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.objects.*

/**
 * Created by Paha on 8/6/2016.
 */
class GameScreenData{
    var currLevel = 0
    lateinit var ship: Ship
    val planetList: Array<Planet> = Array()
    val obstacleList: Array<Obstacle> = Array()
    val asteroidSpawnerList: Array<AsteroidSpawner> = Array()
    val asteroidList: Array<Asteroid> = Array()
    val stationList: Array<SpaceStation> = Array()
}