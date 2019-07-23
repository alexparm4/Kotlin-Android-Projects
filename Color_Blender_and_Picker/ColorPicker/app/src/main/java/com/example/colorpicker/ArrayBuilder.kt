package com.example.colorpicker

import java.io.File

class ArrayBuilder {

//https://www.baeldung.com/kotlin-read-file For this readFile method:
    private fun readFileAsTextUsingInputStream(fileName: String)
            = File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)

    fun populateArrayFromFile(filePath: String):Array<String>{
        var str = ArrayBuilder().readFileAsTextUsingInputStream(filePath)
        return str.split("\n").toTypedArray()
    }
}