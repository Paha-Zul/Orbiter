package com.quickbite.spaceslingshot.interfaces

import com.quickbite.spaceslingshot.util.IKillable

/**
 * Created by Paha on 8/6/2016.
 */
interface IUpdateable : IKillable {
    fun update(delta:Float)
    fun fixedUpdate(delta: Float)
}