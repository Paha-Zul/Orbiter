package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.graphics.Texture

/**
 * Created by Paha on 9/3/2016.
 */
class TextureGeneratorRunnable(val function:() -> Texture, val onFinishedCallback:(texture:Texture) -> Unit) : Runnable{
    override fun run() {
        System.out.println("Running??!?!?!")
        val texture = function()
        onFinishedCallback(texture)
    }
}