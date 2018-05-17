package com.quickbite.spaceslingshot.json

import com.badlogic.gdx.math.Vector2

data class StationJson(val position: Vector2, val rotation:Float){

    /**
     * Needed for loading from json file
     */
    constructor():this(Vector2(), 0f)
}