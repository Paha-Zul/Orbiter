package com.quickbite.spaceslingshot.objects

/**
 * Created by Paha on 9/10/2016.
 * A Data class to hold information on a body for collisions
 * @param type The ObjectType info for collisions
 * @param id The ID of the object (this is usually to help call in the event system)
 * @param bodyOwner The body owner. This could be something like a PlayerShip, SpaceStation, etc
 * @param dataObject Usually the same thing as the body owner?
 */
class BodyData(val type: ObjectType, val id:Long, val bodyOwner:Any, val dataObject:Any) {
    enum class ObjectType{Ship, Planet, Obstacle, Station, FuelContainer, Asteroid}
}