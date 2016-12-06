package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.quickbite.spaceslingshot.data.json.JsonLevelData

/**
 * Created by Paha on 8/31/2016.
 */

object JsonLevelLoader{
    val json:Json = Json()

    fun loadLevels(){
        GameLevels.levels = json.fromJson(Array<JsonLevelData>::class.java, Gdx.files.internal("data/levels.json"))
    }
}