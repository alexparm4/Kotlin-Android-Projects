//TODO: if want textview, then just keep a classwide string var to hold the
// textview contents and keep appending
package com.example.flashbeepshake_inclass

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator
import android.widget.Toast
import android.os.VibrationEffect
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.util.*

class MainActivity : AppCompatActivity() {
    private var myUUID: UUID? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null //holds the Bluetooth Adapter
//    private var mTextarea: TextView = findViewById(R.id.displayTextView)               //for writing messages to screen
    private var server: AcceptThread? = null                //server object
    private var client:ConnectThread? = null
    private var theMessage = ""
    private var lightSwitch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myUUID = UUID.fromString(MY_UUID_STRING) //unique UUID (or GUID) for this App
        setUpButtons()
    }

    private fun setUpButtons(){
        flashlight_button.setOnClickListener{
            lightSwitch = FlashLightHandler().lightSwitch(this, lightSwitch)
            theMessage = "flash"
        }

        val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        beep_button.setOnClickListener{
            toneG.startTone(ToneGenerator.TONE_PROP_BEEP)
            theMessage = "beep"
        }

        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrate_button.setOnClickListener {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            theMessage = "shake"
        }

        val scanButton = findViewById(R.id.scan_button) as Button?
        scanButton?.setOnClickListener {       //Scanning is the action performed by the client
            getPairedDevices()
            setUpBroadcastReceiver()
        }

        val connectButton = findViewById(R.id.connect_button) as Button?
        connectButton?.setOnClickListener {    //This button activates the App as the server
            Log.i(TSERVER, "Connect Button setting up server")
//            mTextarea!!.append("Connect Button: setting up server\n")
            //make server discoverable for N_SECONDS
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, N_SECONDS)
            startActivity(discoverableIntent)
            //create server thread
            server = AcceptThread()
            if (server != null) {   //start server thread
                Log.i(TSERVER, "Connect Button spawning server thread")
                //        mTextarea!!.append("Connect Button: spawning server thread $server \n")
                server!!.start()     //calls AcceptThread's run() method
            } else {
                Log.i(TSERVER, "setupButtons(): server is null")
            }
        }

        val cancelButton = findViewById(R.id.cancel_action) as Button?
        cancelButton?.setOnClickListener {
            //          mTextarea!!.append("Cancelling connection with server\n")
            theMessage = "cancel"

        }
    }
    //Added Bluetooth Functions

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(LOG_TAG, "BroadcastReceiver onReceive()")
            handleBTDevice(intent)
        }
    }//when activated, scans for Bluetooth devices -- looks to see which ones it can detect

    public override fun onResume() {
        super.onResume()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.i(LOG_TAG, "onResume()")
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.i(LOG_TAG, "No Bluetooth on this device")
            Toast.makeText(this@MainActivity,
                "No Bluetooth on this device", Toast.LENGTH_LONG).show()
        } else if (!mBluetoothAdapter!!.isEnabled) {
            Log.i(LOG_TAG, "enabling Bluetooth")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        Log.i(LOG_TAG, "End of onResume()")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.i(LOG_TAG, "onActivityResult(): requestCode = $requestCode")
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(LOG_TAG, "  --    Bluetooth is enabled")
                getPairedDevices() //find already known paired devices
                setUpBroadcastReceiver()
            }
        }
    }

    private fun getPairedDevices() {//find already known paired devices
        val pairedDevices = mBluetoothAdapter!!.bondedDevices
        Log.i(TCLIENT, "--------------\ngetPairedDevices() - Known Paired Devices")
        // If there are paired devices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                Log.i(TCLIENT, device.name + "\n" + device)
            }
        }
        Log.i(TCLIENT, "getPairedDevices() - End of Known Paired Devices\n------")
    }

    private fun setUpBroadcastReceiver() {
        // Create a BroadcastReceiver for ACTION_FOUND
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)    {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION)
            Log.i(TCLIENT,"Getting Permission")
            return
            //Discovery will be setup in onRequestPermissionResult() if permission is granted
        }
        setupDiscovery()
    }

    /**
     * Callback when request for permission is addressed by the user.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, "Fine_Location Permission granted")
                    setupDiscovery()
                } else {    //tracking won't happen since user denied permission
                    Log.i(LOG_TAG, "Fine_Location Permission refused")
                }
                return
            }
        }
    }

    /**
     * Activate Bluetooth discovery for the client
     */
    private fun setupDiscovery() {
        Log.i(TCLIENT,"Activating Discovery")
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)
        mBluetoothAdapter!!.startDiscovery()
    }

    /**
     * called by BroadcastReceiver callback when a new BlueTooth device is found
     */
    private fun handleBTDevice(intent: Intent) {
        Log.i(TCLIENT, "handleBRDevice() -- starting   <<<<--------------------")
        val action = intent.action
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND == action) {
            // Get the BluetoothDevice object from the Intent
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val deviceName  =
                if (device.name != null) {
                    device.name.toString()
                } else {
                    "--no name--"
                }
            Log.i(TCLIENT, deviceName + "\n" + device)
    //        mTextarea!!.append("$deviceName, $device \n")

            // The following is specific to this App for the client
            if (deviceName.length > 3) { //for now, looking for APm prefix
                val prefix = deviceName.subSequence(0,3)
         //       mTextarea!!.append("Prefix = $prefix\n    ")
                if (prefix == "APm") {//This is the server
                    Log.i(TCLIENT,"Canceling Discovery")
                    mBluetoothAdapter!!.cancelDiscovery()
                    Log.i(TCLIENT,"Connecting")
                    client = ConnectThread(device)  //FIX** remember and reconnect if interrupted?
                    Log.i(TCLIENT,"Running Connect Thread")
                    client?.start()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mBluetoothAdapter!!.cancelDiscovery()    //stop looking for Bluetooth devices
        client?.cancel()
    }

    ////////////////// Client Thread to talk to Server here ///////////////////

    private inner class ConnectThread(mmDevice: BluetoothDevice):Thread(){//from android developer
    private var mmSocket: BluetoothSocket? = null

        init {
            Log.i(TCLIENT, "ConnectThread: init()")
            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(myUUID)
            } catch (e: IOException) {
                Log.i(TCLIENT, "IOException when creating RFcommSocket\n $e")
            }
        }

        override fun run() {
            Log.i(TCLIENT, "ConnectThread: run()")
            Log.i(TCLIENT, "in ClientThread - Canceling Discovery")
            mBluetoothAdapter!!.cancelDiscovery()
            if (mmSocket == null) {
                Log.e(TCLIENT,"ConnectThread:run(): mmSocket is null")
            }
            try {
                Log.i(TCLIENT, "Connecting to server")
                mmSocket!!.connect()
            } catch (connectException: IOException) {
                Log.i(TCLIENT,
                    "Connect IOException when trying socket connection\n $connectException")
                try {
                    mmSocket!!.close()
                } catch (closeException: IOException) {
                    Log.i(TCLIENT,
                        "Close IOException when trying socket connection\n $closeException")
                }

                return
            }
            Log.i(TCLIENT, "Connection Established")
            val sock = mmSocket!!
            manageConnectedSocket(sock)
        }

        //manage the connection over the passed-in socket
        private fun manageConnectedSocket(socket: BluetoothSocket) {
            val out: OutputStream
            val msg = theMessage.toByteArray()
            try {
                Log.i(TCLIENT, "Sending the message: [$theMessage]")
                out = socket.outputStream
                out.write(msg)
                if (theMessage == "cancel"){
                    cancel()
                    Toast.makeText(baseContext, "Cancelling the connection with device", Toast.LENGTH_LONG).show()
                }
            } catch (ioe: IOException) {
                Log.e(TCLIENT, "IOException when opening outputStream\n $ioe")
                return
            }
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (ioe: IOException) {
                Log.e(TCLIENT, "IOException when closing outputStream\n $ioe")
            }
        }
    }

    ///////////////////////////////  ServerSocket stuff here ///////////////////////////

    private inner class AcceptThread : Thread() {  //from android developer
        private var mmServerSocket: BluetoothServerSocket? = null

        init {
            val tmp: BluetoothServerSocket
            try {
                tmp = mBluetoothAdapter!!.listenUsingRfcommWithServiceRecord(SERVICE_NAME, myUUID)
                Log.i(TSERVER, "AcceptThread registered the server\n")
                mmServerSocket = tmp
            } catch (e: IOException) {
                Log.e(TSERVER, "AcceptThread registering the server failed\n $e")
            }
        }

        override fun run() {
            var socket: BluetoothSocket?
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                Log.i(TSERVER, "AcceptTread.run(): Server Looking for a Connection")
                try {
                    socket = mmServerSocket!!.accept()  //block until connection made or exception
                    Log.i(TSERVER, "Server socket accepting a connection")
                } catch (e: IOException) {
                    Log.e(TSERVER, "socket accept threw an exception\n $e")
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    Log.i(TSERVER, "Server Thread run(): Connection accepted")
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket)
                    break
                } else {
                    Log.i(TSERVER, "Server Thread run(): The socket is null")
                }
            }
        }

        //manage the Server's end of the conversation on the passed-in socket
        fun manageConnectedSocket(socket: BluetoothSocket) {
            Log.i(TSERVER, "\nManaging the Socket\n")
            val inSt: InputStream
            val nBytes: Int
            val msg = ByteArray(255) //arbitrary size
            try {
                inSt = socket.inputStream
                nBytes = inSt.read(msg)
                Log.i(TSERVER, "\nServer Received $nBytes \n")
            } catch (ioe: IOException) {
                Log.e(TSERVER, "IOException when opening inputStream\n $ioe")
                return
            }

//            try {
//                //inputStreamReader(inSt)
//                actionResponseFromMessage(nBytes.toString())
//            } catch (uee: UnsupportedEncodingException) {
//                Log.e(TSERVER,
//                    "UnsupportedEncodingException when converting bytes to String\n $uee")
//            } finally {
//            }

            try {
                val msgString = msg.toString(Charsets.UTF_8)
                Log.i(TSERVER, "\nServer Received  $nBytes, Bytes:  [$msgString]\n")
                //runOnUiThread { echoMsg("\nReceived $nBytes:  [$msgString]\n") }
                actionResponseFromMessage(msgString)
            } catch (uee: UnsupportedEncodingException) {
                Log.e(TSERVER,
                    "UnsupportedEncodingException when converting bytes to String\n $uee")
            } finally {
                //cancel()        //for this App - close() after 1 (or no) message received
            }

        }

