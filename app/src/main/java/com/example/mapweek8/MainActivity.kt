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
import com.example.mapweek8.worker.ThirdWorker

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

        // Permission notifikasi untuk Android 13+
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

        val thirdRequest = OneTimeWorkRequest.Builder(ThirdWorker::class.java)
            .setConstraints(constraints)
            .setInputData(createInputData(ThirdWorker.INPUT_DATA_ID, id))
            .build()

        // Rantai worker sesuai assignment
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .then(thirdRequest)
            .enqueue()

        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showToast("Second process is done")
                    launchFirstNotificationService()
                }
            }

        workManager.getWorkInfoByIdLiveData(thirdRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showToast("Third process is done")
                    launchSecondNotificationService()
                }
            }
    }

    private fun launchFirstNotificationService() {
        val intent = Intent(this, NotificationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun launchSecondNotificationService() {
        val intent = Intent(this, SecondNotificationService::class.java)
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
