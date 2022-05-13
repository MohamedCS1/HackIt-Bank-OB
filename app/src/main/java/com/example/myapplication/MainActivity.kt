package com.example.myapplication

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.databinding.ActivityMainBinding
import android.content.Intent
import android.util.Log
import android.database.sqlite.SQLiteDatabase

import android.content.ContentValues
import java.io.File
import java.io.FileReader
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    val ACTIVITY_CHOOSE_CSV = 12

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buRecent.setBackgroundResource(R.drawable.background_bu_recent)
        binding.buRecent.setTextColor(Color.parseColor("#000000"))

        binding.buNewUpload.setBackgroundResource(R.drawable.background_bu_new_upload)
        binding.buNewUpload.setTextColor(Color.parseColor("#FFFFFF"))

        binding.buNewUpload.setOnClickListener {
            binding.buNewUpload.setBackgroundResource(R.drawable.background_bu_new_upload)
            binding.buNewUpload.setTextColor(Color.parseColor("#FFFFFF"))

            binding.buRecent.setBackgroundResource(R.drawable.background_bu_recent)
            binding.buRecent.setTextColor(Color.parseColor("#000000"))

        }

        binding.buRecent.setOnClickListener {
            binding.buRecent.setBackgroundResource(R.drawable.background_bu_recent_black)
            binding.buRecent.setTextColor(Color.parseColor("#FFFFFF"))

            binding.buNewUpload.setBackgroundResource(R.drawable.background_bu_new_upload_white)
            binding.buNewUpload.setTextColor(Color.parseColor("#000000"))
        }

        binding.buAddCsv.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/csv"
            startActivityForResult(Intent.createChooser(intent, "Open CSV"), ACTIVITY_CHOOSE_CSV)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            ACTIVITY_CHOOSE_CSV ->
                if (data != null)
                {
                    Log.d("currentData" , data.data!!.path.toString())
                }
        }

    }
}