//        private fun inputStreamReader(inputStream: InputStream){
//            Log.i(TSERVER, "\nReading input stream from the Socket\n")
//            var inputAsString = ""
//            try {
//                inputAsString = inputStream.bufferedReader().use { it.readText() }  // defaults to UTF-8
//                actionResponseFromMessage(inputAsString)
//            } catch (ioe: IOException) {
//                Log.e(TSERVER, "IOException when opening inputStream\n $ioe")
//                return
//            }
//        }

        private fun actionResponseFromMessage(input: String){
            if (input == "flash"){
                lightSwitch = FlashLightHandler().lightSwitch(baseContext, lightSwitch)
                Toast.makeText(this@MainActivity, "Received Flash input", Toast.LENGTH_LONG).show()
            }
            else if (input == "beep"){
                val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneG.startTone(ToneGenerator.TONE_PROP_BEEP)
                Toast.makeText(this@MainActivity, "Received Beep input", Toast.LENGTH_LONG).show()
            }
            else if (input == "shake"){
                val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                Toast.makeText(this@MainActivity, "Received Vibrate input", Toast.LENGTH_LONG).show()
            }
            else if (input == "cancel"){
                cancel()
                Toast.makeText(this@MainActivity, "Server closed the connection", Toast.LENGTH_LONG).show()
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        fun cancel() {
            try {
                mmServerSocket!!.close()
            } catch (ioe: IOException) {
                Log.e(TSERVER, "IOException when canceling serverSocket\n $ioe")
            }
        }
    }

    companion object {
        private const val ACCESS_FINE_LOCATION = 1
        private const val N_SECONDS = 255
        private const val TCLIENT = "--Talker Client--"  //for Log.X
        private const val TSERVER = "--Talker Server--"  //for Log.X
        private const val REQUEST_ENABLE_BT = 3313  //our own code used with Intents
        private const val MY_UUID_STRING = "11d6954e-cb83-4f50-acd7-fcf910887fcf" //from www.uuidgenerator.net
        private const val SERVICE_NAME = "Talker"
        private const val LOG_TAG = "--Talker----"
    }
}