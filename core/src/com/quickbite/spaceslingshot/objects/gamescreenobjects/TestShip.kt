package com.quickbite.spaceslingshot.objects.gamescreenobjects

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.objects.BodyData
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.util.Constants
import com.quickbite.spaceslingshot.util.EventSystem

class TestShip(position:Vector2, fuel:Float) : ShipBase(position, fuel) {
    val planetList = mutableListOf<Planet>()

    constructor():this(Vector2(0f, 0f), 0f)

    init{
        EventSystem.onEvent("collide_begin", { args ->
            val other = args[0] as Fixture
            val otherData = other.body.userData as BodyData

            //If the other fixture is a sensor and it's body belongs to a planet, we are in the gravity well
            if(other.isSensor && otherData.type == BodyData.ObjectType.Planet){
                val planet = otherData.bodyOwner as Planet
                planetList.add(planet)
            }

        }, this.uniqueID)

        //On collide_end, remove the planet from the list
        EventSystem.onEvent("collide_end", { args ->
            val other = args[0] as Fixture
            val otherData = other.body.userData as BodyData

            if(other.isSensor && otherData.type == BodyData.ObjectType.Planet){
                val planet = otherData.bodyOwner as Planet
                planetList.remove(planet)
            }

        }, this.uniqueID)
    }

    override fun fixedUpdate(delta: Float) {
        super.fixedUpdate(delta)
        testShipSimulation()
    }

    override fun createBody() {
        val bodyDef = BodyDef()
//        bodyDef.type = if(testShip) BodyDef.BodyType.DynamicBody else BodyDef.BodyType.KinematicBody
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(position.x* Constants.BOX2D_SCALE, position.y* Constants.BOX2D_SCALE)
        bodyDef.allowSleep = false

        val world = MyGame.world
        this.body = world.createBody(bodyDef)

        //Create the main circle on the body.
        val mainFixture = FixtureDef()
        val shape = PolygonShape()

        shape.setAsBox((Constants.SHIP_SIZE/2f)* Constants.BOX2D_SCALE, (Constants.SHIP_SIZE/2f)* Constants.BOX2D_SCALE)

        mainFixture.shape = shape
        mainFixture.isSensor = true

        this.body.createFixture(mainFixture)

        shape.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Ship, this.uniqueID, this, this)
    }

    private fun testShipSimulation(){
        burnFuel()
        planetList.forEach { planet ->
            val dst = planet.position.dst(this.position)
            if (dst <= planet.gravityRange)
                GameScreen.applyGravity(planet, this)
        }
        position.set(body.position.x * Constants.BOX2D_INVERSESCALE, body.position.y * Constants.BOX2D_INVERSESCALE)
        velocity.set(body.linearVelocity.x*Constants.BOX2D_SCALE, body.linearVelocity.y*Constants.BOX2D_SCALE)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
        if(physicsArePaused == pausePhysics) return //If we are not changing the state, simply return

        when(pausePhysics){
        //If we are pausing, remove velocity and store it away.
            true -> {
                velocityHolder.set( body.linearVelocity.x,  body.linearVelocity.y)
                body.setLinearVelocity(0f, 0f)
            }
        //Otherwise, set the velocity back
            false -> {
                body.setLinearVelocity(velocityHolder.x, velocityHolder.y)
                velocityHolder.set(0f, 0f)
            }
        }

        physicsArePaused = pausePhysics
    }
}