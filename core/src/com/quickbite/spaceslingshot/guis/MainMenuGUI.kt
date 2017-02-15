package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Timer
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.screens.MainMenuScreen
import com.quickbite.spaceslingshot.util.Constants
import com.quickbite.spaceslingshot.util.GameLevels
import com.quickbite.spaceslingshot.util.Padding

/**
 * Created by Paha on 8/8/2016.
 */
class MainMenuGUI(val mainMenu:MainMenuScreen) {
    val mainTable:Table = Table()
    val levelsTable:Table = Table()
    val gameTypeSelectionTable:Table = Table()

    val mainMenuTable:Table = Table()

    val buttonUp = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("buttonNew"), 25, 25, 25, 25))
//    val buttonDown = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("button_down"), 25, 25, 25, 25))
    val buttonDown = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("buttonNew_down"), 25, 25, 25, 25))

    val boxUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("buttonNewSmall"))
    val boxDown = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("buttonNewSmall_down"))

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

        val buttonStyle2 = TextButton.TextButtonStyle()
        buttonStyle2.font = MyGame.font
        buttonStyle2.fontColor = Color.WHITE
        buttonStyle2.disabledFontColor = Color(0.5f, 0.5f, 0.5f, 0.75f)
        buttonStyle2.up = boxUp
        buttonStyle2.over = boxDown
        buttonStyle2.down = boxDown

        val playButtonStyle = ImageButton.ImageButtonStyle()
        playButtonStyle.up = buttonUp
        playButtonStyle.over = buttonDown
        playButtonStyle.down = buttonDown
        playButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("playButtonIcon"))

        val leaderboardButtonStyle = ImageButton.ImageButtonStyle()
        leaderboardButtonStyle.up = buttonUp
        leaderboardButtonStyle.over = buttonDown
        leaderboardButtonStyle.down = buttonDown
        leaderboardButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("leaderboardIcon"))

        val quitButtonStyle = ImageButton.ImageButtonStyle()
        quitButtonStyle.up = buttonUp
        quitButtonStyle.over = buttonDown
        quitButtonStyle.down = buttonDown
        quitButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("quitButtonIcon"))

        val titleLabel = Label("Orbiter", labelStyle)
        titleLabel.setFontScale(0.8f)
        titleLabel.setAlignment(Align.center)

        val loginButton = TextButton("Log-Out", buttonStyle2)
        loginButton.label.setFontScale(0.15f)
        if(!MyGame.actionResolver.signedInGPGS)
            loginButton.setText("Log-In")

        val playButton = ImageButton(playButtonStyle)
        playButton.imageCell.size(55f, 55f)

        val leaderboardButton = ImageButton(leaderboardButtonStyle)
        leaderboardButton.imageCell.size(55f, 55f)
        if(!MyGame.actionResolver.signedInGPGS)
            disableButton(leaderboardButton)

        val quitButton = ImageButton(quitButtonStyle)
        quitButton.imageCell.size(55f, 55f)

        mainMenuTable.add(loginButton).top().left().size(64f, 64f).pad(5f, 5f, 0f, 0f)
        mainMenuTable.row().expandX().fillX()
        mainMenuTable.add(titleLabel).spaceBottom(100f).expandX().fillX()
        mainMenuTable.row().expandX().fillX()
        mainMenuTable.add(playButton).size(128f, 64f).spaceBottom(20f).expandX().fillX()
        mainMenuTable.row().expandX().fillX()
        mainMenuTable.add(leaderboardButton).size(128f, 64f).spaceBottom(20f).expandX().fillX()
        mainMenuTable.row().expandX().fillX()
        mainMenuTable.add(quitButton).size(128f, 64f).expandX().fillX()

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

        loginButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if(!MyGame.actionResolver.signedInGPGS) {
                    MyGame.actionResolver.loginGPGS() //Login

                    //Make a timer to check if we are logged in. When we are, enable the button
                    Timer.schedule(object:Timer.Task(){
                        override fun run() {
                            if(MyGame.actionResolver.signedInGPGS){
                                enableButton(leaderboardButton)
                                loginButton.setText("Log-Out")
                                this.cancel()
                            }
                        }
                    }, 0f, 0.3f, 100)
                }else {
                    MyGame.actionResolver.logoutGPGS()

                    //Make a timer to check if we are logged out. When we are, disable the button
                    Timer.schedule(object:Timer.Task(){
                        override fun run() {
                            if(!MyGame.actionResolver.signedInGPGS){
                                disableButton(leaderboardButton)
                                loginButton.setText("Log-In")
                                this.cancel()
                            }
                        }
                    }, 0f, 0.3f, 100)
                }
            }
        })

        leaderboardButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                MyGame.actionResolver.getLeaderboardGPGS(Constants.LEADERBOARD)
            }
        })

        mainMenuTable.top().left()
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

    private fun disableButton(button: Button){
        button.isDisabled = true
        button.color.a = 0.5f
    }

    private fun enableButton(button: Button){
        button.isDisabled = false
        button.color.a = 1f
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

        val numButtons = GameLevels.levels.size

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