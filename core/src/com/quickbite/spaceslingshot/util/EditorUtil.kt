package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.quickbite.spaceslingshot.json.PlanetJson
import com.quickbite.spaceslingshot.json.ShipJson
import com.quickbite.spaceslingshot.json.StationJson
import com.quickbite.spaceslingshot.objects.Planet
import com.quickbite.spaceslingshot.objects.PlayerShip
import com.quickbite.spaceslingshot.objects.SpaceBody
import com.quickbite.spaceslingshot.objects.SpaceStation

object EditorUtil {
    val json = Json()

    fun saveCurrent(list:List<SpaceBody>){
        val jsonList = mutableListOf<Any>()

        list.forEach {
            when(it){
                is PlayerShip -> jsonList +=
                        ShipJson(it.position, it.rotation, it.velocity, it.fuel)
                is SpaceStation -> jsonList +=
                        StationJson(it.position, it.rotation)
                is Planet -> jsonList +=
                        PlanetJson(it.position, it.size, it.rotation, it.gravityRangeRadius, it.density, 0L)
            }
        }

        val file = Gdx.files.local("data/testLevel.json")
        file.writeString(json.prettyPrint(jsonList), false)


//        println(json.prettyPrint(jsonList))
    }

    fun loadScene():List<SpaceBody>{
        val savedFile = Gdx.files.local("data/testLevel.json")

        //TODO This is broken.. can't convert to type
        val data = json.fromJson(JsonList::class.java, savedFile.readString())

        val objectList = mutableListOf<SpaceBody>()

        data.list.forEach {
            when(it){
                is PlanetJson -> objectList += Planet(it.position, it.size, it.gravityRange,
                        it.gravityStrength, it.rotation, ProceduralPlanetTextureGenerator.getNextTexture())
                is ShipJson -> objectList += PlayerShip(it.position, it.fuel).apply { rotation = it.rotation }
                is StationJson -> objectList += SpaceStation(it.position, 70, 0f, it.rotation, false)
            }
        }

        return objectList
    }

    private class JsonList(){
        lateinit var list:Array<Any>
    }
}