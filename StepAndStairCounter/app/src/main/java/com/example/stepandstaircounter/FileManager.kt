package com.example.stepandstaircounter

import android.content.Context
import java.io.File

class FileManager {
    private var flatFileName = "flatFile.txt"
    private var stairsFileName = "stairsFile.txt"

    fun readFileAsTextUsingInputStream(fileName: String) =
        File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)

    //TODO: Buffer spaces here or before going to listview if needed to line up X Y Z
    //TODO: pretty up strings with tags after splitting into array
    fun fileWriter(mContext: Context, isFlat: Boolean, tripTime: String, rawX: Float, rawY: Float, rawZ: Float) {
        if (isFlat == true) {
            mContext.openFileOutput(flatFileName, Context.MODE_APPEND).use {
                val outputString =
                    tripTime + "\n" + rawX.toString() + "\n" + rawY.toString() + "\n" + rawZ.toString() + "\n"
                it.write(outputString.toByteArray())
                it.close()
            }
        } else {
            mContext.openFileOutput(stairsFileName, Context.MODE_APPEND).use {
                val outputString =
                    tripTime + "\n" + rawX.toString() + "\n" + rawY.toString() + "\n" + rawZ.toString() + "\n"
                it.write(outputString.toByteArray())
                it.close()
            }
        }
    }

    fun fileStringToArray(fileName: String, mContext: Context): Array<String> {
        var fileString = FileManager().readFileAsTextUsingInputStream(mContext.filesDir.toString() + "/" + fileName)
        return fileString.split("\n").toTypedArray()
    }

    fun initialFileChecker(fileName: String, mContext: Context){
        mContext.openFileOutput(fileName, Context.MODE_APPEND).use {
            it.write("Run a Trip First".toByteArray())
            it.close()
            }
    }
}