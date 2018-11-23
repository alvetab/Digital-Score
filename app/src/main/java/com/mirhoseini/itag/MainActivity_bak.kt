package com.mirhoseini.itag


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.mirhoseini.itag.pulsadores.devices.DevicesActivity
import com.polidea.rxandroidble.RxBleDevice
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity_bak : AppCompatActivity(), BLEView {

    private val TAG: String = BLEService::class.java.simpleName
    var currentButtonActionId = 0
    var currentButtonPressCount = 0
    var currentButtonPressTime:Long = 0
    var currentButtonTimerActivationTime = 0L
    val buttonPressMaxDelay =1000
    val buttonActionDelay =0
    val requiredButtonPressCount =2
    val cantidadDePulseras=1
    var prefsName = "Player 1"


    private lateinit var dateFormat: SimpleDateFormat
    private var bleService: BLEService? = null
    private var bleServiceBound = false

    //connect to the service
    private val bleServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? BLEService.ServiceBinder
            bleService = binder?.service

            bleService?.bindView(this@MainActivity_bak)

            bleServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleServiceBound = false
        }
    }

    fun onmarcadorClick(view: View) {
       // bleService?.pressKey()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateFormat = SimpleDateFormat("hh:mm:ss", Locale.US)

        // set scrolling method for log TextView
        log.movementMethod = ScrollingMovementMethod()
        GrabarMacs(); //captura las MAC de las pulseras si las hay

        action.setOnClickListener { bleService?.scan() }
        marcador.setOnClickListener {
            val intent = Intent(this,MainActivitymarcador::class.java)
            startActivity(intent)
            //bleService?.scan()
            }
        pulseras.setOnClickListener {
            val intent = Intent(this, Mainmiband2Activity::class.java)
            startActivity(intent)
            //bleService?.scan()
        }
        pulsadores.setOnClickListener {
            val intent = Intent(this, DevicesActivity::class.java)
            startActivity(intent)
            //bleService?.scan()
        }
        practicaclicks.setOnClickListener {
            val intent = Intent(this,PracticaActivity::class.java)
            startActivity(intent)
            //bleService?.scan()
        }
        printLog("app started")
    }

    override fun onResume() {
        super.onResume()
        if (checkAllRequiredPermissions()) {
            if (!bleServiceBound) {
                val bleServiceIntent = Intent(applicationContext, BLEService::class.java)
                applicationContext.bindService(bleServiceIntent, bleServiceConnection, Context.BIND_AUTO_CREATE)

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        context.startForegroundService(mainServiceIntent)
//                    } else {
                applicationContext.startService(bleServiceIntent)
//                    }
            }
        }
    }

    private fun checkAllRequiredPermissions(): Boolean {

        val requiredPermissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        )

        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_ALL_PERMISSIONS)
                return false
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_ALL_PERMISSIONS -> finishIfRequiredPermissionsNotGranted(grantResults)
            else -> {
            }
        }
    }

    private fun finishIfRequiredPermissionsNotGranted(grantResults: IntArray) {
        if (grantResults.isNotEmpty()) {
            for (grantResult in grantResults) {
                // If request is cancelled, the result arrays are empty.
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Required permissions not granted! We need them all!!!", Toast.LENGTH_LONG).show()
                    finish()
                    break
                }
            }
        } else {
            Toast.makeText(this, "Required permissions not granted! We need them all!!!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onScanning() {
        setAction("Scanning...")
        action.setOnClickListener(null)
        printLog("start scanning...")
    }

    override fun onConnecting(bleDevice: RxBleDevice) {
        printLog("found ${bleDevice.nameToString()})")
        setAction("Connecting...")
        printLog("connecting to ${bleDevice.nameToString()}")
    }

    override fun onConnected(bleDevice: RxBleDevice) {
        setAction("Connected")
        printLog("connected to ${bleDevice.nameToString()})")
    }

    override fun onRegistered() {
        printLog("registered!")
    }

    public override fun onPause() {
        super.onPause()
        // obtains the SharedPreferences object
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        // obtains an editor to it - needed only for writing
        val editor = prefs.edit()

        val MACpulsera1 = findViewById<View>(R.id.MAC_) as EditText
        val MACpulsera2 = findViewById<View>(R.id.MACEquipo2) as EditText

        // includes or changes the value of variables
        editor.putString("MACpulsera1", MACpulsera1.text.toString())
        editor.putString("MACpulsera2", MACpulsera2.text.toString())

        // we need to commit at the end
        editor.commit()
    }
    override fun onKeyPressed() {

        if (cantidadDePulseras==1){
           var timeSinceLastPress = System.currentTimeMillis() - currentButtonPressTime

            if ((currentButtonPressTime == 0L) || (timeSinceLastPress < buttonPressMaxDelay)) {
                currentButtonPressCount++
                printLog((currentButtonPressTime-timeSinceLastPress).toString())
                printLog(currentButtonPressCount.toString())
                currentButtonPressTime = System.currentTimeMillis();
            }


            else {/*
                if (currentButtonPressCount == 1) {
                printLog("Buton 1 vez presionado!!")
                currentButtonActionId=1;
                currentButtonPressCount = 0
                currentButtonPressTime =0

                }
                else if (currentButtonPressCount == 2) {
                printLog("Buton 2 veces presionado!!")
                currentButtonActionId=2;
                currentButtonPressCount = 0
                currentButtonPressTime =0

                }
                else if (currentButtonPressCount == 3) {
                printLog("Buton 3 veces presionado!!")
                currentButtonActionId=3;
                currentButtonPressCount = 0
                currentButtonPressTime =0

                }
                else if (currentButtonPressCount == 4) {
                printLog("Buton 4 veces presionado!!")
                currentButtonActionId=4;
                currentButtonPressCount = 0
                currentButtonPressTime =0
                }

                else {
                printLog("Buton presionado mas de 4 veces!!")
                currentButtonActionId = 5
                currentButtonPressCount =0;
                currentButtonPressTime =0
                //Log.i(TAG, "Botton presionado!!!")
                //val intent = Intent("com.mirhoseini.itag.button_pressed")
                //sendBroadcast(intent)

                }*/

                if (System.currentTimeMillis() > currentButtonPressTime+5000){
                    currentButtonPressCount=1
                    printLog((currentButtonPressTime-timeSinceLastPress).toString())
                    printLog(currentButtonPressCount.toString())
                    currentButtonPressTime = System.currentTimeMillis();
                }
                else {
                    printLog("boton validado y pulsado " + currentButtonPressCount.toString() + " veces")
                    currentButtonPressCount = 0
                    currentButtonPressTime = 0
                    printLog(currentButtonPressCount.toString())

                    //SecondActivity.PlayerOneWonpressed();
                }

            }


           }



            /*if (currentButtonPressCount == requiredButtonPressCount) {
                currentButtonTimerActivationTime = currentButtonPressTime;
                if (buttonActionDelay > 0) {
                    /* LOG.info("Activating timer");
                    final Timer buttonActionTimer = new Timer("Mi Band Button Action Timer");
                    buttonActionTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            runButtonAction();
                            buttonActionTimer.cancel();
                        }
                    }, buttonActionDelay, buttonActionDelay);*/
                } else {
                    /*LOG.info("Activating button action");
                    runButtonAction();*/
                    //Log.i(TAG, "Botton mas presionado!!!")
                    printLog("Buton mas presionado!!")
                    //val intent = Intent("com.mirhoseini.itag.button_pressed")
                    //sendBroadcast(intent)
                }
                currentButtonActionId++;
                currentButtonPressCount = 0;*/


        //printLog("Button pressed!!")
        }


    override fun onError(throwable: Throwable) {
        setAction("Scan")
        action.setOnClickListener { bleService?.scan() }

        printLog(throwable.message ?: throwable.toString())
    }

    private fun setAction(actionText: String) {
        action.post {
            action.text = actionText
        }
    }

    private fun printLog(message: String) {
        val date = Date()
        log.post {
            log.text = "${dateFormat.format(date)} - $message\n--------\n${log.text}"
        }
    }

    private fun RxBleDevice.nameToString() =
            "${this.name?.trim()}(${this.macAddress?.trim()})"

    companion object {
        private const val REQUEST_ALL_PERMISSIONS = 1001
    }
    fun GrabarMacs(){
        val MACEquipo1 = findViewById<View>(R.id.MAC_) as EditText
        val MACEquipo2 = findViewById<View>(R.id.MACEquipo2) as EditText

        // obtains the SharedPreferences object
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        // reads from sharedPreferences
        // The second argument defines the return value if it does not exist
        val MACpulsera1 = prefs.getString("MACpulsera1", null)
        var MACpulsera2 = prefs.getString("MACpulsera2", null)

        if (MACpulsera1 != null && MACpulsera2 != null) {
            // writes to the players name
            MACEquipo1.setText(MACpulsera1)
            MACEquipo2.setText(MACpulsera2)
        }
    }
}
