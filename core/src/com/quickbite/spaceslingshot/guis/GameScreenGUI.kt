package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Timer
import com.quickbite.spaceslingshot.LevelManager
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.interfaces.IUpdateable
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.screens.MainMenuScreen
import com.quickbite.spaceslingshot.util.AchievementManager
import com.quickbite.spaceslingshot.util.GH
import com.quickbite.spaceslingshot.util.format

/**
 * Created by Paha on 8/7/2016.
 */
class GameScreenGUI(val gameScreen: GameScreen) : Disposable, IUpdateable{
    override var dead: Boolean = false
    val fuelTable = Table()

    lateinit var fuelBar:CustomBar
    lateinit var fuelText:Label
    lateinit var levelTimerText:Label
    lateinit var scoreLabel:Label

    /* Game Over Stuff */
    lateinit var bottomPauseText:Label
    lateinit var bottomPauseProgressBar:ProgressBar

    lateinit var relocateShipButton:Button
    private var bottomTable:Table = Table()

    private val buttonNewUp = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("buttonNew"), 25, 25, 20, 25))
    private val buttonNewDown = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("buttonNew_down"), 25, 25, 20, 25))

    private val boxUp = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("buttonNewSmallSolid"))
    private val boxDown = TextureRegionDrawable(MyGame.GUIAtlas.findRegion("buttonNewSmallSolid_down"))

    val checks = arrayOf(
            Image(MyGame.GUIAtlas.findRegion("checkmark")),
            Image(MyGame.GUIAtlas.findRegion("checkmark")),
            Image(MyGame.GUIAtlas.findRegion("checkmark")))

    init{

        //Need a bottom bar
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = MyGame.font
        buttonStyle.fontColor = Color.BLACK
        buttonStyle.up = TextureRegionDrawable(TextureRegion(GH.createPixel(Color.WHITE), MyGame.viewport.worldWidth.toInt(), 100))

        val mainMenuButtonStyle = ImageButton.ImageButtonStyle()
        mainMenuButtonStyle.imageUp = TextureRegionDrawable(TextureRegion(MyGame.GUIAtlas.findRegion("backButton")))
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

//        val background = NinePatchDrawable(NinePatch(MyGame.manager["box", Texture::class.java], 10, 10, 10, 10))
        fuelBar = CustomBar(GameScreen.gameScreenData.ship.fuel, 0f, GameScreen.gameScreenData.ship.fuel, null, TextureRegionDrawable(TextureRegion(GH.createPixel(Color.WHITE))))

        val fuelLabel = Label("Fuel", Label.LabelStyle(MyGame.font, Color.WHITE))
        fuelLabel.setFontScale(0.2f)

        backTable.background = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("fuelBarBackground"), 20, 20, 20, 20))
        backTable.setSize(128f, 64f)
        backTable.add(fuelBar).size(110f, 32f).bottom().padTop(20f)

        frontTable.background = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("fuelBarOverlay"), 15, 15, 18, 30))
        frontTable.setSize(128f, 64f)
        frontTable.add(fuelLabel).top().padBottom(25f)

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

        frontTable.touchable = Touchable.enabled

        val imageButtonStyle = Button.ButtonStyle()
        imageButtonStyle.up = TextureRegionDrawable(TextureRegion(MyGame.GUIAtlas.findRegion("locateButton")))

        val progressBarStyle = ProgressBar.ProgressBarStyle()
        progressBarStyle.knobBefore = TextureRegionDrawable(TextureRegion(GH.createPixel(Color.GREEN, 1, 20)))

        //The text that says the state of pasued
        bottomPauseText = Label("Paused", Label.LabelStyle(MyGame.font, Color.WHITE))
        bottomPauseText.setSize(40f, 40f)
        bottomPauseText.setFontScale(0.2f)
        bottomPauseText.setAlignment(Align.center)

        //The progress bar for pause timer
        bottomPauseProgressBar = ProgressBar(0f, 100f, 0.1f, false, progressBarStyle)
        bottomPauseProgressBar.setSize(400f, 40f)

        relocateShipButton = Button(imageButtonStyle)

        //The table behind the progress bar. Also includes the progress bar
        backTable.background = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("fuelBarBackground"), 10, 10, 10, 10))
        backTable.setSize(MyGame.viewport.worldWidth - 100f, 20f)
        backTable.add(bottomPauseProgressBar).size(MyGame.viewport.worldWidth - 120f, 32f).bottom().padTop(25f)

        //The table overlaying on top of the progress bar
        frontTable.background = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("fuelBarOverlay"), 15, 15, 18, 30))
        frontTable.setSize(MyGame.viewport.worldWidth - 80f, 64f)
        frontTable.add(bottomPauseText).top().padBottom(20f).fillX().expandX()

        //Stack the back table and front table
        val stack = Stack(backTable, frontTable)
        stack.setSize(MyGame.viewport.worldWidth - 100f, 64f)
        stack.setPosition(0f, 0f)

        frontTable.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                gameScreen.toggleGamePause()
                when(GameScreen.paused){
                    true -> bottomPauseText.setText("Resume")
                    false -> bottomPauseText.setText("Pause")
                }
            }
        })

        mainTable.add(stack).size(MyGame.viewport.worldWidth - 100f, 64f)
        mainTable.add().fillX().expandX()
        mainTable.add(relocateShipButton).size(64f, 64f).pad(0f, 0f, 5f, 5f)
        mainTable.setPosition(0f, 0f)
        mainTable.setSize(MyGame.viewport.worldWidth, 64f)

