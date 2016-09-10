package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IUpdateable

/**
 * Created by Paha on 8/13/2016.
 */
class Asteroid(val position: Vector2, val radius:Float) : IDrawable, IUpdateable{
    override fun draw(batch: SpriteBatch) {

    }

    override fun update(delta: Float) {

    }

    override fun fixedUpdate() {

    }
}