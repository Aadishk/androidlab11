package com.example.exp10map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var isReceivingUpdates = false

    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var btnGetLocation: MaterialButton
    private lateinit var btnToggleUpdates: MaterialButton
    private lateinit var mapWebView: WebView
    private lateinit var tvMapPlaceholder: TextView

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            tvStatus.text = "Permission granted. Fetching location..."
            fetchCurrentLocation()
        } else {
            tvStatus.text = "Permission denied. Location access is required to render map."
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        setupWebView()
        setupListeners()
    }

    private fun initViews() {
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        btnGetLocation = findViewById(R.id.btnGetLocation)
        btnToggleUpdates = findViewById(R.id.btnToggleUpdates)
        mapWebView = findViewById(R.id.mapWebView)
        tvMapPlaceholder = findViewById(R.id.tvMapPlaceholder)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        mapWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                tvMapPlaceholder.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        btnGetLocation.setOnClickListener {
            if (hasLocationPermission()) {
                fetchCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }

        btnToggleUpdates.setOnClickListener {
            if (isReceivingUpdates) {
                stopLocationUpdates()
            } else {
                if (hasLocationPermission()) {
                    startLocationUpdates()
                } else {
                    requestLocationPermission()
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        showLoading(true)
        tvStatus.text = "Acquiring GPS location..."

        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            showLoading(false)
            if (location != null) {
                updateLocationUI(location)
            } else {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                    if (lastLoc != null) {
                        updateLocationUI(lastLoc)
                    } else {
                        tvStatus.text = "Unable to retrieve location. Please check if GPS is enabled."
                    }
                }.addOnFailureListener {
                    tvStatus.text = "Failed to fetch location: ${it.message}"
                }
            }
        }.addOnFailureListener { exception ->
            showLoading(false)
            tvStatus.text = "Error fetching location: ${exception.message}"
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setGranularity(Granularity.GRANULARITY_FINE)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocationUI(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        isReceivingUpdates = true
        btnToggleUpdates.setText(R.string.btn_stop_updates)
        btnToggleUpdates.setIconResource(android.R.drawable.ic_media_pause)
        tvStatus.text = "Receiving real-time location updates..."
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        isReceivingUpdates = false
        btnToggleUpdates.setText(R.string.btn_live_updates)
        btnToggleUpdates.setIconResource(android.R.drawable.ic_menu_rotate)
        tvStatus.text = "Real-time updates stopped."
    }

    private fun updateLocationUI(location: Location) {
        val latStr = String.format(Locale.US, "%.6f", location.latitude)
        val lngStr = String.format(Locale.US, "%.6f", location.longitude)
        val accStr = String.format(Locale.US, "± %.1f m", location.accuracy)
        val timeStr = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(location.time))

        tvLatitude.text = latStr
        tvLongitude.text = lngStr
        tvAccuracy.text = accStr
        tvStatus.text = "Updated at $timeStr"

        loadMapInWebView(location.latitude, location.longitude)
    }

    private fun loadMapInWebView(latitude: Double, longitude: Double) {
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    html, body {
                        margin: 0;
                        padding: 0;
                        width: 100%;
                        height: 100%;
                        overflow: hidden;
                        background-color: #f5f5f5;
                    }
                    iframe {
                        width: 100%;
                        height: 100%;
                        border: 0;
                    }
                </style>
            </head>
            <body>
                <iframe
                    src="https://maps.google.com/maps?q=$latitude,$longitude&z=15&output=embed"
                    allowfullscreen>
                </iframe>
            </body>
            </html>
        """.trimIndent()

        mapWebView.loadDataWithBaseURL(
            "https://maps.google.com",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnGetLocation.isEnabled = !isLoading
    }

    override fun onPause() {
        super.onPause()
        if (isReceivingUpdates) {
            stopLocationUpdates()
        }
    }
}