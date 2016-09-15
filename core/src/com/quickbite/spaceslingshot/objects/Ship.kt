package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.util.BodyData
import com.quickbite.spaceslingshot.util.Constants
import com.quickbite.spaceslingshot.util.EventSystem
import com.quickbite.spaceslingshot.util.GH
import java.util.*

/**
 * Created by Paha on 8/6/2016.
 * @param position The real 'world' coordinates of the ship. These will be used for everything (including rendering) except Box2D which is scaled.
 */
class Ship(val position:Vector2, var fuel:Float, initialVelocity:Vector2, val testShip:Boolean = false): IUpdateable, IDrawable, IUniqueID, IPhysicsBody, Disposable {
    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body

    private val planetList:LinkedList<Planet> = LinkedList()

    private val velocityHolder = Vector2()
    var rotation = 0f

    var burnTime = 0 //Burn for 10 ticks

    var burnPerTick = 0.1f //The amount of fuel burned per tick
        get
        private set

    var burnForce:Float = 0.01f //The amount of burn force per tick
        get
        private set

    val burnAmount:Float
        get() = burnTime*burnPerTick

    var doubleBurn = false
        private set

    val velocity:Vector2
        get() = Vector2(body.linearVelocity.x*Constants.VELOCITY_INVERSESCALE, body.linearVelocity.y*Constants.VELOCITY_INVERSESCALE)

    override var physicsArePaused = false

    private lateinit var sprite:Sprite
    private lateinit var ring:Sprite
    private lateinit var burnHandle:Sprite

    private val ringRadius = 100f
    private val shipWidth = 20f
    private val shipHeight = 10f
    private val burnBallRadius = 30f

    val burnHandleLocation = Vector2()
    val burnBallBasePosition = Vector2()

    private lateinit var normalBurnTexture:Texture
    private lateinit var doubleBurnTexture:Texture

