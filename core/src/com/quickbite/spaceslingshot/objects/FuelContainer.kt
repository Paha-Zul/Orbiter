package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.util.BodyData
import com.quickbite.spaceslingshot.util.Constants

/**
 * Created by Paha on 1/29/2017.
 */
class FuelContainer(val position: Vector2, val fuel:Int) : IDrawable, IUpdateable, Disposable, IPhysicsBody, IUniqueID {

    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body
    override var physicsArePaused: Boolean = false
    override var dead: Boolean = false
    val sprite:Sprite
    var rotation = 0f

    val width = 32f
    val height = 16f

    init {
        sprite = Sprite(MyGame.manager["fuelContainer", Texture::class.java])
        sprite.setSize(width, height)

        sprite.setCenter(sprite.width*0.5f, sprite.height*0.5f)
        sprite.setOrigin(sprite.width*0.5f, sprite.height*0.5f)
        sprite.setPosition(position.x - sprite.width*0.5f, position.y - sprite.height*0.5f)

        createBody()
    }

    override fun update(delta: Float) {

    }

    override fun fixedUpdate(delta: Float) {
        if(physicsArePaused)
            return

        rotation += delta*100f
        body.setTransform((position.x)*Constants.BOX2D_SCALE, (position.y)*Constants.BOX2D_SCALE, rotation*MathUtils.degreesToRadians)
        sprite.setPosition(position.x - sprite.width*0.5f, position.y - sprite.height*0.5f)
        sprite.rotation = rotation
    }

    override fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
    }

    override fun createBody() {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(position.x * Constants.BOX2D_SCALE, position.y* Constants.BOX2D_SCALE)

        val world = MyGame.world
        this.body = world.createBody(bodyDef)

        //Create the main circle on the body.
        val mainFixture = FixtureDef()
        val shape = PolygonShape()

        shape.setAsBox((sprite.width/2f)*Constants.BOX2D_SCALE, (sprite.height/2f)*Constants.BOX2D_SCALE)

        mainFixture.shape = shape
        mainFixture.isSensor = true

        this.body.createFixture(mainFixture)

        shape.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.FuelContainer, this.uniqueID, this, this)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
        this.physicsArePaused = pausePhysics
    }

    override fun dispose() {
        MyGame.world.destroyBody(this.body)
    }

}