//        mainTable.debugAll()

//        bottomPauseText.addListener(object:ClickListener(){
//            override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                gameScreen.toggleGamePause()
//                when(GameScreen.paused){
//                    true -> bottomPauseText.setText("Resume")
//                    false -> bottomPauseText.setText("Pause")
//                }
//            }
//        })

        relocateShipButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val ship = GameScreen.gameScreenData.ship
//                gameScreen.scrollScreen(ship.position.x, ship.position.y)

                val ease = Interpolation.circleOut
                val currPos = Vector2(MyGame.camera.position.x, MyGame.camera.position.y)
                val targetPos = Vector2(ship.position)
                var counter = 0f

                Timer.schedule(object:Timer.Task(){
                    override fun run() {
                        counter += 0.032f
                        val x = ease.apply(currPos.x, targetPos.x, counter)
                        val y = ease.apply(currPos.y, targetPos.y, counter)
                        gameScreen.scrollScreen(x, y)

                        if(counter >= 1)
                            this.cancel()
                    }
                }, 0f, 0.016f)
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

    /**
     * Prebuilds things like the main menu / retry buttons, labels, listeners and such
     */
    private fun makeGameOverStuff(){
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = MyGame.font
        textButtonStyle.up = buttonNewUp
        textButtonStyle.over = buttonNewDown
        textButtonStyle.down = buttonNewDown

        val labelStyle = Label.LabelStyle(MyGame.font, Color.WHITE)

        GameOverTable.statusLabel = Label("Lost", labelStyle)
        GameOverTable.statusLabel.setFontScale(0.2f)
        GameOverTable.statusLabel.setAlignment(Align.center)

        GameOverTable.timeLabel = Label("haha", labelStyle)
        GameOverTable.timeLabel.setFontScale(0.2f)
        GameOverTable.timeLabel.setAlignment(Align.center)

        GameOverTable.mainMenuButton = TextButton("Main \n   Menu", textButtonStyle)
        GameOverTable.mainMenuButton.label.setFontScale(0.15f)

        GameOverTable.retryButton = TextButton("Retry", textButtonStyle)
        GameOverTable.retryButton.label.setFontScale(0.15f)

        GameOverTable.nextButton = TextButton("Next", textButtonStyle)
        GameOverTable.nextButton.label.setFontScale(0.15f)

        GameOverTable.mainMenuButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                goToMainMenu()
            }
        })

        GameOverTable.retryButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                gameScreen.loadLevel(GameScreen.gameScreenData.currLevel)
                hideGameOver()
            }
        })

        GameOverTable.nextButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if(!gameScreen.hasNextLevel()){
                    goToMainMenu()
                }else {
                    gameScreen.loadNextLevel()
                    hideGameOver()
                }
            }
        })
    }

    private fun goToMainMenu(){
        gameScreen.dispose()
        gameScreen.game.screen = MainMenuScreen(gameScreen.game)
        MyGame.ads.showInterAd()
    }

    fun showGameOver(failed:Boolean){
        val containerTable = Table()

        GameOverTable.gameOverTable.clear()

        val innerTable = Table()

        val completedTable = Table()
        val buttonTable = Table()

        val labelStyle = Label.LabelStyle(MyGame.font, Color.WHITE)

        val label1 = Label(GH.getAchievementMessage(0, gameScreen), labelStyle)
        label1.setFontScale(0.15f)
        label1.setAlignment(Align.center)
        label1.setWrap(true)

        val label2 = Label(GH.getAchievementMessage(1, gameScreen), labelStyle)
        label2.setFontScale(0.15f)
        label2.setAlignment(Align.center)
        label2.setWrap(true)

        val label3 = Label(GH.getAchievementMessage(2, gameScreen), labelStyle)
        label3.setFontScale(0.15f)
        label3.setAlignment(Align.center)
        label3.setWrap(true)

        //The boxes
        val image1 = Image(MyGame.GUIAtlas.findRegion("checkBoxSquare"))
        val image2 = Image(MyGame.GUIAtlas.findRegion("checkBoxSquare"))
        val image3 = Image(MyGame.GUIAtlas.findRegion("checkBoxSquare"))

        val stack1 = Stack(image1)
        val stack2 = Stack(image2)
        val stack3 = Stack(image3)

        completedTable.add(label1).fillX().expandX()
        completedTable.add(label2).fillX().expandX()
        completedTable.add(label3).fillX().expandX()
        completedTable.row()
        completedTable.add(stack1).size(50f)
        completedTable.add(stack2).size(50f)
        completedTable.add(stack3).size(50f)

        GameOverTable.gameOverTable.background = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("gameOverDialog"), 50, 50, 50, 50))

        if(failed) GameOverTable.statusLabel.setText("Failed")
        else GameOverTable.statusLabel.setText("Success!")

        innerTable.add(GameOverTable.statusLabel)
        innerTable.row().spaceTop(10f)
        innerTable.add(GameOverTable.timeLabel)
        innerTable.row().spaceTop(10f)
        innerTable.add(completedTable).fillX()
        innerTable.row().spaceTop(10f)
        innerTable.add(buttonTable).padBottom(5f)

        GameOverTable.gameOverTable.add(innerTable)

        if(GameScreen.gameScreenData.endlessGame == null)
            GameOverTable.timeLabel.setText("Completed in ${GameScreen.gameScreenData.levelTimer.format(2)}s")
        else
            GameOverTable.timeLabel.setText("Passed ${GameScreen.gameScreenData.currPlanetScore} planets!")

        if(failed){
            buttonTable.add(GameOverTable.mainMenuButton).spaceRight(20f).width(100f).height(50f)
            buttonTable.add(GameOverTable.retryButton).width(100f).height(50f)
        }else{
            buttonTable.add(GameOverTable.mainMenuButton).spaceRight(20f).width(100f).height(50f)
            buttonTable.add(GameOverTable.nextButton).width(100f).height(50f)
        }

        GameOverTable.gameOverTable.isTransform = true
        GameOverTable.gameOverTable.setOrigin(Align.center)
        GameOverTable.gameOverTable.setScale(0f)

        stack1.add(checks[0])
        stack2.add(checks[1])
        stack3.add(checks[2])

        val prefAchievements = AchievementManager.loadAchievementsFromPref((GameScreen.gameScreenData.currLevel+1).toString())
        if(!prefAchievements[0]) checks[0].color.a = 0f
        if(!prefAchievements[1]) checks[1].color.a = 0f
        if(!prefAchievements[2]) checks[2].color.a = 0f

        //Open the table quickly and display the checks fo the achievements
        GameOverTable.gameOverTable.addAction(Actions.sequence(Actions.scaleTo(1f, 1f, 0.1f), Actions.delay(1f), object: Action() {
            override fun act(delta: Float): Boolean {
                //For each flag, fade in a check if we met the requirements
                val ach1 = GameScreen.gameScreenData.achievementFlags[0]
                val ach2 = GameScreen.gameScreenData.achievementFlags[1]
                val ach3 = GameScreen.gameScreenData.achievementFlags[2]

                if(ach1) fadeInCheckmark(image1, checks[0], 0f)
                if(ach2) fadeInCheckmark(image2, checks[1], 0.2f)
                if(ach3) fadeInCheckmark(image3, checks[2], 0.4f)
                return true
            }
        }))

        containerTable.setOrigin(Align.center)
        containerTable.setFillParent(true)
        containerTable.add(GameOverTable.gameOverTable).center()
        MyGame.stage.addActor(containerTable)
    }

    private fun fadeInCheckmark(box:Actor, checkmark:Actor, delay:Float, speed:Float = 0.3f){
        val startSize = 512f

        val coords = Vector2(0f, 0f) // (0,0) for the bottom left of the actor
        box.localToStageCoordinates(coords) //Translate to stage
//        coords.y = Gdx.graphics.height - coords.y - checkmark.height/2f //Gotta flip the Y here

        checkmark.color.a = 0f
        checkmark.setSize(startSize, startSize)
        checkmark.setPosition(coords.x, coords.y)

        checkmark.addAction(Actions.sequence(Actions.delay(delay), object :Action(){
            override fun act(delta: Float): Boolean {
                checkmark.addAction(Actions.fadeIn(speed))
                checkmark.addAction(Actions.sizeTo(64f, 64f, speed))
                checkmark.addAction(Actions.moveTo(coords.x, coords.y, speed))
                return true
            }
        }))

        MyGame.stage.addActor(checkmark)
    }

    fun hideGameOver(){
        GameOverTable.gameOverTable.remove()
        checks.forEach { it.remove() }
    }

    override fun dispose() {
        MyGame.stage.clear()
    }

    override fun update(delta: Float) {
        this.levelTimerText.setText("Time: ${GameScreen.gameScreenData.levelTimer.format(2)}")
    }

    override fun fixedUpdate(delta: Float) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    object GameOverTable{
        val gameOverTable = Table()
        lateinit var mainMenuButton:TextButton
        lateinit var retryButton:TextButton
        lateinit var nextButton:TextButton
        lateinit var timeLabel:Label
        lateinit var statusLabel: Label
    }
}