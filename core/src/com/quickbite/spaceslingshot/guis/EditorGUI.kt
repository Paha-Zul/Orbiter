package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.scenes.scene2d.ui.Table

object EditorGUI {
    val mainTable = Table()
    val bottomTable = Table() //This table will be for 'contextual' things, like when we select a planet
    val rightTable = Table() // This table is for placing things like stations, planets, the ship

    init{

    }

    fun openEditorGUI(){

    }

    fun clickedOn(context:String){
        bottomTable.clear()

        when(context){
            "planet" -> clickedOnPlanet()
            "station" -> clickedOnStation()
            "ship" -> clickedOnShip()
            "asteroid" -> clickedOnASteroidSpawner()
        }
    }

    private fun clickedOnPlanet(){
        //Need to be able to change size, looks, gravity
    }

    private fun clickedOnStation(){
        //Need to be able to change rotation
    }

    private fun clickedOnShip(){
        //Need to be able to change initial velocity, rotation, position
    }

    private fun clickedOnASteroidSpawner(){
        //Change spawn direction, spawn speed range, asteroid speed, location
    }
}