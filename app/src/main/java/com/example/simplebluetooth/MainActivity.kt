package com.example.simplebluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import splitties.toast.toast

class MainActivity : AppCompatActivity() {

    private var list = ArrayList <String> ()
    private lateinit var adapter : ArrayAdapter<String>
    private lateinit var mBluetooth: BluetoothAdapter
    private var discoveredDevices = arrayListOf<String>()

    private val lvBluetooth: ListView by lazy { findViewById(R.id.listViewGeräte) }
    private val btnKoppeln: Button by lazy { findViewById(R.id.buttonGekoppelteGeräte) }
    private val btnSuche: Button by lazy { findViewById(R.id.buttonSucheGeräte) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBluetooth = BluetoothAdapter.getDefaultAdapter()
        if(mBluetooth == null)
        {
            toast(getString(R.string.bt_not_available))
            finish();
        }

        btnKoppeln.setOnClickListener{
            getPairedDevices()
        }

        btnSuche.setOnClickListener {
            checkBTPermission()
            getDiscoverDevices()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!mBluetooth.isEnabled) {
            val turnBTOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTOn, 1)
        }
    }

    private fun getPairedDevices() {

        val pairedDevices = mBluetooth!!.bondedDevices
        val list = ArrayList<Any>()

        if (pairedDevices.size > 0) {
            for (bt in pairedDevices) {
                list.add("""${bt.name}${bt.address}""".trimIndent())
            }
        } else {
            toast(getString(R.string.bt_no_paired_devices))
        }

        val adapter: ArrayAdapter<*> = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            list)
        lvBluetooth.adapter = adapter
    }

    private fun checkBTPermission() {
        var permissionCheck = checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
        permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
        if (permissionCheck != 0) {
            requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 1001)
        }
    }

    private fun getDiscoverDevices() {
        if(!mBluetooth.isDiscovering) { // Suche ist nicht gestartet
            mBluetooth.startDiscovery();  // starte Suche
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND) //auf diese Signale soll unser Broadcast Receiver filtern
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent)
            btnSuche.text = getString(R.string.buttonStoppSucheGeräte);
        } else {                        // Suche ist gestartet
            mBluetooth.cancelDiscovery(); // Stoppe suche
            unregisterReceiver(mBroadcastReceiver);
            btnSuche.text = getString(R.string.buttonSucheGeräte);
        }
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceInfo = """${device!!.name}${device.address}""".trimIndent()
                Log.i(TAG, deviceInfo)

                // gefundenes Gerät der Liste hinzufügen, wenn es noch nicht aufgeführt ist
                if (!discoveredDevices.contains(deviceInfo)) {
                    discoveredDevices.add(deviceInfo)
                }

                // aktualisierte Liste im Listview anzeigen
                val adapt = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, discoveredDevices)
                lvBluetooth.adapter = adapt
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
        mBluetooth.cancelDiscovery()
    }
}