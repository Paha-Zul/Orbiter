package com.quickbite.spaceslingshot.util

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Array
import com.quickbite.spaceslingshot.interfaces.IDrawable
import com.quickbite.spaceslingshot.interfaces.IUpdateable

/**
 * Created by Paha on 12/1/2016.
 */
object UpdateManager {
    val updateList:Array<Array<IUpdateable>> = Array()
    val renderList:Array<Array<IDrawable>> = Array()

    fun update(delta:Float){
        updateList.forEach { list ->
            var i = 0
            val size = list.size

            //While we are under the size
            while(i < size){
                //If dead, remove
                if(list[i].dead) {
                    list.removeIndex(i)
                    i--

                //Otherwise, update
                }else
                    list[i].update(delta)

                i++
            }
        }
    }

    fun updateFixed(delta:Float){
        updateList.forEach { list ->
            var i = 0
            val size = list.size

            //While we are under the size
            while(i < size){
                //If dead, remove
                if(list[i].dead) {
                    list.removeIndex(i)
                    i--

                    //Otherwise, update
                }else
                    list[i].fixedUpdate(delta)

                i++
            }
        }
    }

    fun render(batch:SpriteBatch){
        renderList.forEach { list ->
            var i = 0
            val size = list.size

            //While we are under the size
            while(i < size){
                //If dead, remove
                if(list[i].dead) {
                    list.removeIndex(i)
                    i--

                    //Otherwise, update
                }else
                    list[i].draw(batch)

                i++
            }
        }
    }

    /**
     * @param updateable The IUpdateable object to add
     * @param order The order to put the object into. Lower numbers have priority
     */
    fun addUpdateable(updateable:IUpdateable, order:Int){
        //If we don't have enough arrays to handle the order, increase the array
        if(order >= updateList.size){
            val diff = order - updateList.size
            for(i in 0..diff) { //0 to diff does <=, so 0..4 is i <= 4
                updateList.add(Array())
            }
        }

        updateList[order].add(updateable)
    }

    /**
     * @param drawable The IDrawable object to add
     * @param order The order to put the object into. Lower numbers have priority
     */
    fun addDrawable(drawable:IDrawable, order:Int){
        //If we don't have enough arrays to handle the order, increase the array
        if(order >= renderList.size){
            val diff = order - renderList.size
            for(i in 0..diff) { //0 to diff does <=, so 0..4 is i <= 4
                renderList.add(Array())
            }
        }

        renderList[order].add(drawable)
    }

    fun <T> addUpdateableAndDrawable(obj:T, updateOrder:Int, drawOrder:Int) where T:IUpdateable, T:IDrawable{
        addUpdateable(obj, updateOrder)
        addDrawable(obj, drawOrder)
    }

    fun clearAll(){
        renderList.forEach { list ->
            list.forEach(IDrawable::dispose)
        }

        updateList.forEach { list2 ->
            list2.forEach(IUpdateable::dispose)
        }
    }
}