package com.quickbite.spaceslingshot.objects

/**
 * Created by Paha on 10/2/2016.
 *
 * @param burnForce The amount of force that this thruster outputs.
 * @param fuelBurnedPerTick The amount of fuel that the thruster consumes per tick.
 */
class Thruster(var burnForce:Float, var fuelBurnedPerTick:Float) {
    var burnTime:Float = 0f
    var doubleBurn:Boolean = false

    val burnAmount:Float
        get() = burnTime*fuelBurnedPerTick
}