package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

/**
 * Created by Paha on 11/13/2016.
 */
object PrefManager {

    var achievementPrefs = Gdx.app.getPreferences("Achievements")
    var dataPrefs = Gdx.app.getPreferences("Data")

    fun addToPrefs(name:String, value:Int, type:String){
        val prefs = getPref(type)
        prefs.putInteger(name, value)
    }

    fun addToPrefs(name:String, value:String, type:String){
        val prefs = getPref(type)
        prefs.putString(name, value)
    }

    fun addToPrefs(name:String, value:Float, type:String){
        val prefs = getPref(type)
        prefs.putFloat(name, value)
    }

    fun addToPrefs(name:String, value:Boolean, type:String){
        val prefs = getPref(type)
        prefs.putBoolean(name, value)
    }

    fun getIntFromPrefs(name:String, type:String):Int{
        val prefs = getPref(type)
        return prefs.getInteger(name)
    }

    fun getFloatFromPrefs(name:String, type:String):Float{
        val prefs = getPref(type)
        return prefs.getFloat(name)
    }

    fun getStringFromPrefs(name:String, type:String):String{
        val prefs = getPref(type)
        return prefs.getString(name)
    }

    fun getBooleanFromPrefs(name:String, type:String):Boolean{
        val prefs = getPref(type)
        return prefs.getBoolean(name)
    }

    private fun getPref(type:String):Preferences{
        when(type){
            "achievement" -> return achievementPrefs
            else -> return dataPrefs
        }
    }
}