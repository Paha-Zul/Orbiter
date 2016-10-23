package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.util.MutablePair

/**
 * Created by Paha on 10/2/2016.
 *
 * @param burnForce The amount of force that this thruster outputs.
 * @param fuelBurnedPerTick The amount of fuel that the thruster consumes per tick.
 */
class Thruster(var burnForce:Float, var fuelBurnedPerTick:Float, val burnDirection:Vector2, val location:Ship.ShipLocation, val rotationOffset:Float) {
    private var _result:MutablePair<Float, Float> = MutablePair(0f, 0f)

    var burnTime:Int = 0
    var doubleBurn:Boolean = false

    /** The amount of fuel burned. This is calculated from burnTime * fuelBurnedPerTick */
    val burnAmount:Float
        get() = burnTime*fuelBurnedPerTick

    /**
     * Calculates the burning of fuel for a tick
     * @param fuel The fuel of the ship.
     * @return A Pair object that holds the remaining fuel in the first and the burn force in the second.
     */
    fun burnFuel(fuel:Float):MutablePair<Float, Float>{
        if(fuel <= 0 || burnTime <= 0) {
            _result.set(0f, 0f)
            return _result
        }

        burnTime--

        _result.set(fuelBurnedPerTick, burnForce)
        return _result
    }

    /** Toggles the double burn of the ship
     * @return True if the double burn is activated, false otherwise.
     */
    fun toggleDoubleBurn(fuel:Float):Boolean{
        return setDoubleBurn(!this.doubleBurn, fuel)
    }

    /**
     * Sets the double burn to a certain value
     * @param burnValue The value to set the double burn as
     * @return What the burn value is.
     */
    fun setDoubleBurn(burnValue:Boolean, fuel:Float):Boolean{
        if(burnValue == this.doubleBurn) return doubleBurn
        this.doubleBurn = burnValue

        when(burnValue){
            //If we are double burning, set the texture and apply the bonus burn.
            true -> {
                burnForce *= 2f
                fuelBurnedPerTick *= 2f
            }
            //If we are not double burning, set the texture and reduce our burn.
            false ->{
                burnForce /= 2f
                fuelBurnedPerTick /= 2f
            }
        }

        //If we don't have enough fuel, clamp it
        if(burnAmount > fuel)
            burnTime = (fuel/fuelBurnedPerTick).toInt()

        return burnValue
    }

    /**
     * Used mostly for copying the burn force and burn per tick to another ship.
     */
    fun setBurnForceAndPerTick(force:Float, amount:Float){
        this.burnForce = force
        this.fuelBurnedPerTick = amount
    }

    fun reset(){
        burnTime = 0
    }
}