    init{
        if(!testShip) {
            normalBurnTexture = MyGame.manager["arrow", Texture::class.java]
            normalBurnTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            doubleBurnTexture = MyGame.manager["doubleArrow", Texture::class.java]
            doubleBurnTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

            sprite = Sprite(GH.createPixel(Color.WHITE, shipWidth.toInt(), shipHeight.toInt()))
            sprite.setPosition(position.x - shipWidth / 2, position.y - shipHeight / 2)

            val ringTexture = MyGame.manager["arrowCircle", Texture::class.java]
            ringTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

            ring = Sprite(ringTexture)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            ring.setSize(ringRadius * 2f, ringRadius * 2f)
            ring.color = Color.WHITE
            ring.setOrigin(ringRadius, ringRadius)

            burnHandle = Sprite(normalBurnTexture)
            burnHandle.setPosition(position.x - burnBallRadius, position.y - burnBallRadius)
            burnHandle.setSize(burnBallRadius * 2f, burnBallRadius * 2f)
            burnHandle.setOrigin(burnHandle.width/2f, burnHandle.height/2f)
            burnHandle.color = Color.WHITE

            setRotationTowardsMouse(0f, 0f)

            EventSystem.onEvent("collide_begin", { args ->
                val other = args[0] as Fixture
                val otherData = other.body.userData as BodyData

                if(!other.isSensor && otherData.type == BodyData.ObjectType.Planet) {
                    val planet = otherData.bodyOwner as Planet
                    GameScreen.finished = true
                    GameScreen.lost = !planet.homePlanet
                }

                //If the other fixture is a sensor and it's body belongs to a planet, we are in the gravity well
                else if(other.isSensor && otherData.type == BodyData.ObjectType.Planet){
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

        //If we are the test ship
        }else{
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

        this.createBody()
    }

    //Empty constructor
    constructor():this(Vector2(0f, 0f), 0f, Vector2(0f, 0f), false)

    override fun update(delta: Float) {

        if(!testShip) {
            sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            burnHandle.setPosition(position.x - burnBallRadius, position.y - burnBallRadius)
        }
    }

    override fun fixedUpdate() {
        if(!physicsArePaused) {
            burnFuel()
            planetList.forEach { planet -> GameScreen.applyGravity(planet, this) }
            position.set(body.position.x*Constants.BOX2D_INVERSESCALE, body.position.y*Constants.BOX2D_INVERSESCALE)
        }
    }

    override fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
    }

    fun drawHandles(batch: SpriteBatch){
        setBurnHandleLocation()

        ring.draw(batch)
        burnHandle.draw(batch)
    }

    fun addVelocity(x:Float, y:Float){
        body.setLinearVelocity(body.linearVelocity.x + x*Constants.VELOCITY_SCALE, body.linearVelocity.y + y*Constants.VELOCITY_SCALE)
}

    fun addVelocityForward(force:Float){
        val angle = rotation*MathUtils.degreesToRadians
        val x = MathUtils.cos(angle)*force
        val y = MathUtils.sin(angle)*force
        addVelocity(x, y)
    }

    /**
     * Sets the velocity of the ship
     * @param x The X velocity
     * @param y The Y velocity
     */
    fun setVelocity(x:Float, y:Float){
        body.setLinearVelocity(x*Constants.VELOCITY_SCALE, y*Constants.VELOCITY_SCALE)
    }

    fun setRotationTowardsMouse(mouseX:Float, mouseY:Float){
        val angle = MathUtils.atan2(mouseY - position.y, mouseX - position.x)
        this.rotation = angle*MathUtils.radiansToDegrees
        sprite.rotation = angle*MathUtils.radiansToDegrees
        burnHandle.rotation = angle*MathUtils.radiansToDegrees
        ring.rotation = angle*MathUtils.radiansToDegrees - 90 //Give an offset of 90 so the arrow doesn't sit under the burn ball

        setBurnHandleLocation()
    }

    private fun setBurnHandleLocation(){
        val angle = rotation*MathUtils.degreesToRadians
        val x = ringRadius * MathUtils.cos(angle) - MathUtils.sin(angle) //Original X position of the burn ball
        val y = ringRadius * MathUtils.sin(angle) + MathUtils.cos(angle) //Original Y position of the burn ball
        val x2 = (ringRadius + burnTime) * MathUtils.cos(angle) - MathUtils.sin(angle) //Adjusted X position using the burn time.
        val y2 = (ringRadius + burnTime) * MathUtils.sin(angle) + MathUtils.cos(angle) //Adjusted Y position using the burn time.

        burnBallBasePosition.set(position.x + x, position.y + y)

        burnHandleLocation.set(position.x + x2, position.y + y2 )
        burnHandle.setPosition(burnHandleLocation.x - burnBallRadius, burnHandleLocation.y - burnBallRadius)
    }

    private fun burnFuel(){
        if(fuel <= 0 || burnTime <= 0)
            return

        addVelocityForward(burnForce)

        fuel -= burnPerTick
        burnTime--
    }

    fun clickOnShip(mouseX:Float, mouseY:Float):Int{
        val dst = position.dst(mouseX, mouseY)
        val dstToBurnBall = burnHandleLocation.dst(mouseX, mouseY)
        if(dstToBurnBall <= burnBallRadius)
            return 2
        else if(dst <= ringRadius){
            return 1
        }

        return 0
    }

    /** Toggles the double burn of the ship
     * @return True if the double burn is activated, false otherwise.
     */
    fun toggleDoubleBurn():Boolean{
        this.doubleBurn = !this.doubleBurn
        return setDoubleBurn(doubleBurn)
    }

    fun setDoubleBurn(burnValue:Boolean):Boolean{
        if(burnValue == !this.doubleBurn) return doubleBurn

        when(burnValue){
            //If we are double burning, set the texture and apply the bonus burn.
            true -> {
                if(!testShip) {
                    burnHandle.texture = doubleBurnTexture
                    burnHandle.color = Color.RED
                }
                burnForce *= 2f
                burnPerTick *= 2f
            }
            //If we are not double burning, set the texture and reduce our burn.
            false ->{
                if(!testShip) {
                    burnHandle.texture = normalBurnTexture
                    burnHandle.color = Color.WHITE
                }
                burnForce /= 2f
                burnPerTick /= 2f
            }
        }

        //If we don't have enough fuel, clamp it
        if(burnAmount > fuel)
            burnTime = (fuel/burnPerTick).toInt()

        return doubleBurn
    }

    fun setBurnForceAndPerTick(force:Float, amount:Float){
        this.burnForce = force
        this.burnPerTick = amount
    }

    fun dragBurn(mouseX:Float, mouseY:Float){
        var dst = position.dst(mouseX, mouseY) - ringRadius
        if(dst <= 0) dst = 0f

        burnTime = dst.toInt()
        if(burnAmount > fuel){
            burnTime = (fuel/burnPerTick).toInt()
        }
    }

    fun reset(position:Vector2, fuel:Float, initialVelocity:Vector2){
        this.position.set(position.x, position.y)
        this.fuel = fuel
        this.setDoubleBurn(false)
        this.rotation = 0f
        this.body.setTransform(Vector2(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE), 0f)
        this.body.setLinearVelocity(initialVelocity.x*Constants.VELOCITY_SCALE, initialVelocity.y*Constants.VELOCITY_SCALE)
        this.planetList.clear()

        //If we are the test ship, don't do this!
        if(!testShip) {
            sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            burnHandle.setPosition(position.x - burnBallRadius, position.y - burnBallRadius)
            setBurnHandleLocation()
        }
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
        circle.radius = 2* Constants.BOX2D_SCALE

        mainFixture.shape = circle
        if(testShip) mainFixture.isSensor = true

        this.body.createFixture(mainFixture)

        circle.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Ship, this.uniqueID, this)
    }

    /**
     * Sets the position of the ship. This sets both the rendering position and the physics body position.
     * @param x The X position (real value)
     * @param y The Y position (real value)
     */
    fun setPosition(x:Float, y:Float){
        this.position.set(x, y)
        this.body.setTransform(x*Constants.BOX2D_SCALE, y*Constants.BOX2D_SCALE, 0f)

        if(!testShip) {
            sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            burnHandle.setPosition(position.x - burnBallRadius, position.y - burnBallRadius)
            setBurnHandleLocation()
        }
    }

    override fun dispose() {
        val world = MyGame.world
        world.destroyBody(this.body)
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