package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.quickbite.spaceslingshot.objects.BodyData

/**
 * Created by Paha on 9/10/2016.
 */
class ContactListenerClass : ContactListener{
    override fun endContact(contact: Contact) {
        val dataA = contact.fixtureA.body.userData as BodyData
        val dataB = contact.fixtureB.body.userData as BodyData


        //call this event for both bodies
        EventSystem.callEvent("collide_end", listOf(contact.fixtureB), dataA.id)
        EventSystem.callEvent("collide_end", listOf(contact.fixtureA), dataB.id)
    }

    override fun beginContact(contact: Contact) {
        val dataA = contact.fixtureA.body.userData as BodyData
        val dataB = contact.fixtureB.body.userData as BodyData

        //call this event for both bodies
        EventSystem.callEvent("collide_begin", listOf(contact.fixtureB), dataA.id)
        EventSystem.callEvent("collide_begin", listOf(contact.fixtureA), dataB.id)
    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {

    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {

    }
}