package com.quickbite.spaceslingshot.data.json

/**
 * Created by Paha on 9/11/2016.
 */
class JsonAsteroidSpawner {
    var immediate:Boolean = false
    lateinit var pos:Array<Float>
    lateinit var spawnDir:Array<Float>
    lateinit var spawnFreq:Array<Float>
    lateinit var speedRange:Array<Float>
}