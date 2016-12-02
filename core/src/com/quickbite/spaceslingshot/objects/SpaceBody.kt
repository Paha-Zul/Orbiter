package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IUpdateable

/**
 * Created by Paha on 8/12/2016.
 */
open class SpaceBody(val position: Vector2, val radius: Int, protected val _gravityRangeRadius: Float, protected val _density: Float) : IDrawable, IUpdateable {
    override var dead: Boolean = false

    val gravityRange:Float
        get() = radius + _gravityRangeRadius

    fun getPull(dst:Float):Float{
//        val multiplier = (1.1f - (dst/gravityRange))*20
//        val logValue = (MathUtils.log2(multiplier)*_density)/20f
        val normalValue = 1.1f - (dst/gravityRange)
        return normalValue*_density
    }

    override fun draw(batch: SpriteBatch) {

    }

    override fun update(delta: Float) {

    }

    override fun fixedUpdate(delta: Float) {

    }

    override fun dispose() {

    }
}