package com.example.mapweek8

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
                }
            }
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
