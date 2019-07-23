package com.example.colorblender

import android.app.Activity
import android.content.Intent

class UtilitiesManager {
    fun stringToRGBIntLister(color: String): List<Int>{
        return listOf(color.substring(0,3).toInt(),
                        color.substring(3,6).toInt(),
                        color.substring(6,9).toInt())
    }

    fun colorsPercentageLister(barProgress:Int):List<Float>{
        var color1Percent: Float = barProgress.toFloat() / 100.toFloat()
        var color2Percent: Float = 1.toFloat() - color1Percent
        return listOf(color1Percent, color2Percent)
    }

    fun colorImporter(requestCode: Int, action: String, mActivity: Activity){
        val importColorIntent = Intent(action)
        importColorIntent.addCategory(Intent.CATEGORY_DEFAULT)
        mActivity.startActivityForResult(importColorIntent, requestCode)
    }

    fun percentOfOriginalCalculator(original: Int, percent: Float) =
        (original*percent).toInt()


}