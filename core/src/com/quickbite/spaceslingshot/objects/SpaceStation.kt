package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Timer
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.util.BodyData
import com.quickbite.spaceslingshot.util.Constants
import com.quickbite.spaceslingshot.util.CustomTimer
import com.quickbite.spaceslingshot.util.EventSystem

/**
 * Created by Paha on 9/23/2016.
 */

class SpaceStation(position: Vector2, size:Int, var fuelStorage:Float, val rotation:Float, val homeStation:Boolean = false):SpaceBody(position, size, 0f, 0f), IUniqueID, IPhysicsBody, Disposable{

    companion object{
        val arrowOffsets:List<Vector2> = listOf(
                Vector2(80f, 0f),
                Vector2(70f, 0f),
                Vector2(60f, 0f)
        )

        val dockingOffset = Vector2(70f, 0f)
    }

    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body
    override var physicsArePaused: Boolean = false

    val secondsToRefuel = 2f
    val fuelRechargeAmountPerTick = fuelStorage/(60f*secondsToRefuel) //Assuming 60 ticks per second

    var sprite: Sprite
    val arrow:Sprite = Sprite(MyGame.gameScreenAtlas.findRegion("arrow"))

    var currArrowSpot:Vector2 = Vector2(arrowOffsets[0])
    var currArrowCounter = 0
    val timer = CustomTimer(0.25f, false, {
        currArrowCounter = (currArrowCounter+1)% arrowOffsets.size
        setArrowOffset(arrowOffsets[currArrowCounter])
    })

    val dstToDock = 20f

    init{
//        sprite = Sprite(MyGame.manager["station", Texture::class.java])
        sprite = Sprite(MyGame.gameScreenAtlas.findRegion("station"))
        sprite.setSize(radius*2f, radius*2f)
        sprite.setPosition(position.x - radius, position.y - radius)
        sprite.setOrigin(radius.toFloat(), radius.toFloat())
        sprite.rotation = rotation

        arrow.setSize(32f, 32f)
        arrow.setOrigin(arrow.width/2f, arrow.height/2f)
        arrow.color = Color.GREEN
        arrow.rotation = this.rotation + 180f

        EventSystem.onEvent("hit_station", { args ->
            val ship = args[0] as Ship

            //If the ship is a test ship, ignore
            if(ship.testShip) return@onEvent

            //If the ship is not docking in the right spot, lose!
            //TODO Make this an explosion!
            if(!checkCloseToDocking(ship.position, dstToDock)){
                GameScreen.setGameOver(true)
                return@onEvent
            }

            //If the ship's velocity is too great, lose!
            //TODO Make this an explosion!
            if(ship.velocity.x > 0.5f || ship.velocity.y > 0.5f){
                GameScreen.setGameOver(true)
                return@onEvent
            }

            //If we made it here, we docked well!
            GameScreen.pauseTimer = true

            //If we approached well, set the ship to docking!
            val dockingPos = getDockingPosition()
            ship.setDocking(Vector2(dockingPos.x, dockingPos.y), rotation, {
                if(homeStation){
                    GameScreen.setGameOver(false)
                }
            })

            if(!homeStation) {
                //Set a timer to refuel
                Timer.schedule(object : Timer.Task() {
                    override fun run() {
                        //Cancel the timer when we have no more fuel or the ship is full.
                        if (this@SpaceStation.fuelStorage <= 0 || ship.addFuel(fuelRechargeAmountPerTick))
                            this.cancel()

                        this@SpaceStation.fuelStorage -= fuelRechargeAmountPerTick
                    }
                }, 0f, 0.016f)
            }

        }, this.uniqueID)

        this.createBody()

        setArrowOffset(arrowOffsets[currArrowCounter])
        timer.start()
    }

    override fun draw(batch: SpriteBatch) {
        timer.update(0.016f)
        arrow.setPosition(currArrowSpot.x - arrow.width/2f, currArrowSpot.y - arrow.height/2f)
        arrow.draw(batch)

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
        circle.radius = (radius/2f) * Constants.BOX2D_SCALE

        mainFixture.shape = circle
        mainFixture.isSensor = true

        this.body.createFixture(mainFixture)

        circle.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Station, this.uniqueID, this, this)
    }

    private fun setArrowOffset(arrowOffset:Vector2){
        val angle = rotation*MathUtils.degreesToRadians
        val x = arrowOffset.x * MathUtils.cos(angle) - arrowOffset.y*MathUtils.sin(angle) //Original X position
        val y = arrowOffset.x * MathUtils.sin(angle) + arrowOffset.y*MathUtils.cos(angle) //Original Y position

        currArrowSpot.set(this.position.x + x, this.position.y + y)
    }

    private fun checkCloseToDocking(otherPosition:Vector2, range:Float):Boolean{
        val angle = rotation*MathUtils.degreesToRadians
        val x = dockingOffset.x * MathUtils.cos(angle) - dockingOffset.y*MathUtils.sin(angle) //Original X position
        val y = dockingOffset.x * MathUtils.sin(angle) + dockingOffset.y*MathUtils.cos(angle) //Original Y position

        val dst = otherPosition.dst(this.position.x + x, this.position.y + y)
        if(dst <= range)
            return true

        return false
    }

    private fun getDockingPosition():Vector2{
        val angle = rotation*MathUtils.degreesToRadians
        val x = dockingOffset.x * MathUtils.cos(angle) - dockingOffset.y*MathUtils.sin(angle) //Original X position
        val y = dockingOffset.x * MathUtils.sin(angle) + dockingOffset.y*MathUtils.cos(angle) //Original Y position

        return Vector2(this.position.x + x, this.position.y + y)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {

    }

    override fun dispose() {
        MyGame.world.destroyBody(this.body)
    }
}