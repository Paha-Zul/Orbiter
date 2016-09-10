package com.quickbite.spaceslingshot.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.ProceduralPlanetTextureGenerator
import com.quickbite.spaceslingshot.guis.MainMenuGUI
import com.quickbite.spaceslingshot.util.JsonLevelLoader

/**
 * Created by Paha on 8/7/2016.
 */
class MainMenuScreen(val game:MyGame) :Screen{
    override fun show() {
        MainMenuGUI(this)

        JsonLevelLoader.loadLevels()
        Gdx.input.inputProcessor = MyGame.stage

        ProceduralPlanetTextureGenerator.generatePlanetTexturesThreaded(10)
    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        MyGame.stage.draw()
        MyGame.stage.act()
    }

    override fun resume() {

    }

    override fun dispose() {

    }
}