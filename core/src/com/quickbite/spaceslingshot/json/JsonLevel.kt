package com.quickbite.spaceslingshot.json

import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.data.json.AchievementJson

class JsonLevel{
    var level = 0
    var name = ""
    lateinit var ship:ShipJson
    var planets:List<PlanetJson> = listOf()
    var stations:List<StationJson> = listOf()
    var achievements:Array<AchievementJson> = Array()
}