package com.quickbite.spaceslingshot.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.quickbite.spaceslingshot.MyGame
import com.quickbite.spaceslingshot.guis.EditorGUI
import com.quickbite.spaceslingshot.inputprocessor.EditorScreenInputProcessor
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Planet
import com.quickbite.spaceslingshot.objects.gamescreenobjects.PlayerShip
import com.quickbite.spaceslingshot.objects.gamescreenobjects.SpaceBody
import com.quickbite.spaceslingshot.objects.gamescreenobjects.SpaceStation
import com.quickbite.spaceslingshot.util.LevelManager
import com.quickbite.spaceslingshot.util.ProceduralPlanetTextureGenerator

/**
 * Created by Paha on 8/8/2016.
 */
class EditorScreen(val game:MyGame) : Screen{
    private var currentlyPlacing: SpaceBody? = null
    var currentlySelected: SpaceBody? = null

    val placedThings = mutableListOf<SpaceBody>()

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
        batch.end()

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

    private fun checkClickedOn(){
        if(Gdx.input.justTouched()){
            val worldCoords = MyGame.camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            //Loop through each thing and check if any were clicked on
            placedThings.forEach {
                if(it.clickedOn(worldCoords.x, worldCoords.y)) {
                    currentlySelected = it //Set the currently selected
                    editorGUI.clickedOn(currentlySelected!!)
                    return
                }
            }

            editorGUI.clickedOn(null)
        }
    }

    private fun checkDelete(){
        if(currentlySelected != null && Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)){
           placedThings -= currentlySelected!!
            currentlySelected = null
            editorGUI.clickedOn(null)
        }
    }

    private fun checkSave(){
        if(Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            editorGUI.openSaveLevelDialog()
        }else if(Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            editorGUI.openLoadLevelDialog()
        }
    }

    fun loadLevel(level:Int){
        //TODO Achievements?
        val data = LevelManager.loadLevel(level)
        placedThings.forEach { it.dispose() }
        placedThings.clear()
        placedThings.addAll(data.first)
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
            placedThings += currentlyPlacing!!
            currentlyPlacing = null
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
                val station = SpaceStation(Vector2(worldCoords.x, worldCoords.y), 60, 100f, 0f)
                currentlyPlacing = station
            }
            "ship" -> {
                val ship = PlayerShip(Vector2(worldCoords.x, worldCoords.y), 100f)
                ship.rotation = 0f
                ship.hideControls = true
                currentlyPlacing = ship
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
        MyGame.stage.clear()
    }
}