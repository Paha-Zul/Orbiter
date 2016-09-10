package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

/**
 * Created by Paha on 8/6/2016.
 */

fun Vector2.translate(x:Float, y:Float){
    this.x += x
    this.y += y
}

fun Color.set(r:Int, g:Int, b:Int, a:Int){
    this.set(MathUtils.clamp(r/255f, 0f, 1f),
            MathUtils.clamp(g/255f, 0f, 1f),
            MathUtils.clamp(b/255f, 0f, 1f),
            MathUtils.clamp(a/255f, 0f, 1f))
}

fun Color.set(r:Int, g:Int, b:Int){
    this.set(r, g, b, 255)
}