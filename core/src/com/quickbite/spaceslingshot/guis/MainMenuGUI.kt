package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
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
    val gameTypeSelectionTable:Table = Table()

    val mainMenuTable:Table = Table()

    val buttonUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("button"))
    val buttonDown = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("button_down"))
    val boxUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("levelButton"))
    val boxDown = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("levelButton_down"))

    val scaleSpeed = 0.3f

    init{
        buildMainMenu()
        buildGameTypeSelection()
        buildLevelSelection()
        showMainMenu()
    }

    /**
     * Builds and lays out the main menu table. Use mainMenuTable to add or remove from the stage.
     */
    fun buildMainMenu(){
        val labelStyle = Label.LabelStyle(MyGame.font, Color.WHITE)

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = MyGame.font
        buttonStyle.fontColor = Color.WHITE
        buttonStyle.disabledFontColor = Color(0.5f, 0.5f, 0.5f, 0.75f)
        buttonStyle.up = buttonUp
        buttonStyle.over = buttonDown
        buttonStyle.down = buttonDown

        val titleLabel = Label("Orbiter", labelStyle)
        titleLabel.setFontScale(0.8f)

        val playButton = TextButton("Play", buttonStyle)
        playButton.label.setFontScale(0.2f)

        val editorButton = TextButton("Editor", buttonStyle)
        editorButton.label.setFontScale(0.2f)
        editorButton.isDisabled = true

        val quitButton = TextButton("Quit", buttonStyle)
        quitButton.label.setFontScale(0.2f)

        mainMenuTable.add(titleLabel).spaceBottom(100f)
        mainMenuTable.row()
        mainMenuTable.add(playButton).size(128f, 64f).spaceBottom(20f)
        mainMenuTable.row()
        mainMenuTable.add(editorButton).size(128f, 64f).spaceBottom(20f)
        mainMenuTable.row()
        mainMenuTable.add(quitButton).size(128f, 64f)

        //On hitting the play button, show us the level select
        playButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                gameTypeSelectionTable.color.a = 0f
                gameTypeSelectionTable.setScale(0f)

                showGameTypeSelection()
                scaleTableIn(gameTypeSelectionTable, scaleSpeed, null, Interpolation.circle, Interpolation.circle)
                scaleTableOut(mainMenuTable, scaleSpeed, 8f, {mainMenuTable.remove()}, Interpolation.circle, Interpolation.circle)
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

        mainMenuTable.setFillParent(true)
    }

    private fun scaleTableOut(table:Table, speed:Float, scale:Float, callback:(()->Unit)? = null, scaleInterp:Interpolation = Interpolation.linear, fadeInterp:Interpolation = Interpolation.linear){
        table.isTransform = true
        table.setOrigin(Align.center)

        table.addAction(Actions.scaleTo(scale, scale, speed, scaleInterp))
        table.addAction(Actions.sequence(Actions.alpha(0f, speed, fadeInterp), object:Action(){
            override fun act(delta: Float): Boolean {
                callback?.invoke()
                return true
            }
        }))
    }

    private fun scaleTableIn(table:Table, speed:Float, callback:(()->Unit)? = null, scaleInterp:Interpolation = Interpolation.linear, fadeInterp:Interpolation = Interpolation.linear){
        table.isTransform = true
        table.setOrigin(Align.center)

        table.addAction(Actions.scaleTo(1f, 1f, speed, scaleInterp))
        table.addAction(Actions.sequence(Actions.alpha(1f, speed, fadeInterp), object:Action(){
            override fun act(delta: Float): Boolean {
                callback?.invoke()
                return true
            }
        }))
    }

    /**
     * Shows the main menu. Simply adds mainMenuTable to the game stage.
     */
    fun showMainMenu(){
        MyGame.stage.addActor(mainMenuTable)
    }

    fun buildGameTypeSelection(){
        gameTypeSelectionTable.clear()

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = MyGame.font
        textButtonStyle.up = buttonUp
        textButtonStyle.over = buttonDown
        textButtonStyle.down = buttonDown

        val levelsButton = TextButton("Levels", textButtonStyle)
        levelsButton.label.setFontScale(0.2f)

        val endless = TextButton("Endless", textButtonStyle)
        endless.label.setFontScale(0.2f)

        val backButton = TextButton("Back", textButtonStyle)
        backButton.label.setFontScale(0.2f)

        gameTypeSelectionTable.add(levelsButton).size(128f, 64f).spaceBottom(40f) //.padTop(270f)
        gameTypeSelectionTable.row()
        gameTypeSelectionTable.add(endless).size(128f, 64f).spaceBottom(40f)
        gameTypeSelectionTable.row()
        gameTypeSelectionTable.add(backButton).size(128f, 64f)

        levelsButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                levelsTable.setScale(0f)
                levelsTable.color.a = 0f

                showLevelSelection()
                scaleTableOut(gameTypeSelectionTable, scaleSpeed, 8f, { gameTypeSelectionTable.remove() }, Interpolation.circle, Interpolation.circle)
                scaleTableIn(levelsTable, scaleSpeed, null, Interpolation.circle, Interpolation.circle)
            }
        })

        endless.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                MyGame.stage.clear() //Clear the stage
                val game = GameScreen(mainMenu.game, -1, true) //Create the game screen instance
                mainMenu.game.screen = game //Set the screen
            }
        })

        backButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                scaleTableOut(gameTypeSelectionTable, scaleSpeed, 0f, {gameTypeSelectionTable.remove()}, Interpolation.circle, Interpolation.circle)
                scaleTableIn(mainMenuTable, scaleSpeed, null, Interpolation.circle, Interpolation.circle)

                showMainMenu()
            }
        })

        gameTypeSelectionTable.validate()
        gameTypeSelectionTable.setPosition(MyGame.viewport.worldWidth/2f - gameTypeSelectionTable.width/2f, MyGame.viewport.worldHeight/2f - gameTypeSelectionTable.height/2f)
    }

    fun showGameTypeSelection(){
        MyGame.stage.addActor(gameTypeSelectionTable)
    }

    fun buildLevelSelection(){
        val buttonTable = Table()

        val background = TextureRegionDrawable(TextureRegion(MyGame.manager["box", Texture::class.java]))

        val boxTextButtonStyle = TextButton.TextButtonStyle()
        boxTextButtonStyle.font = MyGame.font
        boxTextButtonStyle.fontColor = Color.WHITE
        boxTextButtonStyle.up = boxUp
        boxTextButtonStyle.over = boxDown
        boxTextButtonStyle.down = boxDown

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = MyGame.font
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = buttonUp
        textButtonStyle.over = buttonDown
        textButtonStyle.down = buttonDown

        val backButton = TextButton("Back", textButtonStyle)
        backButton.label.setFontScale(0.2f)

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
            val levelButton = TextButton("${level+1}", boxTextButtonStyle)
            levelButton.label.setFontScale(0.2f)
            levelButton.label.setAlignment(Align.center)

            levelButton.addListener(object:ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    MyGame.stage.clear() //Clear the stage
                    val game = GameScreen(mainMenu.game, level, false) //Create the game screen instance
                    mainMenu.game.screen = game //Set the screen
                }
            })

            buttonTable.add(levelButton).size(64f).space(spaceX.toFloat())
            if(i%numX == 0) buttonTable.row()
        }

        backButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                gameTypeSelectionTable.color.a = 0f
                gameTypeSelectionTable.setScale(8f)

                scaleTableOut(levelsTable, scaleSpeed, 0f, {levelsTable.remove()}, Interpolation.circle, Interpolation.circle)
                scaleTableIn(gameTypeSelectionTable, scaleSpeed, null, Interpolation.circle, Interpolation.circle)
                showGameTypeSelection()
//                showMainMenu()
            }
        })

        levelsTable.add(buttonTable).spaceBottom(20f)
        levelsTable.row()
        levelsTable.add(backButton).size(128f, 64f)

        levelsTable.validate()
        levelsTable.setPosition(MyGame.viewport.worldWidth/2f - levelsTable.width/2f, MyGame.viewport.worldHeight/2f - levelsTable.height/2f)
        levelsTable.setOrigin(Align.center)
    }

    fun showLevelSelection(){
        MyGame.stage.addActor(levelsTable)
    }
}