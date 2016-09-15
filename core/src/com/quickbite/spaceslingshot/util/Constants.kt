package com.quickbite.spaceslingshot.util

/**
 * Created by Paha on 9/10/2016.
 */
object Constants {

    val BOX2D_SCALE = 0.1f
    val BOX2D_INVERSESCALE: Float
        get() = 1/ BOX2D_SCALE

    val VELOCITY_SCALE = 10f
    val VELOCITY_INVERSESCALE:Float
        get() = 1/VELOCITY_SCALE

    val UPDATE_TIME_STEP = 1/60f

    val PHYSICS_TIME_STEP = 1/60f
    val VELOCITY_ITERATIONS = 6
    val POSITION_ITERATIONS = 2
}