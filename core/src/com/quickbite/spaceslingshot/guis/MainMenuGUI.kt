package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.screens.MainMenuScreen
import com.quickbite.spaceslingshot.util.JsonLevelLoader
import com.quickbite.spaceslingshot.util.Padding

/**
 * Created by Paha on 8/8/2016.
 */
class MainMenuGUI(val mainMenu:MainMenuScreen) {
    val mainTable:Table = Table()
    val levelsTable:Table = Table()

    init{
        val labelStyle = Label.LabelStyle(MyGame.font, Color.WHITE)

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = MyGame.font
        buttonStyle.fontColor = Color.WHITE
        buttonStyle.disabledFontColor = Color(0.5f, 0.5f, 0.5f, 0.75f)

        val titleLabel = Label("Orbiter", labelStyle)
        titleLabel.setFontScale(0.8f)

        val playButton = TextButton("Play", buttonStyle)
        playButton.label.setFontScale(0.4f)

        val editorButton = TextButton("Editor", buttonStyle)
        editorButton.label.setFontScale(0.4f)
        editorButton.isDisabled = true

        val quitButton = TextButton("Quit", buttonStyle)
        quitButton.label.setFontScale(0.4f)

        mainTable.add(titleLabel).spaceBottom(100f)
        mainTable.row()
        mainTable.add(playButton)
        mainTable.row()
        mainTable.add(editorButton)
        mainTable.row()
        mainTable.add(quitButton)

        playButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                showLevels()
            }
        })

        quitButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                Gdx.app.exit()
            }
        })

//        editorButton.addListener(object:ChangeListener(){
//            override fun changed(event: ChangeEvent?, actor: Actor?) {
//                MyGame.stage.clear()
//                mainMenu.game.screen = EditorScreen(mainMenu.game)
//            }
//        })

        mainTable.setFillParent(true)

        MyGame.stage.addActor(mainTable)
    }

    fun showLevels(){
        MyGame.stage.clear()

        val buttonTable = Table()

        val background = TextureRegionDrawable(TextureRegion(MyGame.manager["box", Texture::class.java]))

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = MyGame.font
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = background


        val buttonSize = 64
        val offsets:Padding = Padding(10, 10, 10, 10)
        val maxWidth = 480
        val maxHeight = 800

        val spaceX = 5

        val numX = (maxWidth - offsets.left - offsets.right)/(buttonSize+spaceX)
        val numY = (maxHeight - offsets.top - offsets.bottom)/(buttonSize+spaceX)

        val numButtons = JsonLevelLoader.levels.size

        for(i in 1..numButtons){
            val level = i-1
            val levelButton = TextButton("$level", textButtonStyle)
            levelButton.label.setFontScale(0.2f)
            levelButton.label.setAlignment(Align.center)

            levelButton.addListener(object:ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    MyGame.stage.clear() //Clear the stage
                    val game = GameScreen(mainMenu.game, -1, true) //Create the game screen instance
                    mainMenu.game.screen = game //Set the screen
                }
            })

            buttonTable.add(levelButton).size(64f).space(spaceX.toFloat())
            if(i%numX == 0) buttonTable.row()
        }

        levelsTable.add(buttonTable)
        levelsTable.setFillParent(true)
        MyGame.stage.addActor(levelsTable)
    }
}