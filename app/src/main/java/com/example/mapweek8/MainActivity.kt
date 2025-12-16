package com.example.mapweek8

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.mapweek8.worker.FirstWorker
import com.example.mapweek8.worker.SecondWorker

class MainActivity : AppCompatActivity() {

    private val workManager by lazy {
        WorkManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(constraints)
            .setInputData(createInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(constraints)
            .setInputData(createInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) {
                if (it.state.isFinished) {
                    showToast("First process is done")
                }
            }

        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) {
                if (it.state.isFinished) {
                    showToast("Second process is done")
                    launchNotificationService()
                }
            }
    }

    private fun launchNotificationService() {

        NotificationService.trackingCompletion.observe(this) { id ->
            showToast("Process for Notification Channel ID $id is done!")
        }

        val intent = Intent(this, NotificationService::class.java).apply {
            putExtra(NotificationService.EXTRA_ID, "001")
        }

        ContextCompat.startForegroundService(this, intent)
    }

    private fun createInputData(key: String, value: String): Data {
        return Data.Builder()
            .putString(key, value)
            .build()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
