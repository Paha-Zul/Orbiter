package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.util.GH

/**
 * Created by Paha on 8/9/2016.
 */
class Obstacle(val rect:Rectangle) :IDrawable, Disposable{
    var sprite:Sprite

    init{
        sprite = Sprite(GH.createPixel(Color.WHITE))
        sprite.setPosition(rect.x, rect.y)
        sprite.setSize(rect.width, rect.height)
    }

    override fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
    }

    override fun dispose() {

    }
}