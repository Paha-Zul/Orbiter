package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.utils.Array

/**
 * Created by Paha on 9/9/2016.
 * A simple event system to pass messages to other objects. Use onEvent to store an event and callEvent to call the event.
 */
object EventSystem {
    val eventMap:MutableMap<String, Array<Pair<Long, (List<Any>) -> Unit>>> = mutableMapOf()

    /**
     * @param name The name of the event
     * @param event The event function
     * @param uniqueID Optional ID parameter. Leaving blank will result in -1 and is used to called events globally. Set
     * this ID to use for specific actors/objects
     */
    fun onEvent(name:String, event:(List<Any>) -> Unit, uniqueID:Long = -1){
        val evtList = eventMap.getOrPut(name, {Array<Pair<Long, (List<Any>) -> Unit>>()})
        evtList.add(Pair(uniqueID, event))
    }

    /**
     * @param name The name of the event
     * @param args The list of arguments to send to the event.
     * @param uniqueID Optional ID parameter. Leaving blank will result in -1 and is used to called events globally. Set
     * this ID to use for specific actors/objects
     */
    fun callEvent(name:String, args:List<Any>, uniqueID:Long = -1){
        //Get the event list. If null (?:), return
        val evtList:(Array<Pair<Long, (List<Any>) -> Unit>>)? = eventMap[name] ?: return

        //For each pair in the list, check the unique ID match and call the event if matching.
        evtList!!.forEach {evt ->
            if(evt.first === uniqueID)
                evt.second(args)
        }
    }

}