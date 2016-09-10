package com.quickbite.spaceslingshot.data

import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.objects.Obstacle
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.Ship

/**
 * Created by Paha on 8/6/2016.
 */
class GameScreenData{
    var currLevel = 0
    lateinit var ship: Ship
    val planetList: Array<Planet> = Array()
    val obstacleList: Array<Obstacle> = Array()
}