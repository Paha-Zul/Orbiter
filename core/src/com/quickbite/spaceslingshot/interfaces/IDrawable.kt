package com.quickbite.spaceslingshot.interfaces

import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * Created by Paha on 8/6/2016.
 */
interface IDrawable : IKillable{
    fun draw(batch: SpriteBatch)
}