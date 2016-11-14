package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.screens.MainMenuScreen
import com.quickbite.spaceslingshot.util.GH
import com.quickbite.spaceslingshot.util.format

/**
 * Created by Paha on 8/7/2016.
 */
class GameScreenGUI(val gameScreen: GameScreen) : Disposable, IUpdateable{
    val fuelTable = Table()

    lateinit var fuelBar:CustomBar
    lateinit var fuelText:Label
    lateinit var levelTimerText:Label
    lateinit var scoreLabel:Label

    /* Game Over Stuff */
    val gameOverTable = Table()
    lateinit var gameOverStatusLabel:Label
    lateinit var timeLabel:Label
    lateinit var mainMenuButton:TextButton
    lateinit var retryButton:TextButton
    lateinit var nextLevelButton:TextButton

    lateinit var bottomPauseText:Label
    lateinit var bottomPauseButton:ProgressBar

    lateinit var relocateShipButton:Button
    var bottomTable:Table

    val buttonUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("button"))
    val buttonDown = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("button_down"))
    val boxUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("levelButton"))
    val boxDown = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("levelButton_down"))

    init{
        bottomTable = Table()

        //Need a bottom bar
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = MyGame.font
        buttonStyle.fontColor = Color.BLACK
        buttonStyle.up = TextureRegionDrawable(TextureRegion(GH.createPixel(Color.WHITE), MyGame.viewport.worldWidth.toInt(), 100))

        val mainMenuButtonStyle = ImageButton.ImageButtonStyle()
        mainMenuButtonStyle.imageUp = TextureRegionDrawable(TextureRegion(MyGame.manager["backButton", Texture::class.java]))
        mainMenuButtonStyle.up = boxUp
        mainMenuButtonStyle.over = boxDown
        mainMenuButtonStyle.down = boxDown

        val mainMenuButton = ImageButton(mainMenuButtonStyle)
        mainMenuButton.setSize(50f, 50f)
        mainMenuButton.setPosition(0f, MyGame.UIViewport.worldHeight - mainMenuButton.height)
        mainMenuButton.imageCell.size(25f)

        mainMenuButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                goToMainMenu()
            }
        })

        makeGameOverStuff()
        makeTopInfo()

        MyGame.stage.addActor(bottomTable)
        MyGame.stage.addActor(mainMenuButton)

        makePauseButton()
        makeFuelTable()
    }

    private fun makeFuelTable(){
        val mainTable = Table()
        val backTable = Table()
        val frontTable = Table()

        val background = NinePatchDrawable(NinePatch(MyGame.manager["box", Texture::class.java], 10, 10, 10, 10))
        fuelBar = CustomBar(gameScreen.data.ship.fuel, 0f, gameScreen.data.ship.fuel, background, TextureRegionDrawable(TextureRegion(GH.createPixel(Color.WHITE))))

        val fuelLabel = Label("Fuel", Label.LabelStyle(MyGame.font, Color.WHITE))
        fuelLabel.setFontScale(0.2f)

        backTable.background = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("fuelBarBackground"))
        backTable.setSize(128f, 64f)
        backTable.add(fuelBar).size(110f, 32f).bottom().padTop(25f)

        frontTable.background = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("fuelBarOverlay"))
        frontTable.setSize(128f, 64f)
        frontTable.add(fuelLabel).top().padBottom(45f)

        val stack = Stack(backTable, frontTable)
        stack.setSize(128f, 64f)
        stack.setPosition(MyGame.viewport.worldWidth - 128f, MyGame.viewport.worldHeight - 64f)

        mainTable.add(stack).top().right().size(128f, 64f)

        MyGame.stage.addActor(stack)
    }

    private fun makePauseButton(){
        val mainTable = Table()
        val backTable = Table()
        val frontTable = Table()

        val imageButtonStyle = Button.ButtonStyle()
        imageButtonStyle.up = TextureRegionDrawable(TextureRegion(MyGame.manager["relocateButton", Texture::class.java]))

        val progressBarStyle = ProgressBar.ProgressBarStyle()
        progressBarStyle.knobBefore = TextureRegionDrawable(TextureRegion(GH.createPixel(Color.GREEN, 1, 20)))

        bottomPauseText = Label("Paused", Label.LabelStyle(MyGame.font, Color.WHITE))
        bottomPauseText.setSize(40f, 40f)
        bottomPauseText.setFontScale(0.2f)
        bottomPauseText.setAlignment(Align.center)
//        bottomPauseText.style.background = TextureRegionDrawable(TextureRegion(GH.createPixel(Color.WHITE, 1, 40)))

        bottomPauseButton = ProgressBar(0f, 100f, 0.1f, false, progressBarStyle)
        bottomPauseButton.setSize(400f, 40f)

//        val pauseStack = Stack(bottomPauseButton, bottomPauseText)

        relocateShipButton = Button(imageButtonStyle)

//        bottomTable.add(pauseStack).height(40f).width(400f)
//        bottomTable.add(relocateShipButton).size(40f).padLeft(20f).padRight(20f).fillX()
//        bottomTable.bottom()
//        bottomTable.width = 480f
//        bottomTable.setPosition(0f,0f)

        backTable.background = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("fuelBarBackground"), 10, 10, 10, 10))
        backTable.setSize(MyGame.viewport.worldWidth - 100f, 20f)
        backTable.add(bottomPauseButton).size(MyGame.viewport.worldWidth - 120f, 32f).bottom().padTop(25f)

        frontTable.background = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("pauseBarOverlay"), 40, 40, 20, 30))
        frontTable.setSize(MyGame.viewport.worldWidth - 80f, 64f)
        frontTable.add(bottomPauseText).top().padBottom(20f)

        val stack = Stack(backTable, frontTable)
        stack.setSize(MyGame.viewport.worldWidth - 100f, 64f)
        stack.setPosition(0f, 0f)

        mainTable.add(stack).size(MyGame.viewport.worldWidth - 100f, 64f)
        mainTable.add().fillX().expandX()
        mainTable.add(relocateShipButton).size(64f, 64f)
        mainTable.setPosition(0f, 0f)
        mainTable.setSize(MyGame.viewport.worldWidth, 64f)

