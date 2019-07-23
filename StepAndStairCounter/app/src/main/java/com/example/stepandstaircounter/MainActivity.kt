package com.example.stepandstaircounter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.hardware.SensorEvent
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Math.abs

//TODO: ? -> determine flights by changing values? ie once flat to stairs. probably too much too implement.

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var rawX: Float = 0.toFloat()
    private var rawY: Float = 0.toFloat()
    private var rawZ: Float = 0.toFloat()
    private var stairsFlightCounter = 0
    private var tripTimer: Long = 0
    private var tripStartTime: Long = 0
    private var flatStepCounter = 0
    private var stairsStepCounter = 0
    var lastX = 0.toFloat()
    var lastY = 0.toFloat()
    var lastZ = 0.toFloat()
    var diffX = 0.toFloat()
    var diffY = 0.toFloat()
    var diffZ = 0.toFloat()
    var oldTime = 0.toFloat()
    var newTime = 0.toFloat()
    private var walkSpeed = (.55).toFloat()

    private var isFlat = true
    private var tripInProgress = false
    private var setTripStartTimeFlag = false
    private var flatFlag = false
    private var stairsFlag = false
    private var flatFileName = "flatFile.txt"
    private var stairsFileName = "stairsFile.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        endStartTrip_btn.setBackgroundColor(getColor(R.color.myGreen))


        flat_start_btn.setOnClickListener {
            flatStepCounter = Utilities().stepCounterCleaner(flatStepCounter)
            flat_start_btn.visibility = View.INVISIBLE
            stairs_start_btn.visibility = View.VISIBLE
            stairs_tv.setBackgroundColor(Color.rgb(255,255,255))
            flat_tv.setBackgroundColor(Color.rgb(0,0,0))
            isFlat = true
            flatFlag = true
            lastXYZValuesResetter()
            tripFlagSetters()
            sensorManager.unregisterListener(this, accelSensor)
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        stairs_start_btn.setOnClickListener {
            stairsStepCounter = Utilities().stepCounterCleaner(stairsStepCounter)
            stairs_start_btn.visibility = View.INVISIBLE
            flat_start_btn.visibility = View.VISIBLE
            stairs_tv.setBackgroundColor(Color.rgb(0,0,0))
            flat_tv.setBackgroundColor(Color.rgb(255,255,255))
            isFlat = false
            stairsFlag = true
            lastXYZValuesResetter()
            tripFlagSetters()
            sensorManager.unregisterListener(this, accelSensor)
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        endStartTrip_btn.setOnClickListener {
            if (tripInProgress == true) {
                endStartTrip_btn.setBackgroundColor(getColor(R.color.myGreen))
                hideButtonsBeforeTrip()
                report_btn.visibility = View.VISIBLE
                sensorManager.unregisterListener(this, accelSensor)
                endStartTrip_btn.setText(R.string.start_trip_btn)
                tripInProgress = false
            } else {
                endStartTrip_btn.setBackgroundColor(getColor(R.color.myGray))
                showButtonsDuringTrip()
                resetCounters()
                flat_step_tv.setText(flatStepCounter.toString() + " steps")
                stairs_step_tv.setText(stairsStepCounter.toString() + " stairs")
                flight_counter_tv.setText(flightCalculator() + " flights")
                flushTextViews()
                flat_start_btn.visibility = View.VISIBLE
                stairs_start_btn.visibility = View.VISIBLE
                report_btn.visibility = View.INVISIBLE
                endStartTrip_btn.setText(R.string.end_trip_btn)
                Toast.makeText(this,"Click 'Start Flat' or 'Start Stairs' to begin.", Toast.LENGTH_SHORT).show()
            }
        }

        report_btn.setOnClickListener {
            if (flatFlag == false){
                FileManager().initialFileChecker(flatFileName, this)
            }
            else if (stairsFlag == false){
                FileManager().initialFileChecker(stairsFileName, this)
            }
            flatFlag = false
            stairsFlag  = false

            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("flatSteps", flatStepCounter.toString())
            intent.putExtra("stairsSteps", stairsStepCounter.toString())
            //TODO: add flight to report
            intent.putExtra("flightCount", flightCalculator())
            startActivity(intent)
        }

        slow_btn.setOnClickListener {
            Utilities().walkSpeedButtonBGColorUpdater(this, slow_btn, med_btn, fast_btn)
            walkSpeed = (0.8).toFloat()
            walkspeed_display_tv.setText("0.80 sec / step")
        }
        med_btn.setOnClickListener {
            Utilities().walkSpeedButtonBGColorUpdater(this, med_btn, slow_btn, fast_btn)
            walkSpeed = (0.55).toFloat()
            walkspeed_display_tv.setText("0.55 sec / step")
        }
        fast_btn.setOnClickListener {
            Utilities().walkSpeedButtonBGColorUpdater(this, fast_btn, slow_btn, med_btn)
            walkSpeed = (0.4).toFloat()
            walkspeed_display_tv.setText("0.40 sec / step")
        }
    }

    override
    fun onSensorChanged(event: SensorEvent) {
        tripTimer = event.timestamp / 100000000 //milliseconds
        newTime = totalTripTimeFinder().toFloat()
        timestamp_tv.setText(totalTripTimeFinder() + " seconds")
        rawX = event.values[0]
        rawY = event.values[1]
        rawZ = event.values[2]
        Toast.makeText(this,event.timestamp.toString(), Toast.LENGTH_LONG).show()

        if (setTripStartTimeFlag == true) {
            deleteFile("flatFile.txt")
            deleteFile("stairsFile.txt")
            tripStartTime = event.timestamp / 100000000 //seconds
            //TODO: reimplement following lines to load into spinner to allow different sets of data to load
//            val currentDateTime = LocalDateTime.now().toString().replace('.','_', false).replace(':', '_', false)
//            flatFileName = currentDateTime + "_flat.txt"
//            stairsFileName = currentDateTime + "_stairs.txt"
            lastValToNewVal()
            endStartTrip_btn.setBackgroundColor(getColor(R.color.myRed))
            setTripStartTimeFlag = false
        }

        absDifferenceCalculator()

        if (((diffX + diffY + diffZ)> 4.5) && ((newTime - oldTime) >= walkSpeed) && (isFlat == true)){
            flatStepCounter++
            oldTime = newTime
            flat_step_tv.setText(flatStepCounter.toString() + " steps")
            lastValToNewVal()
        }
        else if (((diffX + diffY + diffZ)> 6.8) && ((newTime - oldTime) >= (walkSpeed + .15)) && (isFlat == false)){
            stairsStepCounter++
            oldTime = newTime
            stairs_step_tv.setText(stairsStepCounter.toString() + " stairs")
            flight_counter_tv.setText(flightCalculator().toString() + " flights")
            lastValToNewVal()
        }

//        minMaxSetter()
        tvUpdater()
        FileManager().fileWriter(this, isFlat, totalTripTimeFinder(), rawX, rawY, rawZ)

    }

    override
    fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    private fun tvUpdater() {
        if (isFlat) {
            Utilities().textViewUpdater(rawX, rawY, rawZ, flat_raw_x_tv, flat_raw_y_tv, flat_raw_z_tv)
        } else {
            Utilities().textViewUpdater(rawX, rawY, rawZ, stairs_raw_x_tv, stairs_raw_y_tv, stairs_raw_z_tv)
        }
    }

    private fun tripFlagSetters() {
        if (tripInProgress == false) {
            tripInProgress = true
            setTripStartTimeFlag = true
        }
    }

    fun totalTripTimeFinder(): String {
        val timer = (tripTimer - tripStartTime)
        return (timer.toDouble() / 10).toString()
    }

    private fun flushTextViews() {
        timestamp_tv.setText(R.string.initial_timer)
        Utilities().flushTextViews(flat_raw_x_tv, flat_raw_y_tv, flat_raw_z_tv)
        Utilities().flushTextViews(stairs_raw_x_tv, stairs_raw_y_tv, stairs_raw_z_tv)
    }

    private fun lastXYZValuesResetter(){
        lastX = 0.toFloat()
        lastY = 0.toFloat()
        lastZ = 0.toFloat()
    }

    private fun hideButtonsBeforeTrip(){
        walkspeed_title_tv.visibility = View.INVISIBLE
        slow_btn.visibility = View.INVISIBLE
        med_btn.visibility = View.INVISIBLE
        fast_btn.visibility = View.INVISIBLE
        walkspeed_display_tv.visibility = View.INVISIBLE
        flat_start_btn.visibility = View.INVISIBLE
        stairs_start_btn.visibility = View.INVISIBLE
    }

    private fun showButtonsDuringTrip(){
        walkspeed_title_tv.visibility = View.VISIBLE
        walkspeed_display_tv.visibility = View.VISIBLE
        slow_btn.visibility = View.VISIBLE
        med_btn.visibility = View.VISIBLE
        fast_btn.visibility = View.VISIBLE
    }

    private fun lastValToNewVal(){
        lastX = rawX
        lastY = rawY
        lastZ = rawZ
    }

    private fun absDifferenceCalculator(){
        diffX = abs(lastX - rawX)
        diffY = abs(lastY - rawY)
        diffZ = abs(lastZ - rawZ)
    }

    private fun flightCalculator():String {
        var calculation = stairsStepCounter.toFloat() / 12
        return ("%.2f".format(calculation))
    }

    private fun resetCounters(){
        flatStepCounter = 0
        stairsStepCounter = 0
        stairsFlightCounter = 0
        oldTime = 0.toFloat()
    }
}