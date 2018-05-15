package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID
import com.quickbite.spaceslingshot.util.BodyData
import com.quickbite.spaceslingshot.util.Constants

/**
 * Created by Paha on 8/6/2016.
 * @param position The position of the planet
 * @param radius The radius/size of the planet
 * @param gravityRangeRadius The radius of the gravity well
 * @param density The density of the planet. This affects gravity strength and should be between 0.1 and 1
 * @param texture The texture to use for the planet
 */
class Planet(position: Vector2, radius: Int, var gravityRangeRadius: Float, var density: Float, rotation:Float, texture:Texture)
    : SpaceBody(position, radius, rotation), IUniqueID, IPhysicsBody, Disposable{

    override var physicsArePaused: Boolean = false
    var sprite:Sprite
    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body

    private val gravityRing:TextureRegionDrawable

    val gravityRange:Float
        get() = size + gravityRangeRadius

    fun getPull(dst:Float):Float{
        val normalValue = 1f - (dst/gravityRange)
        return normalValue*density
    }

    init{
        val size = radius*2f

        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        sprite = Sprite(texture)
        sprite.setPosition(position.x - size /2f, position.y - size /2f)
        sprite.setSize(size, size)
        sprite.setOrigin(sprite.width/2f, sprite.height/2f)
        sprite.rotation = rotation

        val ringTexture = MyGame.gameScreenAtlas.findRegion("gravityCircle")
        gravityRing = TextureRegionDrawable(TextureRegion(ringTexture))

        createBody()
    }

    override fun fixedUpdate(delta: Float) {

    }

    override fun update(delta: Float) {

    }

    override fun draw(batch: SpriteBatch) {
        sprite.setPosition(position.x - size, position.y - size)
        sprite.draw(batch)
    }

    override fun draw2(batch: SpriteBatch) {
        drawRing(batch)
    }

    fun drawRing(batch:SpriteBatch){
        val ringSize = (size + gravityRangeRadius)*2f
        gravityRing.draw(batch, position.x - ringSize/2f, position.y - ringSize/2f, ringSize, ringSize)
    }

    override fun createBody(){
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.StaticBody
        bodyDef.position.set(position.x*Constants.BOX2D_SCALE, position.y*Constants.BOX2D_SCALE)

        this.body = MyGame.world.createBody(bodyDef)
        MyGame.world.createBody(bodyDef)

        //Create the main circle on the body.
        val mainFixture = FixtureDef()
        val circle = CircleShape()

        circle.position = Vector2(0f, 0f)
        circle.radius = size.toFloat()*Constants.BOX2D_SCALE

        mainFixture.shape = circle

        this.body.createFixture(mainFixture)

        circle.dispose()

        //Create the ring sensor
        val secondaryFixture = FixtureDef()
        val secondCircle = CircleShape()

        secondCircle.position = Vector2(0f, 0f)
        secondCircle.radius = (size + gravityRangeRadius + Constants.GRAVITYRING_BONUS)*Constants.BOX2D_SCALE

        secondaryFixture.shape = secondCircle
        secondaryFixture.isSensor = true

        this.body.createFixture(secondaryFixture)

        secondCircle.dispose()

        this.body.userData = BodyData(BodyData.ObjectType.Planet, this.uniqueID, this, this)
    }

    override fun dispose() {
        MyGame.world.destroyBody(this.body)
    }

    override fun setPhysicsPaused(pausePhysics: Boolean) {
        //Nothing for now
    }


}