package com.example.stepandstaircounter



import android.content.Context
import android.support.v4.content.ContextCompat.getColor
import android.widget.Button
import android.widget.TextView

class Utilities {
    fun minFinder(currentMin: Float, rawData: Float): Float{
        return if (currentMin > rawData)
            rawData
        else
            currentMin

    }

    fun maxFinder(currentMax: Float, rawData: Float): Float{
        return if (rawData > currentMax)
            rawData
        else
            currentMax
    }

    fun flushTextViews(xTV: TextView, yTV: TextView, zTV: TextView){
        xTV.setText(R.string.x_label)
        yTV.setText(R.string.y_label)
        zTV.setText(R.string.z_label)
    }

    fun textViewUpdater(valX: Float, valY: Float, valZ: Float, xTV: TextView, yTV: TextView, zTV: TextView){
        xTV.setText("X: " + valX.toString())
        yTV.setText("Y: " + valY.toString())
        zTV.setText("Z: " + valZ.toString())
    }

    fun averageCalculatorFromArray(sum: Float, arraySize: Int): Float = (sum/(arraySize/4))

    fun walkSpeedButtonBGColorUpdater(mContext: Context, blueBtn: Button, grayBtn1: Button, grayBtn2: Button){
        blueBtn.setBackgroundColor(mContext.getColor(R.color.myBlue))
        grayBtn1.setBackgroundColor(mContext.getColor(R.color.myGray))
        grayBtn2.setBackgroundColor(mContext.getColor(R.color.myGray))
    }

    fun stepCounterCleaner(stepCounter: Int): Int{
        var counter = stepCounter
        counter += -1
        if (counter <0)
            return 0
        else
            return counter
    }
}