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
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.util.BodyData
import com.quickbite.spaceslingshot.util.Constants
import com.quickbite.spaceslingshot.util.EventSystem

/**
 * Created by Paha on 9/23/2016.
 */

class SpaceStation(position: Vector2, size:Int, fuelStorage:Float):SpaceBody(position, size, 0f, 0f), IUniqueID, IPhysicsBody, Disposable{
    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body
    override var physicsArePaused: Boolean = false

    var sprite: Sprite

    init{
        sprite = Sprite(MyGame.manager["circle", Texture::class.java])
        sprite.setPosition(position.x - radius / 2, position.y - radius / 2)

        EventSystem.onEvent("hit_station", { args ->
            val ship = args[0] as Ship

            if(!ship.testShip) return@onEvent

            val angle = MathUtils.atan2(this.position.y - ship.position.y, this.position.x - ship.position.y)
            ship.rotation = angle*MathUtils.radiansToDegrees

            val x = size * MathUtils.cos(angle) - MathUtils.sin(angle) //Original X position
            val y = size * MathUtils.sin(angle) + MathUtils.cos(angle) //Original Y position

            ship.setPosition(x, y)

        }, this.uniqueID)
    }

    override fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
    }

    override fun createBody() {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
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

        this.body.userData = BodyData(BodyData.ObjectType.Station, this.uniqueID, this)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
    }

    override fun dispose() {
        MyGame.world.destroyBody(this.body)
    }
}