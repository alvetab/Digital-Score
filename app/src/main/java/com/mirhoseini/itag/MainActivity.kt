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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.mirhoseini.itag.R.layout.main
import com.mirhoseini.itag.miband2.Main
import com.mirhoseini.itag.miband2.Main1
import com.mirhoseini.itag.pulsadores.devices.DevicesActivity
import com.polidea.rxandroidble.RxBleDevice
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

  //  private val TAG: String = BLEService::class.java.simpleName
    var currentButtonActionId = 0
    var currentButtonPressCount = 0
    var currentButtonPressTime:Long = 0
    var currentButtonTimerActivationTime = 0L
    val buttonPressMaxDelay =1000
    val buttonActionDelay =0
    val requiredButtonPressCount =2
    val cantidadDePulseras=1
    var prefsName = "Player 1"


   // private lateinit var dateFormat: SimpleDateFormat
   // private var bleService: BLEService? = null
    //private var bleServiceBound = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //configurar mac marcador
        val mac_marcador = findViewById<View>(R.id.MAC_marcador) as EditText //find player two name by id
           // obtains the SharedPreferences object
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val macmarcador = prefs.getString("MAC_marcador", null)

        if (macmarcador != null && macmarcador != null) {
            // writes to the players name
            mac_marcador.setText(macmarcador)

        }


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
        action.setOnClickListener {
            val intent = Intent(this, Main1::class.java)
            startActivity(intent)

        }
       // printLog("app started")
    }



    public override fun onPause() {
        super.onPause()
        // obtains the SharedPreferences object
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        // obtains an editor to it - needed only for writing
        val editor = prefs.edit()

        val mac_marcador = findViewById<View>(R.id.MAC_marcador) as EditText

        // includes or changes the value of variables
        editor.putString("MAC_marcador", mac_marcador.text.toString())


        // we need to commit at the end
        editor.commit()
    }

    public override fun onDestroy() {
       super.onDestroy()
       System.exit(0)
    }

}
