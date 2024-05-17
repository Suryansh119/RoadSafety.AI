package com.example.safezoneadmin


import android.R
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.example.safezoneadmin.databinding.ActivityAdminImageViewBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem


class AdminImageView : AppCompatActivity() {
    val binding by lazy {
        ActivityAdminImageViewBinding.inflate(layoutInflater)

    }
   var latitute:String=""
   var longitute:String=""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val onMenuItemClickListener = OnMenuItemClickListener<PowerMenuItem> { position, item ->
            // Handle the click event here
            var id=intent.getStringExtra("documentId")
            when (position) {
                0 -> {
                    if (id != null) {
                        confirmDelete(id) {
                            // Perform document deletion using documentId
                            deleteDocument(id) // Call the deletion function
                        }
                    }
                }
                1 -> {
                  shareImageAndLocation()
                }

            }
        }

        val powerMenu = PowerMenu.Builder(this)
            .addItem(PowerMenuItem("Resolve", false)) // add an item.
            .addItem(PowerMenuItem("Share", false))
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


        binding.scaleImage.load(intent.getStringExtra("image"))
        binding.textView3.text=intent.getStringExtra("disp")
        latitute= intent.getStringExtra("latitute").toString()
        longitute= intent.getStringExtra("longitute").toString()
        binding.imageView3.setOnClickListener {
            powerMenu.showAsDropDown(binding.imageView3);
        }
        binding.imageView2.setOnClickListener {
            finish()
        }
        binding.button3.setOnClickListener {
            openGoogleMaps(latitute,longitute)
        }
    }

    private fun deleteDocument(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Reportings").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Case resolved successfully", Toast.LENGTH_SHORT).show()
                finish() // Close AdminImageView activity after deletion
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete(documentId: String, onDeleteConfirmed: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to resolve and close this case?")
            .setPositiveButton("Yes") { _, _ ->
                onDeleteConfirmed(documentId)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun openGoogleMaps(Latitude: String, Longitude: String) {
        if(latitute=="" || longitute==""){
            binding.button3.isClickable=false
            binding.button3.text="There is no location available for current reporting"
        }
        else {


            val uri =
                Uri.parse("http://maps.google.com/maps?q=My+Location&daddr=$Latitude,$Longitude")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
    private fun shareImageAndLocation() {
        val locationUri = Uri.parse("http://maps.google.com/maps?q=$latitute,$longitute")
        val imageurl=intent.getStringExtra("image").toString()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Image URL: $imageurl\nLocation: $locationUri")
            type = "text/plain"
        }

        startActivity(Intent.createChooser(shareIntent, "Share Image and Location"))
    }
}
