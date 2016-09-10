package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.util.GH
import com.quickbite.spaceslingshot.util.translate

/**
 * Created by Paha on 8/6/2016.
 */
class Ship(val position:Vector2, var fuel:Float, initialVelocity:Vector2, val testShip:Boolean = false): IUpdateable, IDrawable {
    val velocity = initialVelocity
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


    lateinit var sprite:Sprite
    lateinit var ring:Sprite
    lateinit var burnBall:Sprite

    val ringRadius = 100f
    private val shipWidth = 20f
    private val shipHeight = 10f
    private val burnBallRadius = 30f

    val burnBallPosition = Vector2()
    val burnBallBasePosition = Vector2()

    lateinit var normalBurnTexture:Texture
    lateinit var doubleBurnTexture:Texture

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

            burnBall = Sprite(normalBurnTexture)
            burnBall.setPosition(position.x - burnBallRadius, position.y - burnBallRadius)
            burnBall.setSize(burnBallRadius * 2f, burnBallRadius * 2f)
            burnBall.setOrigin(burnBall.width/2f, burnBall.height/2f)
            burnBall.color = Color.WHITE

            setRotationTowardsMouse(0f, 0f)
        }
    }

    //Empty constructor
    constructor():this(Vector2(0f, 0f), 0f, Vector2(0f, 0f), false)

    override fun update(delta: Float) {
        burnFuel()
        position.translate(velocity.x, velocity.y)

        if(!testShip) {
            sprite.setPosition(position.x - shipWidth / 2f, position.y - shipHeight / 2f)
            ring.setPosition(position.x - ringRadius, position.y - ringRadius)
            burnBall.setPosition(position.x - burnBallRadius, position.y - burnBallRadius)
        }
    }

    override fun fixedUpdate() {

    }

    override fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
    }

    fun drawHandles(batch: SpriteBatch){
        setBurnBallLocation()

        ring.draw(batch)
        burnBall.draw(batch)
    }

    fun addVelocity(x:Float, y:Float){
        velocity.translate(x, y)
    }

    fun addVelocityForward(force:Float){
        val angle = rotation*MathUtils.degreesToRadians
        val x = MathUtils.cos(angle)*force
        val y = MathUtils.sin(angle)*force
        addVelocity(x, y)
    }

    fun setVelocity(x:Float, y:Float){
        velocity.set(x, y)
    }

    fun setRotationTowardsMouse(mouseX:Float, mouseY:Float){
        val angle = MathUtils.atan2(mouseY - position.y, mouseX - position.x)
        this.rotation = angle*MathUtils.radiansToDegrees
        sprite.rotation = angle*MathUtils.radiansToDegrees
        burnBall.rotation = angle*MathUtils.radiansToDegrees
        ring.rotation = angle*MathUtils.radiansToDegrees - 90 //Give an offset of 90 so the arrow doesn't sit under the burn ball

        setBurnBallLocation()
    }

    private fun setBurnBallLocation(){
        val angle = rotation*MathUtils.degreesToRadians
        val x = ringRadius * MathUtils.cos(angle) - MathUtils.sin(angle) //Original X position of the burn ball
        val y = ringRadius * MathUtils.sin(angle) + MathUtils.cos(angle) //Original Y position of the burn ball
        val x2 = (ringRadius + burnTime) * MathUtils.cos(angle) - MathUtils.sin(angle) //Adjusted X position using the burn time.
        val y2 = (ringRadius + burnTime) * MathUtils.sin(angle) + MathUtils.cos(angle) //Adjusted Y position using the burn time.

        burnBallBasePosition.set(position.x + x, position.y + y)

        burnBallPosition.set(position.x + x2, position.y + y2 )
        burnBall.setPosition(burnBallPosition.x - burnBallRadius, burnBallPosition.y - burnBallRadius)
    }

    private fun burnFuel(){
        if(fuel <= 0 || burnTime <= 0)
            return

        addVelocityForward(burnForce)

        fuel-=burnPerTick
        burnTime--
    }

    fun clickOnShip(mouseX:Float, mouseY:Float):Int{
        val dst = position.dst(mouseX, mouseY)
        val dstToBurnBall = burnBallPosition.dst(mouseX, mouseY)
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
            true -> {
                if(!testShip) {
                    burnBall.texture = doubleBurnTexture
                    burnBall.color = Color.RED
                }
                burnForce *= 2f
                burnPerTick *= 2f
            }
            false ->{
                if(!testShip) {
                    burnBall.texture = normalBurnTexture
                    burnBall.color = Color.WHITE
                }
                burnForce /= 2f
                burnPerTick /= 2f
            }
        }

        if(burnAmount > fuel){
            burnTime = (fuel/burnPerTick).toInt()
        }

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
}