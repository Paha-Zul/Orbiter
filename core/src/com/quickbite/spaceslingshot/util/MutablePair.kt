package com.quickbite.spaceslingshot.util

/**
 * Created by Paha on 10/2/2016.
 */

class MutablePair<A, B>(var first:A, var second:B){
    fun set(first:A, second:B){
        this.first = first
        this.second = second
    }
}