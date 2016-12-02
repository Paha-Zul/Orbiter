package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IUpdateable

/**
 * Created by Paha on 8/19/2016.
 */
class LineDraw(start:Vector2, end:Vector2, texture:Texture) : IDrawable, IUpdateable{
    override var dead: Boolean = false

    private val startPoint:Vector2 = Vector2(start.x, start.y)
    private val endPoint:Vector2 = Vector2(end.x, end.x)
    private var pointList:List<Vector2> = listOf()
    private var distance = 0f

    var size = 10
    private var rotation = 0f
    private val textureRegion:TextureRegion

    constructor(points:List<Vector2>, texture:Texture):this(Vector2(), Vector2(), texture){
        pointList = points.toList()
    }

    init{
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        this.textureRegion = TextureRegion(texture, size, size)
//        this.texture.setRegion(0, 0, height, height)
    }

    override fun draw(batch: SpriteBatch) {
        if(this.pointList.size == 0)
            batch.draw(textureRegion, startPoint.x, startPoint.y, 0f, 0f, distance, size.toFloat(), 1f, 1f, rotation)
        else{
            for(i in 0..pointList.size-2){
                val nextPoint = pointList[i+1]
                val currPoint = pointList[i]

                rotation = MathUtils.atan2(nextPoint.y - currPoint.y, nextPoint.x - currPoint.x)*MathUtils.radiansToDegrees
                distance = currPoint.dst(nextPoint)
                this.textureRegion.setRegion(0f, 0f, distance/ size, 1f)
                batch.draw(textureRegion, currPoint.x, currPoint.y, 0f, 0f, distance, size.toFloat(), 1f, 1f, rotation)
            }

        }
    }

    override fun update(delta: Float) {

    }

    override fun fixedUpdate(delta: Float) {

    }

    fun setStartAndEnd(start:Vector2, end:Vector2){
        startPoint.set(start.x, start.y)
        endPoint.set(end.x, end.y)

        rotation = MathUtils.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x)*MathUtils.radiansToDegrees

        distance = startPoint.dst(endPoint)

        this.textureRegion.setRegion(0f, 0f, distance/ size, 1f)

        //Clear the point list if it has anything in it.
        if(this.pointList.size > 0)
            this.pointList = listOf()
    }

    fun setPoints(points:List<Vector2>){
        this.pointList = points.toList()
    }

    override fun dispose() {

    }
}