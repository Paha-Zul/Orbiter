package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IUpdateable

/**
 * Created by Paha on 8/19/2016.
 */
class LineDraw() : IDrawable, IUpdateable{
    private val startPoint:Vector2 = Vector2()
    private val endPoint:Vector2 = Vector2()
    private var distance = 0f

    private var size = 10
    private var rotation = 0f

    lateinit var texture: TextureRegion

    init{
        val texture = MyGame.manager["dash", Texture::class.java]
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        this.texture = TextureRegion(texture, size, size)
//        this.texture.setRegion(0, 0, height, height)
    }

    override fun draw(batch: SpriteBatch) {
        batch.draw(texture, startPoint.x, startPoint.y, 0f, 0f, distance, size.toFloat(), 1f, 1f, rotation)
    }

    override fun update(delta: Float) {

    }

    override fun fixedUpdate() {

    }

    fun setStartAndEnd(start:Vector2, end:Vector2){
        startPoint.set(start.x, start.y)
        endPoint.set(end.x, end.y)

        rotation = MathUtils.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x)*MathUtils.radiansToDegrees

        distance = startPoint.dst(endPoint)

        this.texture.setRegion(0f, 0f, distance/ size, 1f)
    }
}