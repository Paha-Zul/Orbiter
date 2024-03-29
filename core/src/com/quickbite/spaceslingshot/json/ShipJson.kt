package com.quickbite.spaceslingshot.json

import com.badlogic.gdx.math.Vector2

data class ShipJson(val position:Vector2, val rotation:Float, val velocity:Vector2, val fuel:Float){

    /**
     * Needed for loading from Json file
     */
    constructor():this(Vector2(), 0f, Vector2(), 0f)
}