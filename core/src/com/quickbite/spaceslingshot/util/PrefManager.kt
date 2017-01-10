package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

/**
 * Created by Paha on 11/13/2016.
 */
object PrefManager {

    public enum class PrefType{
        Achievement, Data;
    }

    var achievementPrefs = Gdx.app.getPreferences("Achievements")
    var dataPrefs = Gdx.app.getPreferences("Data")

    fun addToPrefs(name:String, value:Int, type:PrefType){
        val prefs = getPref(type)
        prefs.putInteger(name, value)
    }

    fun addToPrefs(name:String, value:String, type:PrefType){
        val prefs = getPref(type)
        prefs.putString(name, value)
    }

    fun addToPrefs(name:String, value:Float, type:PrefType){
        val prefs = getPref(type)
        prefs.putFloat(name, value)
    }

    fun addToPrefs(name:String, value:Boolean, type:PrefType){
        val prefs = getPref(type)
        prefs.putBoolean(name, value)
    }

    fun getIntFromPrefs(name:String, type:PrefType):Int{
        val prefs = getPref(type)
        return prefs.getInteger(name)
    }

    fun getFloatFromPrefs(name:String, type:PrefType):Float{
        val prefs = getPref(type)
        return prefs.getFloat(name)
    }

    fun getStringFromPrefs(name:String, type:PrefType):String{
        val prefs = getPref(type)
        return prefs.getString(name)
    }

    fun getBooleanFromPrefs(name:String, type:PrefType):Boolean{
        val prefs = getPref(type)
        return prefs.getBoolean(name)
    }

    private fun getPref(type:PrefType):Preferences{
        when(type){
            PrefType.Achievement -> return achievementPrefs
            else -> return dataPrefs
        }
    }
}