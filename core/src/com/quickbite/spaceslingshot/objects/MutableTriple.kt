package com.quickbite.spaceslingshot.objects

/**
 * Created by Paha on 11/13/2016.
 */
class MutableTriple<A, B, C>(var first:A, var second:B, var third:C){
    fun set(first:A, second:B, third:C){
        this.first = first
        this.second = second
        this.third = third
    }
}