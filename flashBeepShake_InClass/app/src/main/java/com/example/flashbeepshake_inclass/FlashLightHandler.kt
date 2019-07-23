package com.example.flashbeepshake_inclass

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.widget.Toast

class FlashLightHandler {


    fun lightSwitch(mContext: Context, flashlightToggle: Boolean): Boolean{
        var newSwitch = flashlightToggle
        if(!newSwitch){
            flashLightOn(mContext)
            newSwitch = true
        }
        else if (newSwitch  == true) {
            flashLightOff(mContext)
            newSwitch = false
        }
        else{
            Toast.makeText(mContext, "No flash is available on your device", Toast.LENGTH_SHORT).show()
        }
        return newSwitch
    }

//    fun lightSwitchLongPress(mContext: Context, strobeSwitch: Boolean, button: Button): Boolean{
//        var newSwitch = strobeSwitch
//        if (!newSwitch){
//            button.setOnLongClickListener {
//                flashLightOn(mContext)
//                //return@setOnLongClickListener true
//            }
//
//        }
//        return true
//    }

    private fun flashLightOff(mContext: Context){
        val cameraManager: CameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try{
            val cameraID = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraID, false)
        } catch (e: CameraAccessException){ }
    }

    private fun flashLightOn(mContext: Context){
        val cameraManager: CameraManager = mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try{
            val cameraID = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraID, true)
        } catch (e: CameraAccessException){ }
    }
}