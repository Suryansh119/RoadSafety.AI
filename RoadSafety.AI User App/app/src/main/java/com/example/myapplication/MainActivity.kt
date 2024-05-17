package com.example.myapplication
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.myapplication.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.ExplainReasonCallback
import com.permissionx.guolindev.callback.RequestCallback
import com.robertlevonyan.views.customfloatingactionbutton.doOnExpand
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import org.json.JSONObject
import timber.log.Timber


import java.util.UUID
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var latitude: String = ""
    var longitude: String = ""

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }


    var person = PersonModel()
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                binding.spinKit.visibility = View.VISIBLE
                val fileUri = data?.data!!
                Firebase.storage.reference.child("Reporting/${UUID.randomUUID()}").putFile(fileUri)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            it.result.storage.downloadUrl.addOnSuccessListener {
                                person.ImageUrl = it.toString()
                                binding.uploadImage.setImageURI(fileUri)
                                binding.spinKit.visibility = View.INVISIBLE
                            }
                        }
                    }


            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

        }
        else {
            startLocationUpdates()
        }
        PermissionX.init(this@MainActivity).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
            .onExplainRequestReason(ExplainReasonCallback { scope, deniedList ->
                val message =
                    "We need your consent for the following permissions in order to use the offline call function properly"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }).request(RequestCallback { allGranted, grantedList, deniedList -> })

        binding.uploadImage.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)

                }
        }
        val application: Application = application
        val appID: Long = 1116811214 // Replace TODO() with your App ID
        val appSign: String = "6999b56a2e24bc64ec539273cf0c91389c6eb30d56182626e11691f16f3e3558" // Replace TODO() with your App Sign
        val userID: String = "119"// Replace TODO() with your User ID
        val userName: String = "Reporter"// Replace TODO() with your User Name
        initCallInviteService(appID, appSign, userID, userName)

        initVoiceButton()

        initVideoButton()



         binding.submit.setOnClickListener {


             if(person.ImageUrl.isBlank()){
                 Toast.makeText(this,"Please Upload Image",Toast.LENGTH_SHORT).show()
             }else {
                    if(binding.editTextTextMultiLine.text.toString()!="") {
                        person.disp = binding.editTextTextMultiLine.text.toString()
                    }
                 person.lati=latitude
                 person.longi=longitude
                 person.id=UUID.randomUUID().toString()
                 Firebase.firestore.collection("Reportings").document(person.id)
                     .set(person).addOnCompleteListener {
                     if (it.isSuccessful) {
                         Toast.makeText(
                             this,
                             "Thankyou! Authority will take required actions",
                             Toast.LENGTH_LONG
                         ).show()
                         finish()
                         var intent=Intent(this,MainActivity::class.java)
                         startActivity(intent)

                     } else {
                         Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_LONG).show()
                     }

                 }
             }
         }

    }

    private fun initVideoButton() {
        val newVideoCall = findViewById<ZegoSendCallInvitationButton>(R.id.new_video_call)
        newVideoCall.setIsVideoCall(true)

        //for notification sound
        newVideoCall.resourceID = "zego_data"
        newVideoCall.setInvitees(listOf(ZegoUIKitUser("101", "Surveillance Officer")))


    }

    private fun initVoiceButton() {
        val newVoiceCall = findViewById<ZegoSendCallInvitationButton>(R.id.new_voice_call)

        newVoiceCall.setIsVideoCall(false)

        //for notification sound
        newVoiceCall.resourceID = "zego_data"
        newVoiceCall.setInvitees(listOf(ZegoUIKitUser("101", "Surveillance Officer")))

    }

    private fun initCallInviteService(appID: Long, appSign: String, userID: String, userName: String) {

        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig().apply {
            provider =
                ZegoUIKitPrebuiltCallConfigProvider { invitationData -> getConfig(invitationData) }
        }

        ZegoUIKitPrebuiltCallService.events.errorEventsListener =
            ErrorEventsListener { errorCode, message -> Timber.d("onError() called with: errorCode = [$errorCode], message = [$message]") }

        ZegoUIKitPrebuiltCallService.events.invitationEvents.pluginConnectListener =
            SignalPluginConnectListener { state, event, extendedData -> Timber.d("onSignalPluginConnectionStateChanged() called with: state = [$state], event = [$event], extendedData = [$extendedData]") }

        ZegoUIKitPrebuiltCallService.init(
            application,
            appID,
            appSign,
            userID,
            userName,
            callInvitationConfig
        )

        ZegoUIKitPrebuiltCallService.events.callEvents.callEndListener =
            CallEndListener { callEndReason, jsonObject -> Timber.d("onCallEnd() called with: callEndReason = [$callEndReason], jsonObject = [$jsonObject]") }

        ZegoUIKitPrebuiltCallService.events.callEvents.setExpressEngineEventHandler(object :
            IExpressEngineEventHandler() {
            override fun onRoomStateChanged(
                roomID: String,
                reason: ZegoRoomStateChangedReason,
                errorCode: Int,
                extendedData: JSONObject
            ) {
                Timber.d("onRoomStateChanged() called with: roomID = [$roomID], reason = [$reason], errorCode = [$errorCode], extendedData = [$extendedData]")
            }
        })
    }
    private fun getConfig(invitationData: ZegoCallInvitationData): ZegoUIKitPrebuiltCallConfig {
        val isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.value
        val isGroupCall = invitationData.invitees.size > 1
        return when {
            isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVideoCall()
            !isVideoCall && isGroupCall -> ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
            !isVideoCall -> ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
            else -> ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallService.endCall()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("permission", "success")
            } else {
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Location Permission Required")
        builder.setMessage("This app requires access to your location to function properly. Please grant location permission in settings.")
        builder.setPositiveButton("Go to Settings") { _, _ ->
            // Open app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Close App") { _, _ ->
            // Close the app
            finish()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }



    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update location every 10 seconds
            fastestInterval = 5000 // Fastest update interval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // High accuracy
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    // Update UI or perform other tasks with the location data
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}
