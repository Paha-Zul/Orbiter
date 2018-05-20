package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Json
import com.quickbite.spaceslingshot.json.PlanetJson
import com.quickbite.spaceslingshot.json.ShipJson
import com.quickbite.spaceslingshot.json.StationJson
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Planet
import com.quickbite.spaceslingshot.objects.gamescreenobjects.PlayerShip
import com.quickbite.spaceslingshot.objects.gamescreenobjects.SpaceBody
import com.quickbite.spaceslingshot.objects.gamescreenobjects.SpaceStation

object EditorUtil {
    private val json = Json()
    var loadedLevels:MutableList<JsonLevel> = mutableListOf()

    private val levelFile = "data/testLevel.json"

    private var currLevelEditing = 0

    fun init(){
        val savedFile = Gdx.files.local(levelFile)

        val data = json.fromJson(List::class.java, JsonLevel::class.java, savedFile.readString())
        loadedLevels = data as MutableList<JsonLevel>
    }

    fun getSortedLevels():List<JsonLevel>{
        loadedLevels.sortBy { it.level }
        return loadedLevels
    }

    fun levelExists(number:Int) = loadedLevels.any { it.level ==  number}

    fun saveLevel(levelNumber:Int, levelName:String, list:List<SpaceBody>){
        var ship = ShipJson()
        val planets = mutableListOf<PlanetJson>()
        val stations = mutableListOf<StationJson>()

        list.forEach {
            when(it){
                is PlayerShip -> ship =
                        ShipJson(it.position, it.rotation, it.velocity, it.fuel)
                is SpaceStation -> stations +=
                        StationJson(it.position, it.rotation)
                is Planet -> planets +=
                        PlanetJson(it.position, it.size, it.rotation, it.gravityRangeRadius, it.density, 0L)
            }
        }

        val jsonLevel = JsonLevel().apply {
            this.level = levelNumber
            this.name = levelName
            this.ship = ship
            this.planets = planets
            this.stations = stations
        }

        //Check if we already have a level 1
        val index = loadedLevels.indexOfFirst { it.level == levelNumber }
        if(index >= 0) //If we do, overwrite it
            loadedLevels[index] = jsonLevel
        else //Else, add it to the end of the list
            loadedLevels.add(jsonLevel) //Add it to the list

        loadedLevels.sortBy { it.level }
        val file = Gdx.files.local(levelFile)
        file.writeString(json.prettyPrint(loadedLevels), false)
    }

    fun loadLevel(level:Int):List<SpaceBody>{
        val data = loadedLevels.first { it.level == level }

        val objectList = mutableListOf<SpaceBody>()

        data.stations.forEach {
            objectList += SpaceStation(Vector2(it.position), 70, 0f, it.rotation, false)
        }

        data.planets.forEach {
            objectList += Planet(Vector2(it.position), it.size, it.gravityRange,
                    it.gravityStrength, it.rotation, ProceduralPlanetTextureGenerator.getNextTexture())
        }

        objectList += PlayerShip(Vector2(data.ship.position), data.ship.fuel).apply {
            rotation = data.ship.rotation
            hideControls = true
            velocity.set(data.ship.velocity)
        }

        return objectList
    }

    class JsonLevel{
        var level = 0
        var name = ""
        lateinit var ship:ShipJson
        var planets:List<PlanetJson> = listOf()
        var stations:List<StationJson> = listOf()
    }
}