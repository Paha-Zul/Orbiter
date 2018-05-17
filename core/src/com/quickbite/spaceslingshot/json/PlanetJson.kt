package com.quickbite.spaceslingshot.json

import com.badlogic.gdx.math.Vector2

class PlanetJson(){
    lateinit var position:Vector2
    var size:Int = 0
    var rotation:Float = 0f
    var gravityRange: Float = 0f
    var gravityStrength: Float = 0f
    var seed: Long = 0L

    constructor(position:Vector2, size:Int, rotation:Float, gravityRange:Float, gravityStrength:Float, seed:Long):this(){
        this.position = position
        this.size = size
        this.rotation = rotation
        this.gravityRange = gravityRange
        this.gravityStrength = gravityStrength
        this.seed = seed
    }
}