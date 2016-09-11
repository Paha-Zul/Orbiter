package com.quickbite.spaceslingshot.util

/**
 * Created by Paha on 9/10/2016.
 */
class BodyData(val type:ObjectType, val id:Long, val bodyOwner:Any) {
    enum class ObjectType{Ship, Planet, Obstacle}
}