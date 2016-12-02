package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
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
class Asteroid(val position: Vector2, val radius:Float, val velocity:Vector2, val lifetimeSeconds:Float) : IDrawable, IUpdateable, Disposable, IPhysicsBody, IUniqueID{
    override var dead: Boolean = false

    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override var physicsArePaused: Boolean = false
    lateinit var sprite:Sprite

    val tempVel:Vector2 = Vector2()

    var lifeCounter = 0f

    override lateinit var body: Body

    init{
        this.createBody()
        sprite = Sprite(MyGame.manager["asteroid", Texture::class.java])
        sprite.setSize(radius*2f, radius*2f)
        sprite.setPosition(position.x - sprite.width/2f, position.y - sprite.height/2f)
        sprite.setOrigin(sprite.width/2f, sprite.height/2f)
    }

    override fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
    }

    override fun update(delta: Float) {
        position.set(body.position.x*Constants.BOX2D_INVERSESCALE, body.position.y*Constants.BOX2D_INVERSESCALE)
        sprite.setPosition(position.x - sprite.width/2f, position.y - sprite.height/2f)

        lifeCounter += delta
        if(lifeCounter >= lifetimeSeconds)
            dead = true

    }

    override fun fixedUpdate(delta: Float) {

    }

    override fun dispose() {
        MyGame.world.destroyBody(this.body)
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
        this.body.setLinearVelocity(velocity.x*Constants.VELOCITY_SCALE, velocity.y*Constants.VELOCITY_SCALE)

        circle.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Ship, this.uniqueID, this)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
        if(physicsArePaused == pausePhysics) return

        if(pausePhysics){
            tempVel.set(body.linearVelocity.x, body.linearVelocity.y)
            body.setLinearVelocity(0f, 0f)
        }else{
            body.setLinearVelocity(tempVel.x, tempVel.y)
            tempVel.set(0f, 0f)
        }

        this.physicsArePaused = pausePhysics
    }


}