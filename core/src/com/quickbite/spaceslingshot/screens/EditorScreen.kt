package com.quickbite.spaceslingshot.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.data.json.AchievementJson
import com.quickbite.spaceslingshot.guis.EditorGUI
import com.quickbite.spaceslingshot.inputprocessor.EditorScreenInputProcessor
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Planet
import com.quickbite.spaceslingshot.objects.gamescreenobjects.PlayerShip
import com.quickbite.spaceslingshot.objects.gamescreenobjects.SpaceBody
import com.quickbite.spaceslingshot.objects.gamescreenobjects.SpaceStation
import com.quickbite.spaceslingshot.util.*

/**
 * Created by Paha on 8/8/2016.
 */
class EditorScreen(val game:MyGame) : Screen{
    private var currentlyPlacing: SpaceBody? = null
    var currentlySelected: SpaceBody? = null

    val placedThings = mutableListOf<SpaceBody>()
    var playerShip:PlayerShip? = null

    val editorGUI = EditorGUI(this)

    override fun show() {
        editorGUI.openEditorGUI()
        LevelManager.init()

        val multiInput = InputMultiplexer(MyGame.stage, EditorScreenInputProcessor(this))
        Gdx.input.inputProcessor = multiInput
    }

    override fun pause() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun hide() {

    }

    override fun render(delta: Float) {
        Predictor.update()

        if(currentlyPlacing != null) {
            placeThing()
        }else{
            checkClickedOn()
        }

        checkDelete()
        checkSave()

        draw()
    }

    private fun draw(){
        val batch = MyGame.batch
        batch.projectionMatrix = MyGame.camera.combined
        batch.begin()
        showPlacing()
        placedThings.forEach {
            it.draw(batch)
        }
        placedThings.forEach {
            it.draw2(batch)
        }
        playerShip?.draw(batch)
        Predictor.drawLine(batch)
        batch.end()

        MyGame.debugRenderer.render(MyGame.world, MyGame.Box2dCamera.combined)

        MyGame.stage.act()
        MyGame.stage.draw()
    }

    private fun showPlacing(){
        if(currentlyPlacing == null)
            return

        if(Gdx.input.isKeyJustPressed(Input.Keys.R))
            currentlyPlacing!!.rotation += 90

        val worldCoords = MyGame.camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        currentlyPlacing!!.position.set(worldCoords.x, worldCoords.y)
        currentlyPlacing!!.draw(MyGame.batch)
        currentlyPlacing!!.draw2(MyGame.batch)
    }

    fun checkClickedOn():SpaceBody?{
        if(Gdx.input.justTouched()){
            val worldCoords = MyGame.camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            //Loop through each thing and check if any were clicked on
            placedThings.forEach {
                if(it.clickedOn(worldCoords.x, worldCoords.y)) {
                    currentlySelected = it //Set the currently selected
                    editorGUI.clickedOn(currentlySelected!!)
                    return it
                }
            }

            if(playerShip != null && playerShip!!.clickedOn(worldCoords.x, worldCoords.y)) {
                currentlySelected = playerShip //Set the currently selected
                editorGUI.clickedOn(currentlySelected!!)
                return playerShip!!
            }

            editorGUI.clickedOn(null)
        }

        return null
    }

    private fun checkDelete(){
        if(currentlySelected != null && Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)){
            if(currentlySelected == playerShip) {
                playerShip!!.dispose()
                playerShip = null
            }else {
                placedThings -= currentlySelected!!
                currentlySelected?.dispose()
            }
            currentlySelected = null
            editorGUI.clickedOn(null)
            Predictor.queuePrediction()
        }
    }

    private fun checkSave(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            editorGUI.openSaveLevelDialog()
        }else if(Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            editorGUI.openLoadLevelDialog()
        }
    }

    fun saveLevel(level:Int, levelName:String){
        val list = mutableListOf(playerShip!! as SpaceBody)
        list.addAll(placedThings)
        LevelManager.saveLevel(level, levelName, list,
                Array.with(AchievementJson("win", ""), AchievementJson("fuel", "50"), AchievementJson("time", "10"))) //TODO Fix to save achievements!

    }

    fun loadLevel(level:Int){
        //TODO Achievements?
        val data = LevelManager.loadLevel(level)
        placedThings.forEach { it.dispose() }
        placedThings.clear()

        data.first.forEach {
            if (it is PlayerShip)
                playerShip = it
            else
                placedThings += it
        }
    }

    /**
     * Replaces the currently selected planet with a new planet with the passed in parameters
     * @param size The size of the planet
     * @param gravityRange The range of the gravity well
     * @param gravityDensity The strength of the gravity
     */
    fun replacePlanet(size:Int, gravityRange:Float, gravityDensity:Float){
        placedThings -= currentlySelected!! //Remove it from the list
        //Replace it with a new planet
        currentlySelected = Planet(currentlySelected!!.position, size, gravityRange, gravityDensity, 0f, (currentlySelected!! as Planet).sprite.texture)
        placedThings += currentlySelected!! //Add it back to the list
        editorGUI.clickedOn(currentlySelected) //Reopen the editor GUI with the correct reference
    }

    /**
     * Places the current selected thing
     */
    private fun placeThing(){
        if(Gdx.input.justTouched()){
            if(currentlyPlacing !is PlayerShip) {
                placedThings += currentlyPlacing!!
            }else{
                playerShip?.dispose()
                playerShip = currentlyPlacing!! as PlayerShip
                playerShip?.setPhysicsPaused(true)
                Predictor.init(playerShip!!, {}, {}, {MyGame.world.step(0.016f, 4, 2)})
            }

            currentlyPlacing?.body?.setTransform(currentlyPlacing?.position!!.x*Constants.BOX2D_SCALE,
                    currentlyPlacing?.position!!.y*Constants.BOX2D_SCALE,
                    currentlyPlacing?.rotation!!)
            currentlyPlacing = null
            Predictor.queuePrediction()
        }
    }

    fun setPlacing(type:String){
        val worldCoords = MyGame.camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        when(type){
            "planet" -> {
                val planet = Planet(Vector2(worldCoords.x, worldCoords.y), 40, 40f, 0.01f, 0f, ProceduralPlanetTextureGenerator.getNextTexture())
                currentlyPlacing = planet
            }
            "station" -> {
                val station = SpaceStation(Vector2(worldCoords.x, worldCoords.y), 100f, 0f)
                currentlyPlacing = station
            }
            "ship" -> {
                val ship = PlayerShip(Vector2(worldCoords.x, worldCoords.y), 100f)
                ship.rotation = 0f
                ship.hideControls = true
                ship.setVelocity(0f, 1f)
                currentlyPlacing = ship //TODO Figure out how to run the predictor in the editor.
//                Predictor.init(ship, {ship.physicsArePaused = true}, {ship.physicsArePaused = false},
//                        {MyGame.world.step(0.06f, 2, 4)})
//                Predictor.queuePrediction = true
//                Predictor.update()
            }
        }
    }

    override fun resume() {

    }

    override fun dispose() {
        currentlyPlacing = null
        currentlySelected = null
        placedThings.forEach { it.dispose() } //This is super important to clean up world bodies
        placedThings.clear()
        playerShip?.dispose()
        MyGame.stage.clear()
    }
}