package com.example.colorpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import java.io.File

class UtilityManager{
    //Found at: https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
    fun softKeyboardHider(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }

    fun arrayToLister(colorValueArray: Array<String>, position: Int)
            = listOf(colorValueArray[position].substring(0, 3).toInt(),
            colorValueArray[position].substring(3, 6).toInt(),
            colorValueArray[position].substring(6, 9).toInt())

    fun colorValuePadder(colorVal: Int): String{
        if (colorVal == 0){
            return "000"
        }
        else {
            return colorVal.toString().padStart(3, '0')
        }
    }

    fun smallRGBViewUpdater(colorTextView: TextView, color: Int, key: String){
        if (key == "red"){
            colorTextView.text = color.toString()
            ColorManager().textBoxColorChanger(colorTextView, color, 0, 0)
            colorTextView.setTextColor(Color.rgb( 1-color, 255, 255))
        }
        else if (key == "green"){
            colorTextView.text = color.toString()
            ColorManager().textBoxColorChanger(colorTextView, 0, color, 0)
            colorTextView.setTextColor(Color.rgb(255, 1-color, 255))
        }
        else if (key == "blue"){
            colorTextView.text = color.toString()
            ColorManager().textBoxColorChanger(colorTextView, 0, 0, color)
            colorTextView.setTextColor(Color.rgb(255, 255, 1-color))
        }
    }

    fun initialFileChecker(file: File, fileName: String, mContext: Context){
        if (!(file.exists())) {
            mContext.openFileOutput(fileName, Context.MODE_APPEND).use {
                it.write("000000000Black".toByteArray())
                it.close()
            }
        }
    }

}
