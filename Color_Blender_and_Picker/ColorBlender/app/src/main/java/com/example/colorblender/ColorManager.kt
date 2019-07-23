package com.example.colorblender

import android.graphics.Color
import android.widget.TextView

class ColorManager{
    fun staticViewColorer(staticView: TextView, color: String){
        val colorValues = UtilitiesManager().stringToRGBIntLister(color)
        staticView.setBackgroundColor(Color.rgb(colorValues[0],colorValues[1],colorValues[2]))
    }

    fun dynamicViewBackgroundColorer(dynamicView: TextView, color: String, colorPercent: Float){
        val colorList = UtilitiesManager().stringToRGBIntLister(color)
        val red = UtilitiesManager().percentOfOriginalCalculator(colorList[0], colorPercent)
        val green = UtilitiesManager().percentOfOriginalCalculator(colorList[1], colorPercent)
        val blue = UtilitiesManager().percentOfOriginalCalculator(colorList[2], colorPercent)

        dynamicView.setBackgroundColor(Color.rgb(red, green, blue))
    }

    fun blenderViewColorer(blendView: TextView, barProgress: Int, color1: String, color2: String){
        val colorPercents = UtilitiesManager().colorsPercentageLister(barProgress)
        val color1Values = UtilitiesManager().stringToRGBIntLister(color1)
        val color2Values = UtilitiesManager().stringToRGBIntLister(color2)

        val col1Red = UtilitiesManager().percentOfOriginalCalculator(color1Values[0], colorPercents[0])
        val col1Green = UtilitiesManager().percentOfOriginalCalculator(color1Values[1], colorPercents[0])
        val col1Blue = UtilitiesManager().percentOfOriginalCalculator(color1Values[2], colorPercents[0])
        val col2Red = UtilitiesManager().percentOfOriginalCalculator(color2Values[0], colorPercents[1])
        val col2Green = UtilitiesManager().percentOfOriginalCalculator(color2Values[1], colorPercents[1])
        val col2Blue = UtilitiesManager().percentOfOriginalCalculator(color2Values[2], colorPercents[1])

        val sumRed = col1Red + col2Red
        val sumGreen = col1Green + col2Green
        val sumBlue = col1Blue + col2Blue

        blendView.setBackgroundColor(Color.rgb(sumRed, sumGreen, sumBlue))
    }

    fun dynamicViewTextColorer(colorDynamicView: TextView, color: String, percent: Float){
        val colorValues = UtilitiesManager().stringToRGBIntLister(color)
        val sumColorValues = UtilitiesManager().percentOfOriginalCalculator(colorValues[0], percent)
                                + UtilitiesManager().percentOfOriginalCalculator(colorValues[1], percent)
                                + UtilitiesManager().percentOfOriginalCalculator(colorValues[2], percent)

        if(sumColorValues < 140){
            colorDynamicView.setTextColor(Color.rgb(255, 255, 255))
        }
        else
            colorDynamicView.setTextColor(Color.rgb(0, 0, 0))
    }

}