//        mainTable.debugAll()

        bottomPauseText.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                gameScreen.toggleGamePause()
                when(GameScreen.paused){
                    true -> bottomPauseText.setText("Resume")
                    false -> bottomPauseText.setText("Pause")
                }
            }
        })

        relocateShipButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val ship = gameScreen.data.ship
                MyGame.camera.position.set(ship.position.x, ship.position.y, 0f)
            }
        })

        MyGame.stage.addActor(mainTable)
    }

    private fun makeTopInfo(){
        fuelText = Label("Fuel: ", Label.LabelStyle(MyGame.font, Color.WHITE))
        fuelText.setFontScale(0.2f)
        fuelText.setAlignment(Align.center)

        levelTimerText = Label("Time: ", Label.LabelStyle(MyGame.font, Color.WHITE))
        levelTimerText.setFontScale(0.2f)
        levelTimerText.setAlignment(Align.center)

        fuelTable.setFillParent(true)
        fuelTable.padTop(20f)
        fuelTable.add(fuelText)
        fuelTable.row()
        fuelTable.add(levelTimerText).colspan(2)
        fuelTable.top()

        MyGame.stage.addActor(fuelTable)
    }

    private fun makeGameOverStuff(){
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = MyGame.font

        val labelStyle = Label.LabelStyle(MyGame.font, Color.WHITE)

        gameOverStatusLabel = Label("Lost", labelStyle)
        gameOverStatusLabel.setFontScale(0.2f)

        timeLabel = Label("Some Time...", labelStyle)
        timeLabel.setFontScale(0.2f)

        mainMenuButton = TextButton("Main Menu", textButtonStyle)
        mainMenuButton.label.setFontScale(0.2f)

        retryButton = TextButton("Retry", textButtonStyle)
        retryButton.label.setFontScale(0.2f)

        nextLevelButton = TextButton("Next ->", textButtonStyle)
        nextLevelButton.label.setFontScale(0.2f)

        mainMenuButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                goToMainMenu()
            }
        })

        retryButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                gameScreen.reloadLevel()
                hideGameOver()
            }
        })

        nextLevelButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if(!gameScreen.loadNextLevel()){
                    goToMainMenu()
                }else
                    hideGameOver()
            }
        })
    }

    private fun goToMainMenu(){
        val startTime = TimeUtils.millis()
        System.out.println("Starting to main menu")
        gameScreen.dispose()
        gameScreen.game.screen = MainMenuScreen(gameScreen.game)
        System.out.println("main menu took ${TimeUtils.millis() - startTime}")
    }

    fun showGameOver(failed:Boolean){
        gameOverTable.clear()

        val innerTable = Table()
        innerTable.background = TextureRegionDrawable(TextureRegion(GH.createPixel(Color.BLACK)))

        if(failed) gameOverStatusLabel.setText("Failed")
        else gameOverStatusLabel.setText("Success!")

        innerTable.add(gameOverStatusLabel).colspan(2)
        innerTable.row()
        innerTable.add(timeLabel).colspan(2)
        innerTable.row()

        if(failed){
            innerTable.add(mainMenuButton).spaceRight(20f)
            innerTable.add(retryButton)
        }else{
            innerTable.add(mainMenuButton).spaceRight(20f)
            innerTable.add(nextLevelButton)
        }

        gameOverTable.add(innerTable)
        gameOverTable.setFillParent(true)

        MyGame.stage.addActor(gameOverTable)
    }

    fun hideGameOver(){
        gameOverTable.remove()
    }

    override fun dispose() {
        MyGame.stage.clear()
    }

    override fun update(delta: Float) {
        this.levelTimerText.setText("Time: ${gameScreen.data.levelTimer.format(2)}")
    }

    override fun fixedUpdate(delta: Float) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}