package com.example.colorblender

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast

import kotlinx.android.synthetic.main.content_main.*

//::ColorBlender allows the user to import two colors from ColorPicker, and blend the colors together using a seek bar
//::::Written by Alex Parmentier::::

class ColorBlenderMain : AppCompatActivity() {
    private val REQUEST_CODE_ONE= 2501
    private val REQUEST_CODE_TWO= 2502
    var RIGHT_COLOR = "0000000000"
    var LEFT_COLOR = "0000000000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val importColorAction = getString(R.string.color_action)

        blenderBar.max = 100
        blenderBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val colorPercents = UtilitiesManager().colorsPercentageLister(progress)

                PercentageTextManager().percentageValueUpdater(color1_dynamicview, color2_dynamicview, progress)
                ColorManager().dynamicViewTextColorer(color1_dynamicview, RIGHT_COLOR, colorPercents[0])
                ColorManager().dynamicViewTextColorer(color2_dynamicview, LEFT_COLOR, colorPercents[1])
                ColorManager().dynamicViewBackgroundColorer(color1_dynamicview, RIGHT_COLOR, colorPercents[0])
                ColorManager().dynamicViewBackgroundColorer(color2_dynamicview, LEFT_COLOR, colorPercents[1])
                ColorManager().blenderViewColorer(blendView, progress, RIGHT_COLOR, LEFT_COLOR)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        importColor1.setOnClickListener{
            UtilitiesManager().colorImporter(REQUEST_CODE_ONE, importColorAction, this)
            }

        importColor2.setOnClickListener{
            UtilitiesManager().colorImporter(REQUEST_CODE_TWO, importColorAction, this)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //Code based on tutorial for initial functionality: https://www.youtube.com/watch?v=5wbeWN4hQt0
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val colorPercents = UtilitiesManager().colorsPercentageLister(blenderBar.progress)

        when(requestCode){
            REQUEST_CODE_ONE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    RIGHT_COLOR = data.getStringExtra(getString(R.string.intent_color_value_key))
                }
                ColorManager().staticViewColorer(color1_staticview, RIGHT_COLOR)
                ColorManager().dynamicViewBackgroundColorer(color1_dynamicview, RIGHT_COLOR, colorPercents[0])
                ColorManager().blenderViewColorer(blendView, blenderBar.progress, RIGHT_COLOR, LEFT_COLOR)
                ColorManager().dynamicViewTextColorer(color1_dynamicview, RIGHT_COLOR, colorPercents[0])
                }

            REQUEST_CODE_TWO -> {
                if (resultCode == Activity.RESULT_OK && data != null){
                    LEFT_COLOR = data.getStringExtra(getString(R.string.intent_color_value_key))
                    }
                ColorManager().staticViewColorer(color2_staticview, LEFT_COLOR)
                ColorManager().dynamicViewBackgroundColorer(color2_dynamicview, LEFT_COLOR, colorPercents[1])
                ColorManager().blenderViewColorer(blendView, blenderBar.progress, RIGHT_COLOR, LEFT_COLOR)
                ColorManager().dynamicViewTextColorer(color2_dynamicview, LEFT_COLOR, colorPercents[1])
                }
            
            else -> {
                Toast.makeText(this, getString(R.string.request_code_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
