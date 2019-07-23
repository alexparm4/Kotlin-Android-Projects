package com.example.colorpicker

import android.graphics.Color
import android.widget.SeekBar
import android.widget.TextView

class ColorManager {

    fun colorLoaderFromSpinner(arr: Array<String>, position: Int, colorView: TextView,
                               rBar: SeekBar, gBar: SeekBar, bBar: SeekBar){
    var listColorValues: List<Int> = UtilityManager().arrayToLister(arr, position)
        colorView.setBackgroundColor(
            Color.rgb(listColorValues[0], listColorValues[1], listColorValues[2]))
        rBar.progress = listColorValues[0]
        gBar.progress = listColorValues[1]
        bBar.progress = listColorValues[2]
    }

    fun bigColorViewChanger(cView: TextView, red: Int, green: Int, blue: Int){
        cView.setBackgroundColor(Color.rgb(red, green, blue))
    }

    fun textBoxColorChanger(colorTextView: TextView, red: Int, green: Int, blue: Int){
        colorTextView.setBackgroundColor(Color.rgb(red, green, blue))
    }
}