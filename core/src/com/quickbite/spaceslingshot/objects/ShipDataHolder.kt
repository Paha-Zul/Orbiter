package com.quickbite.spaceslingshot.objects

import com.badlogic.gdx.math.Vector2
import com.quickbite.spaceslingshot.objects.gamescreenobjects.Thruster

data class ShipDataHolder(var position:Vector2, var fuel:Float, var velocity:Vector2, var rotation:Float, var thrusterData:List<Thruster>)