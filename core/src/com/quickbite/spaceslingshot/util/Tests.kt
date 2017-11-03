package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.math.Vector2

/**
 * Created by Paha on 2/27/2017.
 */
object Tests {

    private val predictorList:MutableList<MovementData> = mutableListOf()
    private val shipList:MutableList<MovementData> = mutableListOf()

    var currIndex = 0

    fun update(){
//        if(currIndex < predictorList.size){
//            System.out.println("${shipList[currIndex]} -- ${predictorList[currIndex]}")
//            currIndex++
//        }
    }

    fun addToShipList(data:MovementData){
        shipList.add(data)
    }

    fun addToPredictorList(data:MovementData){
        predictorList.add(data)
    }

    fun clearShipList(){
        shipList.clear()
        currIndex = 0
    }

    fun clearPredictorList(){
        predictorList.clear()
    }

    data class MovementData(val position:Vector2, val velocity:Vector2, val rotation:Float, val gravitySources:Int, val fuel:Float, val delta:Float){
        override fun toString(): String {
            return "|$position|$velocity|$rotation|$gravitySources|$fuel|$delta|"
        }
    }
}