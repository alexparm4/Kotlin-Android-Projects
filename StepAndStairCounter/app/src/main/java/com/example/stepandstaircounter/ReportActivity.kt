package com.example.stepandstaircounter

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import kotlinx.android.synthetic.main.activity_report.*

//https://www.raywenderlich.com/500-introduction-to-android-activities-with-kotlin
//helpful for new activity
class ReportActivity : AppCompatActivity() {
    private var viewStairsFlag = false
    private var flatFileName = "flatFile.txt"
    private var stairsFileName = "stairsFile.txt"
    private var stepCounter = 0
    private var holdTime = ""
    private var holdX = ""
    private var holdY = ""
    private var holdZ = ""
    private var list: MutableList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        var flightCounter = intent.getStringExtra("flightCount")
        report_flightcounter_tv.setText(flightCounter + " flights")

        val array: Array<String> = FileManager().fileStringToArray(flatFileName, this)

        if (array[0] != "Run a Trip First") {
            loadValuesFromArray(array)
            adapterSetter()
        }
        else{
            fillUIWithEmptyData()
        }

        report_step_tv.setText(intent.getStringExtra("flatSteps") + " steps")
        report_switch_title_tv.setTextColor(Color.rgb(14,160,43))

        return_btn.setOnClickListener {
            //TODO: add seperate delete button, if/when add saving reports feature
            deleteFile(flatFileName)
            deleteFile(stairsFileName)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        switch_btn.setOnClickListener {
            list = arrayListOf()
            stepCounter = 0

            if (viewStairsFlag == true){
                adapterSetter()
                val array: Array<String> = FileManager().fileStringToArray(flatFileName, this)
                loadValuesFromArray(array)
                switch_btn.setText(R.string.stairs_title)
                report_switch_title_tv.setText(R.string.flat_title)
                report_switch_title_tv.setTextColor(getColor(R.color.myGreen))
                report_step_tv.setText(intent.getStringExtra("flatSteps") + " steps")
                switchVisibilityForFlightCounter()
                viewStairsFlag = false
            }
            else{
                report_step_tv.visibility = View.VISIBLE
                adapterSetter()
                val array: Array<String> = FileManager().fileStringToArray(stairsFileName, this)
                loadValuesFromArray(array)
                switch_btn.setText(R.string.flat_title)
                report_switch_title_tv.setText(R.string.stairs_title)
                report_switch_title_tv.setTextColor(Color.rgb(183,35,27))
                report_step_tv.setText(intent.getStringExtra("stairsSteps") + " stairs")
                switchVisibilityForFlightCounter()
                viewStairsFlag = true
            }
        }
    }

//TODO: remove fileNames from adapters, rename adapters as builders
    fun listViewAdapter(): ListAdapter
            = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)

    private fun adapterSetter(){
        val adapter = listViewAdapter()
        listView.adapter = adapter
    }

    private fun loadValuesFromArray(array: Array<String>){
        var xSum = 0.toFloat()
        var ySum = 0.toFloat()
        var zSum = 0.toFloat()
        var minX: Float = 101.toFloat()      //Init mins to 101, above max value to ensure rewritten
        var minY: Float = 101.toFloat()
        var minZ: Float = 101.toFloat()
        var maxX: Float = (-101).toFloat()     //Init maxs to -101, below min to ensure it's rewritten
        var maxY: Float = (-101).toFloat()
        var maxZ: Float = (-101).toFloat()

        var index = 0
        while (index < array.size - 1){
            //Time
            holdTime = array[index]
            index++

            //X
            holdX = array[index]
            xSum += array[index].toFloat()
            minX = Utilities().minFinder(minX, array[index].toFloat())
            maxX = Utilities().maxFinder(maxX, array[index].toFloat())
            index++

            //Y
            holdY = array[index]
            ySum += array[index].toFloat()
            minY = Utilities().minFinder(minY, array[index].toFloat())
            maxY = Utilities().maxFinder(maxY, array[index].toFloat())
            index++

            //Z
            holdZ = array[index]
            zSum += array[index].toFloat()
            minZ = Utilities().minFinder(minZ, array[index].toFloat())
            maxZ = Utilities().maxFinder(maxZ, array[index].toFloat())

            Utilities().textViewUpdater(minX, minY, minZ, report_min_x_tv, report_min_y_tv, report_min_z_tv)
            Utilities().textViewUpdater(maxX, maxY, maxZ, report_max_x_tv, report_max_y_tv, report_max_z_tv)
            if ((array.size/4) > list.size) {
                list.add(holdTime + " sec \t X: " + holdX + " \t Y: " + holdY + " \t Z: " + holdZ)
            }
            index ++
        }

        if (array[0] != "Run a Trip First") {
            var avgX = Utilities().averageCalculatorFromArray(xSum, array.size)
            var avgY = Utilities().averageCalculatorFromArray(ySum, array.size)
            var avgZ = Utilities().averageCalculatorFromArray(zSum, array.size)
            Utilities().textViewUpdater(avgX, avgY, avgZ, report_avg_x_tv, report_avg_y_tv, report_avg_z_tv)
        }
        else {
            fillUIWithEmptyData()
        }
    }

    private fun fillUIWithEmptyData(){
        val noDataList = listOf("No data found.","Begin a trip to get raw data,", "click 'Return' to start another trip.")
        val noDataAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, noDataList)
        listView.adapter = noDataAdapter

        report_min_x_tv.setText(R.string.x_label)
        report_min_y_tv.setText(R.string.y_label)
        report_min_z_tv.setText(R.string.z_label)
        report_max_x_tv.setText(R.string.x_label)
        report_max_y_tv.setText(R.string.y_label)
        report_max_z_tv.setText(R.string.z_label)
        report_avg_x_tv.setText(R.string.x_label)
        report_avg_y_tv.setText(R.string.y_label)
        report_avg_z_tv.setText(R.string.z_label)
    }
    private fun switchVisibilityForFlightCounter(){
        if (report_flightcounter_tv.visibility == View.INVISIBLE){
            report_flightcounter_tv.visibility = View.VISIBLE
        }
        else{
            report_flightcounter_tv.visibility = View.INVISIBLE
        }
    }
}