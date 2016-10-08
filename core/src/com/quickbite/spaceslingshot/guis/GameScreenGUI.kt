package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.screens.GameScreen
import com.quickbite.spaceslingshot.screens.MainMenuScreen
import com.quickbite.spaceslingshot.util.GH

/**
 * Created by Paha on 8/7/2016.
 */
class GameScreenGUI(val gameScreen: GameScreen) : Disposable{
    val fuelTable = Table()
    lateinit var fuelBar:CustomBar

    /* Game Over Stuff */
    val gameOverTable = Table()
    lateinit var gameOverStatusLabel:Label
    lateinit var timeLabel:Label
    lateinit var mainMenuButton:TextButton
    lateinit var retryButton:TextButton
    lateinit var nextLevelButton:TextButton

    lateinit var bottomPauseButton:TextButton
    lateinit var relocateShipButton:Button
    lateinit var bottomTable:Table

    init{
        bottomTable = Table()

        val imageButtonStyle = Button.ButtonStyle()
        imageButtonStyle.up = TextureRegionDrawable(TextureRegion(MyGame.manager["relocateButton", Texture::class.java]))

        //Need a bottom bar
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = MyGame.font
        buttonStyle.fontColor = Color.BLACK
        buttonStyle.up = TextureRegionDrawable(TextureRegion(GH.createPixel(Color.WHITE), MyGame.viewport.worldWidth.toInt(), 100))

        bottomPauseButton = TextButton("Resume", buttonStyle)
        bottomPauseButton.label.setFontScale(0.2f)

        relocateShipButton = Button(imageButtonStyle)

        bottomTable.add(bottomPauseButton).height(40f).width(400f)
        bottomTable.add(relocateShipButton).size(40f).padLeft(20f).padRight(20f).fillX()
        bottomTable.bottom()
        bottomTable.width = 480f
        bottomTable.setPosition(0f,0f)

        val mainMenuButton = ImageButton(TextureRegionDrawable(TextureRegion(MyGame.manager["backButton", Texture::class.java])))
        mainMenuButton.setSize(50f, 50f)
        mainMenuButton.setPosition(0f, MyGame.UIViewport.worldHeight - mainMenuButton.height)

        bottomPauseButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                gameScreen.toggleGamePause()
                when(GameScreen.paused){
                    true -> bottomPauseButton.setText("Resume")
                    false -> bottomPauseButton.setText("Pause")
                }
            }
        })

        relocateShipButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val ship = gameScreen.data.ship
                MyGame.camera.position.set(ship.position.x, ship.position.y, 0f)
            }
        })

        mainMenuButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                goToMainMenu()
            }
        })

        makeGameOverStuff()
        makeTopInfo()

        MyGame.stage.addActor(bottomTable)
        MyGame.stage.addActor(mainMenuButton)
    }

    private fun makeTopInfo(){
        val background = NinePatchDrawable(NinePatch(MyGame.manager["box", Texture::class.java], 10, 10, 10, 10))
        fuelBar = CustomBar(gameScreen.data.ship.fuel, 0f, gameScreen.data.ship.fuel, background, TextureRegionDrawable(TextureRegion(GH.createPixel(Color.WHITE))))

        fuelTable.setFillParent(true)
        fuelTable.add(fuelBar).top().size(100f, 25f)
        fuelTable.padTop(20f)
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
        gameScreen.dispose()
        gameScreen.game.screen = MainMenuScreen(gameScreen.game)
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
}