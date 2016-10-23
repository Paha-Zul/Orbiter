package com.quickbite.spaceslingshot.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Texture
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.ProceduralPlanetTextureGenerator
import com.quickbite.spaceslingshot.guis.MainMenuGUI
import com.quickbite.spaceslingshot.util.JsonLevelLoader

/**
 * Created by Paha on 8/7/2016.
 */
class MainMenuScreen(val game:MyGame) :Screen{
    lateinit var background:Texture

    override fun show() {
        MainMenuGUI(this)

        JsonLevelLoader.loadLevels()
        Gdx.input.inputProcessor = MyGame.stage

        val music = MyGame.manager["Action_In_Orbit", Music::class.java]
        music.isLooping = true
        music.play()

        ProceduralPlanetTextureGenerator.generatePlanetTexturesFromData()

        background = MyGame.manager["mainBackground", Texture::class.java]

        MyGame.camera.position.set(MyGame.camera.viewportWidth/2f, MyGame.camera.viewportHeight/2f, 0f)
    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        MyGame.batch.projectionMatrix = MyGame.camera.combined
        MyGame.batch.begin()
        MyGame.batch.draw(background, 0f, 0f, 480f, 800f)
        MyGame.batch.end()
        MyGame.stage.draw()
        MyGame.stage.act()
    }

    override fun resume() {

    }

    override fun dispose() {

    }
}