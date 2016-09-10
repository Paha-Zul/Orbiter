package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IPhysicsBody
import com.quickbite.spaceslingshot.interfaces.IUniqueID

/**
 * Created by Paha on 8/6/2016.
 */
class Planet(position: Vector2, radius: Int, _gravityRangeRadius: Float, _density: Float, rotation:Float, texture:Texture, val homePlanet:Boolean = false)
    : SpaceBody(position, radius, _gravityRangeRadius, _density), IUniqueID, IPhysicsBody{

    lateinit var sprite:Sprite
    lateinit var ring:Sprite
    override val uniqueID: Long = MathUtils.random(Long.MAX_VALUE)
    override lateinit var body: Body

    init{
        val size = radius*2f
        val ringSize = (radius + _gravityRangeRadius)*2f

        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        sprite = Sprite(texture)
        sprite.setPosition(position.x - size /2f, position.y - size /2f)
        sprite.setSize(size, size)
        sprite.setOrigin(sprite.width/2f, sprite.height/2f)
        sprite.rotation = rotation

        val ringTexture = MyGame.manager["ring", Texture::class.java]
        ringTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        ring = Sprite(TextureRegion(ringTexture))
        ring.setPosition(position.x - ringSize/2f, position.y - ringSize/2f)
        ring.setSize(ringSize.toFloat(), ringSize.toFloat())
        ring.color = Color.GRAY
    }

    override fun fixedUpdate() {

    }

    override fun update(delta: Float) {

    }

    override fun draw(batch: SpriteBatch) {
        sprite.draw(batch)
        ring.draw(batch)
    }

    override fun createBody(){
        val mainFixture = FixtureDef()
        val circle = CircleShape()

        circle.position = Vector2(0f, 0f)
        circle.radius = radius.toFloat()

        mainFixture.shape = circle

//        this.body =
    }
}