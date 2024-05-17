package com.example.safezoneadmin

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.helper.widget.Carousel.Adapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.safezoneadmin.databinding.ActivityAdminScreenBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.ExplainReasonCallback
import com.permissionx.guolindev.callback.RequestCallback
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import org.json.JSONObject
import timber.log.Timber

class AdminScreen : AppCompatActivity() {

    private val binding by lazy {
        ActivityAdminScreenBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: PersonAdapter
    private lateinit var datalist: ArrayList<PersonModel>
    private lateinit var snapshotListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        askNotificationPermission()
        val onMenuItemClickListener = OnMenuItemClickListener<PowerMenuItem> { position, item ->
            // Handle the click event here
            when (position) {
                0 -> {
                  Firebase.auth.signOut()
                    startActivity(Intent(this,Login::class.java))
                    finish()
                }

            }

        }
        val powerMenu = PowerMenu.Builder(this)
            .addItem(PowerMenuItem("Log Out", false)) // add an item.
            .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .setTextGravity(Gravity.CENTER)
            .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.BLACK)
            .setMenuColor(Color.parseColor("#4D700BEF"))
            .setSelectedMenuColor(ContextCompat.getColor(this, R.color.white))
            .setOnMenuItemClickListener(onMenuItemClickListener)
            .build()
        binding.imageView3.setOnClickListener {
            powerMenu.showAsDropDown(binding.imageView3);
        }

        PermissionX.init(this@AdminScreen).permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
            .onExplainRequestReason(ExplainReasonCallback { scope, deniedList ->
                val message =
                    "We need your consent for the following permissions in order to use the offline call function properly"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }).request(RequestCallback { allGranted, grantedList, deniedList -> })
        val application: Application = application
        val appID: Long = 1116811214 // Replace TODO() with your App ID
        val appSign: String = "6999b56a2e24bc64ec539273cf0c91389c6eb30d56182626e11691f16f3e3558" // Replace TODO() with your App Sign
        val userID: String = "101"// Replace TODO() with your User ID
        val userName: String = "Surveillance Officer"// Replace TODO() with your User Name

        datalist = ArrayList()
        adapter = PersonAdapter(datalist,this)
        binding.rv.adapter = adapter
        binding.rv.layoutManager = LinearLayoutManager(this)

        fetchDataFromFirestore()
        initCallInviteService(appID, appSign, userID, userName)
    }

    private fun showSettingDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
//                Log.e(TAG, "PERMISSION_GRANTED")
                // FCM SDK (and your app) can post notifications.
            } else {
//                Log.e(TAG, "NO_PERMISSION")
                // Directly ask for the permission
                val launcher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted){
                        // permission granted
                    } else {
                        showSettingDialog()
                    }
                }
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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




    private fun fetchDataFromFirestore() {
        snapshotListener = FirebaseFirestore.getInstance().collection("Reportings").orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: Exception? ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                datalist.clear()

                val documents = snapshot?.documents
                if (documents != null) {
                    for (documentSnapshot in documents) {
                        val temp = documentSnapshot.toObject<PersonModel>()

                        temp?.let { datalist.add(it) }
                    }
                }

                // Move notifyDataSetChanged inside the listener
                adapter.notifyDataSetChanged()
                handleEmptyList()
            }
    }

    private fun handleEmptyList() {
        val isEmpty = datalist.isEmpty()
        binding.rv.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.noContentView.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the snapshot listener when the activity is destroyed
        snapshotListener.remove()
        ZegoUIKitPrebuiltCallService.endCall()
    }
}

