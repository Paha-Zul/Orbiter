package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.objects.*
import com.quickbite.spaceslingshot.screens.EditorScreen
import com.quickbite.spaceslingshot.util.onEnterListener
import com.quickbite.spaceslingshot.util.onLeaveFieldOrEnter

class EditorGUI(val editorScreen: EditorScreen) {

    private val mainTable = Table()
    private val bottomTable = Table() //This table will be for 'contextual' things, like when we select a planet
    private val rightTable = Table() // This table is for placing things like stations, planets, the ship

    private val defaultLabelStyle = Label.LabelStyle(MyGame.scaledFont, Color.WHITE)
    private val defaultTextFieldStyle = TextField.TextFieldStyle()
    private val defaultTextButtonStyle = TextButton.TextButtonStyle()

    private val inputTextBackground = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("inputBoxBackground"), 5, 5, 5, 5))
    private val inputTextBackgroundFilled = NinePatchDrawable(NinePatch(MyGame.GUIAtlas.findRegion("inputBoxBackgroundFilled"), 5, 5, 5, 5))

    init{
        defaultTextFieldStyle.font = MyGame.scaledFont
        defaultTextFieldStyle.fontColor = Color.WHITE
        defaultTextFieldStyle.background = inputTextBackground
        defaultTextFieldStyle.cursor = inputTextBackground

        defaultTextButtonStyle.font = MyGame.scaledFont
        defaultTextButtonStyle.fontColor = Color.WHITE

        bottomTable.background = inputTextBackgroundFilled

        mainTable.add().expand().fill()
        mainTable.add(rightTable)
        mainTable.row()
        mainTable.add(bottomTable).fillX()

    }

    fun openEditorGUI(){
        mainTable.touchable = Touchable.enabled
        makeRightTable()

        mainTable.setFillParent(true)
        MyGame.stage.addActor(mainTable)
    }

    private fun makeRightTable(){
        rightTable.clear()

        val planetImage = Image(TextureRegion(MyGame.manager["planet", Texture::class.java]))
        val stationImage = Image(TextureRegion(MyGame.gameScreenAtlas.findRegion("station")))
        val shipImage = Image(TextureRegion(MyGame.gameScreenAtlas.findRegion("spaceship")))

        rightTable.add(planetImage).size(64f)
        rightTable.row().spaceTop(5f)
        rightTable.add(stationImage).size(64f)
        rightTable.row().spaceTop(5f)
        rightTable.add(shipImage).size(64f)

        planetImage.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                editorScreen.setPlacing("planet")
            }
        })


        stationImage.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                editorScreen.setPlacing("station")
            }
        })

        shipImage.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                editorScreen.setPlacing("ship")
            }
        })

        bottomTable.touchable = Touchable.enabled
    }

    fun insideUI():Boolean{
        var coords = MyGame.stage.screenToStageCoordinates(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
        coords = bottomTable.stageToLocalCoordinates(coords)
        val rect = Rectangle(bottomTable.x, bottomTable.y, bottomTable.width, bottomTable.height)

        return rect.contains(coords)
    }

    fun clickedOn(body:SpaceBody?){
        if(body == null && insideUI())
            return

        bottomTable.clear()

        when(body){
            is Planet -> clickedOnPlanet(body)
            is SpaceStation -> clickedOnStation(body)
            is PlayerShip -> clickedOnShip(body)
//            is AsteroidSpawner -> clickedOnASteroidSpawner()
        }
    }

    private fun clickedOnPlanet(planet:Planet){
        bottomTable.clear()

        val planetSizeLabel = Label("Planet Size:", defaultLabelStyle)
        val planetSizeInput = TextField(planet.size.toString(), defaultTextFieldStyle)

        val gravityWellLabel = Label("Gravity Well:", defaultLabelStyle)
        val gravityWellInput = TextField(planet.gravityRange.toString(), defaultTextFieldStyle)

        val gravityWellStrengthLabel = Label("Gravity Strength:", defaultLabelStyle)
        val gravityWellStrengthInput = TextField(planet.density.toString(), defaultTextFieldStyle)

        val randomizeSeedButton = TextButton("Random Seed", defaultTextButtonStyle)

        bottomTable.add(planetSizeLabel)
        bottomTable.add(planetSizeInput)
        bottomTable.row().spaceTop(2f)
        bottomTable.add(gravityWellLabel)
        bottomTable.add(gravityWellInput)
        bottomTable.row().spaceTop(2f)
        bottomTable.add(gravityWellStrengthLabel)
        bottomTable.add(gravityWellStrengthInput)
        bottomTable.row().spaceTop(2f)
        bottomTable.add(randomizeSeedButton).colspan(2)
        //Need to be able to change size, looks, gravity

        planetSizeInput.addListener(object:InputListener(){
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if(keycode == Input.Keys.ENTER){
                    try{
                        val size = planetSizeInput.text.toInt()
                        val gravityRange = gravityWellInput.text.toFloat()
                        val density = gravityWellStrengthInput.text.toFloat()
                        editorScreen.replacePlanet(size, gravityRange, density)
                    }catch (e:Exception){
                        println(e)
                    }
                }
                return super.keyDown(event, keycode)
            }
        })

        gravityWellInput.addListener(object:InputListener(){
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if(keycode == Input.Keys.ENTER){
                    try{
                        val input = gravityWellInput.text.toFloat()
                        planet.gravityRangeRadius = input
                    }catch (e:Exception){
                        println(e)
                    }
                }
                return super.keyDown(event, keycode)
            }
        })

        gravityWellStrengthInput.addListener(object:InputListener(){
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if(keycode == Input.Keys.ENTER){
                    try{
                        val input = gravityWellStrengthInput.text.toFloat()
                        planet.density = input
                    }catch (e:Exception){
                        println(e)
                    }
                }
                return super.keyDown(event, keycode)
            }
        })
    }

    private fun clickedOnStation(station:SpaceStation){
        println("station")
        bottomTable.clear()

        val rotationLabel = Label("Rotation:", defaultLabelStyle)
        val rotationInput = TextField(station.rotation.toString(), defaultTextFieldStyle)

        bottomTable.add(rotationLabel)
        bottomTable.add(rotationInput)
        bottomTable.row()

        rotationInput.onLeaveFieldOrEnter {
            try{
                val rotation = rotationInput.text.toFloat()
                station.rotation = rotation
            }catch (e:Exception){
                println(e)
            }
        }
    }

    private fun clickedOnShip(ship:PlayerShip){
        //Need to be able to change initial velocity, rotation, position

        bottomTable.clear()

        val shipRotationLabel = Label("Rotation:", defaultLabelStyle)
        val shipRotationInput = TextField(ship.rotation.toString(), defaultTextFieldStyle)

        val shipXVelocityLabel = Label("X Velocity:", defaultLabelStyle)
        val shipXVelocityInput = TextField(ship.velocity.x.toString(), defaultTextFieldStyle)

        val shipYVelocityLabel = Label("Y Velocity:", defaultLabelStyle)
        val shipYVelocityInput = TextField(ship.velocity.y.toString(), defaultTextFieldStyle)

        val shipFuelLabel = Label("Fuel:", defaultLabelStyle)
        val shipFuelInput = TextField(ship.fuel.toString(), defaultTextFieldStyle)

        bottomTable.add(shipRotationLabel)
        bottomTable.add(shipRotationInput)
        bottomTable.row().spaceTop(2f)
        bottomTable.add(shipXVelocityLabel)
        bottomTable.add(shipXVelocityInput)
        bottomTable.row().spaceTop(2f)
        bottomTable.add(shipYVelocityLabel)
        bottomTable.add(shipYVelocityInput)
        bottomTable.row().spaceTop(2f)
        bottomTable.add(shipFuelLabel)
        bottomTable.add(shipFuelInput)

        shipRotationInput.onLeaveFieldOrEnter {
            try{
                val rotation = shipRotationInput.text.toFloat()
                ship.rotation = rotation
            }catch (e:Exception){
                println(e)
            }
        }

        shipXVelocityInput.onLeaveFieldOrEnter {
            try{
                val xVelocity = shipXVelocityInput.text.toFloat()
                ship.velocity.x = xVelocity
            }catch (e:Exception){
                println(e)
            }
        }

        shipYVelocityInput.onLeaveFieldOrEnter {
            try{
                val yVelocity = shipYVelocityInput.text.toFloat()
                ship.velocity.y = yVelocity
            }catch (e:Exception){
                println(e)
            }
        }

        shipFuelInput.onLeaveFieldOrEnter {
            try{
                val fuel = shipFuelInput.text.toFloat()
                ship.fuel = fuel
            }catch (e:Exception){
                println(e)
            }
        }
    }

    private fun clickedOnASteroidSpawner(){
        //Change spawn direction, spawn speed range, asteroid speed, location
    }
}