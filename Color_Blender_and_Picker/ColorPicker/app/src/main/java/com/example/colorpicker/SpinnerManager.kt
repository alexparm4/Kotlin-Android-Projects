package com.example.colorpicker

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner

class SpinnerManager{
    fun populateSpinner(mContext: Context, filePath: String, spinner: Spinner) {

        val adapter = ArrayAdapter(
            mContext,
            R.layout.simple_spinner_item,
            sanitizeArrayForSpinnerView(ArrayBuilder().populateArrayFromFile(filePath))
        )

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = adapter
    }

    private fun sanitizeArrayForSpinnerView(arr: Array<String>):Array<String>{
        var index = 0
        while (index < arr.size){
            if (arr[index].length < 9) {
                break
            }
            else {
                arr[index] = arr[index].substring(9)
                index++
            }
        }
        return arr
    }
}