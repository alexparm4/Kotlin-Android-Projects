package com.example.colorblender

import android.widget.TextView

class PercentageTextManager {
    fun percentageValueUpdater(color1DynamicView: TextView, color2DynamicView: TextView, progress: Int){
        val percentList = percentageForDisplayCleaner(progress)
        color1DynamicView.text = percentList.first()
        color2DynamicView.text = percentList.last()
    }

    private fun percentageForDisplayCleaner(barProgress: Int): List<String>{
        val percentValues = UtilitiesManager().colorsPercentageLister(barProgress)
        val color1Cleaned = (percentValues.first()*100).toInt().toString() + "%"
        val color2Cleaned = (percentValues.last()*100).toInt().toString() + "%"
        return listOf(color1Cleaned, color2Cleaned)
    }
}