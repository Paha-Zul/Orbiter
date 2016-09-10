package com.quickbite.spaceslingshot.interfaces

import com.badlogic.gdx.physics.box2d.Body

/**
 * Created by Paha on 9/9/2016.
 */
interface IPhysicsBody {
    var body:Body

    fun createBody()
}