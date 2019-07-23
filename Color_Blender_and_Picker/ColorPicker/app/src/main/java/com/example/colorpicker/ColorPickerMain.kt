package com.example.colorpicker

import android.app.Activity
import android.content.Context
import android.os.Bundle

import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.content_main.*
import java.io.*

//::ColorPicker allows the user to find colors using 3 seek bars, corresponding to Red, Green and Blue values.
// The user can also save colors for later use.  This app allows other apps to implement it to choose a color
// and the RGB values will be exported as a string, such as "000000000" for black.
//::::Written by Alex Parmentier::::
class ColorPickerMain : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var redValue = 0
    var greenValue = 0
    var blueValue = 0

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fileName = getString(R.string.saved_colors_file_name)
        val filePath = getString(R.string.saved_colors_file_path)

        UtilityManager().initialFileChecker(File(filePath), fileName, this)
        var savedColorArray: Array<String> = ArrayBuilder().populateArrayFromFile(filePath)

        val info = intent
         if (info != null){
            if (info.action == "colorsuite.ACTION_COLOR"){
                exportButton!!.visibility = View.VISIBLE
                exportButton.setOnClickListener{
                    val colorString = UtilityManager().colorValuePadder(redValue) +
                                        UtilityManager().colorValuePadder(greenValue) +
                                        UtilityManager().colorValuePadder(blueValue)
                    intent.putExtra(getString(R.string.intent_colorstring_key), colorString)

                    setResult(Activity.RESULT_OK, intent)
                    super.finish()
                }
            }
        }

//::::Spinner Implementation::::
        SpinnerManager().populateSpinner(this, filePath, spinner)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long) {
                ColorManager().colorLoaderFromSpinner(savedColorArray, position, colorView, redBar, greenBar, blueBar)
            }

            override fun onNothingSelected(parent: AdapterView<*>){
            }
        }

//::::Save Button::::
        saveButton.setOnClickListener {
            var color_string: String = UtilityManager().colorValuePadder(redBar.progress) +
                                        UtilityManager().colorValuePadder(greenBar.progress) +
                                        UtilityManager().colorValuePadder(blueBar.progress)
            var textToWrite = custom_colors.text.toString()
            var fos: FileOutputStream? = null

            try{
                fos = openFileOutput(fileName, Context.MODE_APPEND)
                fos.write("\n".toByteArray() + color_string.toByteArray() + textToWrite.toByteArray())
                Toast.makeText(this, "Saved " + textToWrite, Toast.LENGTH_LONG).show()
            } catch (e: FileNotFoundException){
                e.printStackTrace()
            } catch (e: IOException){
                e.printStackTrace()
            } finally {
                if (fos != null){
                    fos.close()
                    savedColorArray = ArrayBuilder().populateArrayFromFile(filePath)
                    SpinnerManager().populateSpinner(this, filePath, spinner)
                    custom_colors.setText("")
                    UtilityManager().softKeyboardHider(this)
                }
            }
        }

//::::Seekbars for Color Values::::
        redBar.max = 255
        redBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                redValue = redBar.progress
                UtilityManager().smallRGBViewUpdater(redTextView, redValue, getString(R.string.key_red))
                ColorManager().bigColorViewChanger(colorView, redValue,greenValue,blueValue)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        greenBar.max = 255
        greenBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                greenValue = greenBar.progress
                ColorManager().bigColorViewChanger(colorView, redValue,greenValue,blueValue)
                UtilityManager().smallRGBViewUpdater(greenTextView, greenValue, getString(R.string.key_green))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
        blueBar.max = 255
        blueBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blueValue = blueBar.progress
                UtilityManager().smallRGBViewUpdater(blueTextView, blueValue, getString(R.string.key_blue))
                ColorManager().bigColorViewChanger(colorView, redValue,greenValue,blueValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}