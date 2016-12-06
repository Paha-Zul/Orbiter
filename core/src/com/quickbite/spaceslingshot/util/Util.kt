package com.quickbite.spaceslingshot.util

/**
 * Created by Paha on 12/5/2016.
 */
object Util {
    /**
     * Saves the achievements to the player pref file.
     */
    fun saveAchievementsToPref(flags: Array<Boolean>, key:String) {
        //Load the previous scores and OR them together (to get only true)
        val previous = loadAchievementsFromPref(key)
        flags[0] = flags[0] || previous[0]
        flags[1] = flags[1] || previous[1]
        flags[2] = flags[2] || previous[2]

        val combined = flags.joinToString(",") //Join to string with commas and NO space
        PrefManager.addToPrefs(key, combined, "achievement") //Write it
        PrefManager.achievementPrefs.flush() //Save it
    }

    /**
     * Loads the achievements from the player pref file
     */
    fun loadAchievementsFromPref(key:String):Array<Boolean>{
        val data = PrefManager.getStringFromPrefs(key, "achievement")
        val split = data.split(',')

        //If something went wrong (or we haven't written to this key yet), return all false
        if(split.size < 2)
            return arrayOf(false, false, false)

        //Return the array of booleans
        return arrayOf(split[0].toBoolean(), split[1].toBoolean(), split[2].toBoolean())
    }
}