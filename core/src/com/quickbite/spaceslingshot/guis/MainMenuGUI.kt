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
import com.quickbite.spaceslingshot.LevelManager
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.screens.EditorScreen
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.screens.MainMenuScreen
import com.quickbite.spaceslingshot.util.Constants
import com.quickbite.spaceslingshot.util.Padding

/**
 * Created by Paha on 8/8/2016.
 */
class MainMenuGUI(val mainMenu:MainMenuScreen) {

    companion object{
        lateinit var removeAdButton:ImageButton

        fun removeAdsButton(){
            removeAdButton.remove()
        }
    }

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

        val editorButtonStyle = TextButton.TextButtonStyle()
        editorButtonStyle.font = MyGame.font
        editorButtonStyle.up = buttonUp
        editorButtonStyle.over = buttonDown
        editorButtonStyle.down = buttonDown

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

        val loginButtonStyle = ImageButton.ImageButtonStyle()
        loginButtonStyle.up = buttonUp
        loginButtonStyle.over = buttonDown
        loginButtonStyle.down = buttonDown
        loginButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("gpgLogoutIcon")) //Start with the logout icon

        val removeAdButtonStyle = ImageButton.ImageButtonStyle()
        removeAdButtonStyle.up = buttonUp
        removeAdButtonStyle.over = buttonDown
        removeAdButtonStyle.down = buttonDown
        removeAdButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("removeAdsIcon")) //Start with the logout icon

        val titleLabel = Label("Orbiter", labelStyle)
        titleLabel.setFontScale(0.8f)
        titleLabel.setAlignment(Align.center)


        val loginButton = ImageButton(loginButtonStyle)
        loginButton.imageCell.size(55f)
        if(!MyGame.actionResolver.signedInGPGS)
            loginButton.style.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("gpgLoginIcon")) //Put the log in icon

        removeAdButton = ImageButton(removeAdButtonStyle)
        removeAdButton.imageCell.size(55f)

        val playButton = ImageButton(playButtonStyle)
        playButton.imageCell.size(55f, 55f)

        val editorButton = TextButton("Editor", editorButtonStyle)
        editorButton.label.setFontScale(0.2f)

        val leaderboardButton = ImageButton(leaderboardButtonStyle)
        leaderboardButton.imageCell.size(55f, 55f)
        if(!MyGame.actionResolver.signedInGPGS)
            disableButton(leaderboardButton)

        val quitButton = ImageButton(quitButtonStyle)
        quitButton.imageCell.size(55f, 55f)

        //The top button table is for the login and remove ads buttons
        val topButtonTable = Table()

        topButtonTable.add(loginButton).top().left().size(100f, 64f).pad(5f, 5f, 0f, 0f)
        topButtonTable.add().expandX().fillX()
        topButtonTable.add(removeAdButton).top().right().size(100f, 64f).pad(5f, 5f, 0f, 0f)

        mainMenuTable.add(topButtonTable).expandX().fillX()
        mainMenuTable.row().expandX().fillX()
        mainMenuTable.add(titleLabel).spaceBottom(100f).expandX().fillX()
        mainMenuTable.row().expandX().fillX()
        mainMenuTable.add(playButton).size(128f, 64f).spaceBottom(20f).expandX().fillX()
        mainMenuTable.row().expandX().fillX()
        mainMenuTable.add(editorButton).size(128f, 64f).spaceBottom(20f).expandX().fillX()
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
                                loginButton.style.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("gpgLogoutIcon")) //Put the log out icon
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
                                loginButton.style.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("gpgLoginIcon")) //Put the log in icon
                                this.cancel()
                            }
                        }
                    }, 0f, 0.3f, 100)
                }
            }
        })

        editorButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                MyGame.stage.clear()
                mainMenu.game.screen = EditorScreen(mainMenu.game)
            }
        })

        leaderboardButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                MyGame.actionResolver.showLeaderboard(Constants.LEADERBOARD)
            }
        })

        removeAdButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                MyGame.transactions.purchaseNoAds()
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

        val levelsButtonStyle = ImageButton.ImageButtonStyle()
        levelsButtonStyle.up = buttonUp
        levelsButtonStyle.over = buttonDown
        levelsButtonStyle.down = buttonDown
        levelsButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("levelsIcon"))

        val endlessButtonStyle = ImageButton.ImageButtonStyle()
        endlessButtonStyle.up = buttonUp
        endlessButtonStyle.over = buttonDown
        endlessButtonStyle.down = buttonDown
        endlessButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("endlessIcon"))

        val backButtonStyle = ImageButton.ImageButtonStyle()
        backButtonStyle.up = buttonUp
        backButtonStyle.over = buttonDown
        backButtonStyle.down = buttonDown
        backButtonStyle.imageUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("backIcon"))

        val levelsButton = ImageButton(levelsButtonStyle)
        levelsButton.imageCell.size(55f, 55f)

        val endless = ImageButton(endlessButtonStyle)
        endless.imageCell.size(55f, 55f)

        val backButton = ImageButton(backButtonStyle)
        backButton.imageCell.size(55f, 55f)

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

        val numButtons = LevelManager.levels.size
        val levelList = LevelManager.getOrderedLevels()

        for(i in 0 until numButtons){

            val levelButton = TextButton("${levelList[i].level}", boxTextButtonStyle)
            levelButton.label.setFontScale(0.2f)
            levelButton.label.setAlignment(Align.center)

            levelButton.addListener(object:ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    MyGame.stage.clear() //Clear the stage
                    val game = GameScreen(mainMenu.game, levelList[i].level, false) //Create the game screen instance
                    mainMenu.game.screen = game //Set the screen
                }
            })

            buttonTable.add(levelButton).size(64f).space(spaceX.toFloat())
            if(i != 0 && i%numX == 0) buttonTable.row()
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