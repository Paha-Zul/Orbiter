package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.util.BodyData
import com.quickbite.spaceslingshot.util.Constants

/**
 * Created by Paha on 8/13/2016.
 */
class Asteroid(val position: Vector2, val radius:Float, val velocity:Vector2) : IDrawable, IUpdateable, Disposable, IPhysicsBody, IUniqueID{
    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)

    override lateinit var body: Body

    init{
        this.createBody()
    }

    override fun draw(batch: SpriteBatch) {

    }

    override fun update(delta: Float) {

    }

    override fun fixedUpdate() {

    }

    override fun dispose() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createBody() {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(position.x* Constants.BOX2D_SCALE, position.y* Constants.BOX2D_SCALE)

        val world = MyGame.world
        this.body = world.createBody(bodyDef)

        //Create the main circle on the body.
        val mainFixture = FixtureDef()
        val circle = CircleShape()

        circle.position = Vector2(0f, 0f)
        circle.radius = radius * Constants.BOX2D_SCALE

        mainFixture.shape = circle

        this.body.createFixture(mainFixture)

        circle.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Ship, this.uniqueID, this)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}