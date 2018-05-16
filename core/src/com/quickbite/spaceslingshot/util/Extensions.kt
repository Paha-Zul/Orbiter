package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener

/**
 * Created by Paha on 8/6/2016.
 */

fun Vector2.translate(x:Float, y:Float){
    this.x += x
    this.y += y
}

fun Color.set(r:Int, g:Int, b:Int, a:Int){
    this.set(MathUtils.clamp(r/255f, 0f, 1f),
            MathUtils.clamp(g/255f, 0f, 1f),
            MathUtils.clamp(b/255f, 0f, 1f),
            MathUtils.clamp(a/255f, 0f, 1f))
}

fun Color.set(r:Int, g:Int, b:Int){
    this.set(r, g, b, 255)
}

fun TextField.onEnterListener(listener:()->Unit){
    this.addListener(object:InputListener(){
        override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
            if(keycode == Input.Keys.ENTER)
                listener()
            return super.keyDown(event, keycode)
        }
    })
}

fun TextField.onLeaveFieldOrEnter(listener:()->Unit){
    this.addListener(object:InputListener(){
        override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
            if(keycode == Input.Keys.ENTER)
                listener()
            return super.keyDown(event, keycode)
        }
    })

    this.addListener(object:FocusListener(){
        override fun keyboardFocusChanged(event: FocusEvent?, actor: Actor?, focused: Boolean) {
            if(!focused)
                listener()
            super.keyboardFocusChanged(event, actor, focused)
        }
    })
}

fun Double.format(digits: Int):String = java.lang.String.format("%.${digits}f", this)
fun Float.format(digits: Int):String = java.lang.String.format("%.${digits}f", this)