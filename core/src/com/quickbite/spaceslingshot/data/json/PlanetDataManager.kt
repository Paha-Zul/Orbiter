package com.quickbite.spaceslingshot.data.json

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonValue
import com.quickbite.spaceslingshot.util.EasyColor
import java.util.*

/**
 * Created by Paha on 2/28/2017.
 */
object PlanetDataManager {
    val json = Json()
    val definitionMap: HashMap<String, Array<PlanetTextureData>> = hashMapOf()

    private val buildingDefName = "data/worldGenData.json"

    init {
        json.setSerializer(EasyColor::class.java, object: Json.Serializer<EasyColor> {
            override fun read(json: Json, jsonData: JsonValue, type: Class<*>): EasyColor {
                val data = jsonData.child
                val color = EasyColor(data.asInt(), data.next.asInt(), data.next.next.asInt()) //Make the item link
                return color //Return in
            }

            override fun write(json: Json, `object`: EasyColor, knownType: Class<*>?) {
                json.writeValue(arrayOf(`object`.r, `object`.g, `object`.b)) //Write as an array
            }

        })
    }

    fun readDefinitionsJson(){
        //Load the building defs
        val list = json.fromJson(PlanetData::class.java, Gdx.files.internal(buildingDefName))


        definitionMap.put("earth", list.earth)
        definitionMap.put("desert", list.desert)
        definitionMap.put("ice", list.ice)
        definitionMap.put("lava", list.lava)
    }

    class PlanetData{
        lateinit var earth:Array<PlanetTextureData>
        lateinit var desert:Array<PlanetTextureData>
        lateinit var ice:Array<PlanetTextureData>
        lateinit var lava:Array<PlanetTextureData>
    }

    class PlanetTextureData(){
        lateinit var colors:Array<Array<EasyColor>>
        lateinit var transitions:Array<Float>
    }


}