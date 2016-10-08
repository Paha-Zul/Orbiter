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
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.util.BodyData
import com.quickbite.spaceslingshot.util.Constants
import com.quickbite.spaceslingshot.util.EventSystem

/**
 * Created by Paha on 9/23/2016.
 */

class SpaceStation(position: Vector2, size:Int, val fuelStorage:Float, val homeStation:Boolean = false):SpaceBody(position, size, 0f, 0f), IUniqueID, IPhysicsBody, Disposable{
    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body
    override var physicsArePaused: Boolean = false

    var sprite: Sprite

    init{
        sprite = Sprite(MyGame.manager["station", Texture::class.java])
        sprite.setSize(radius*2f, radius*2f)
        sprite.setPosition(position.x - radius, position.y - radius)

        EventSystem.onEvent("hit_station", { args ->
            val ship = args[0] as Ship

            //If the ship is a test ship, ignore
            if(ship.testShip) return@onEvent

            //If the ship's velocity is too great, lose!
            if(ship.velocity.x > 0.5f || ship.velocity.y > 0.5f){
                GameScreen.finished = true
                GameScreen.lost = true
                return@onEvent
            }

            //If it is the home station and we didn't go too fast, win!
            if(homeStation){
                GameScreen.finished = true
                GameScreen.lost = false
                return@onEvent
            }

            //Otherwise, attach to the station.

            val angle = MathUtils.atan2(ship.position.y - this.position.y, ship.position.x - this.position.x)

            //set the ship position
            val x = radius * MathUtils.cos(angle) - MathUtils.sin(angle) //Original X position
            val y = radius * MathUtils.sin(angle) +  MathUtils.cos(angle) //Original Y position
            ship.setPosition(position.x + x, position.y + y)

            //Set the ship rotation
            ship.setShipRotation(angle*MathUtils.radiansToDegrees)
            ship.setVelocity(0f, 0f)
            ship.thrusters.forEach { thruster -> thruster.burnTime = 0 }

        }, this.uniqueID)

        this.createBody()
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
        mainFixture.isSensor = true

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