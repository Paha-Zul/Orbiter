package com.quickbite.spaceslingshot.interfaces

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.quickbite.spaceslingshot.util.IKillable

/**
 * Created by Paha on 8/6/2016.
 */
interface IDrawable : IKillable{
    fun draw(batch: SpriteBatch)
}