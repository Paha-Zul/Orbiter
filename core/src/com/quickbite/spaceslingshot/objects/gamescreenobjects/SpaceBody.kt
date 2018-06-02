package com.quickbite.spaceslingshot.objects.gamescreenobjects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.util.Constants

/**
 * Created by Paha on 8/12/2016.
 */
open class SpaceBody(val position: Vector2, val size: Int, rotation:Float) : IDrawable, IUpdateable {

    companion object {
        protected val rect = Rectangle()
    }

    open var rotation = rotation

    lateinit var body:Body
    var physicsArePaused = false

    override var dead: Boolean = false

    override fun draw(batch: SpriteBatch) {

    }

    open fun draw2(batch:SpriteBatch){

    }

    override fun update(delta: Float) {
    }

    override fun fixedUpdate(delta: Float) {

    }

    override fun dispose() {

    }

    /**
     * Sets the physics body's position. The x and y values should be unadjusted values
     * @param x The (unadjusted) x position which will be scaled for physics
     * @param y The (unadjusted) y position which will be scaled for physics
     */
    open fun setPhysicsPosition(x:Float, y:Float){
        body.setTransform(x* Constants.BOX2D_SCALE,y*Constants.BOX2D_SCALE, rotation)
    }

    open fun clickedOn(x:Float, y:Float):Boolean{
        rect.set(position.x - size.toFloat()/2f, position.y - size.toFloat()/2f, size.toFloat(), size.toFloat())
        return rect.contains(x, y)
    }

    open fun createBody(){

    }
}