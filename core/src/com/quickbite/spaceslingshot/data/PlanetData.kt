package com.quickbite.spaceslingshot.data

import com.badlogic.gdx.math.Vector2

/**
 * Created by Paha on 8/8/2016.
 */
class PlanetData(val position: Vector2, val radius: Int, val _gravityRangeRadius: Float, val _density: Float, val normalTextureSeed:Long, val cloudTextureSeed:Long, val planetType: PlanetType = PlanetData.PlanetType.Earth) {
    enum class PlanetType{
        Earth, Desert, Ice, Gas, Lava
    }
}
