package com.quickbite.spaceslingshot.data.json

/**
 * Created by Paha on 8/31/2016.
 */
class JsonLevelData() {
    var level:Int = 0
    var name:String = ""
    lateinit var ship: JsonShipData
    lateinit var planets:Array<JsonPlanetData>
    lateinit var homePlanet: JsonPlanetData
    var obstacles:Array<JsonObstacleData> = arrayOf()
}