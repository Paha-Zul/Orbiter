package com.quickbite.spaceslingshot.guis

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.Disableable
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

/**
 * Created by Paha on 3/29/2016.
 * A specialized health bar for my game!
 */
class CustomBar(private var normalAmount:Float, private var takenAmount:Float, private var maxAmount:Float, private val background: Drawable?, private val whitePixel: Drawable) : Widget(), Disableable {
    var takenColor = Color.RED
    var normalColor = Color.GREEN

    override fun draw(batch: Batch, parentAlpha: Float) {
        val normalAmountWidth = ((normalAmount - takenAmount)/maxAmount)*width
        val takenAmountWidth = (takenAmount/maxAmount)*width

        //Save the batch color
        val color = batch.color

        //Draw the 'normal' amount (left)
        batch.color = normalColor
        whitePixel.draw(batch, x, y, normalAmountWidth, height)

        //Draw the 'being taken' amount
        batch.color = takenColor
        whitePixel.draw(batch, x + normalAmountWidth, y, takenAmountWidth, height) //Go from the right to the left to posX

        //Restore the batch color
        batch.color = color

        background?.draw(batch, x, y, width, height) //Draw the bar background last.
    }

    fun setAmounts(normalAmount:Float, takenAmount:Float, maxAmount:Float = this.maxAmount){
        this.normalAmount = normalAmount
        this.takenAmount = takenAmount
        this.maxAmount = maxAmount
    }

    override fun setDisabled(p0: Boolean) {
        //        throw UnsupportedOperationException()
    }

    override fun isDisabled(): Boolean {
        //        throw UnsupportedOperationException()
        return false
    }
}