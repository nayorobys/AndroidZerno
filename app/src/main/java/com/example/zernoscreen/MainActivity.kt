package com.example.zernoscreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private var workerThread: Thread? = null
    private var isRunning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageView = findViewById<ImageView>(R.id.imageView)

        isRunning = true
        workerThread = Thread {
            while (isRunning) {
                var bitmap: Bitmap? = null
                try {
                    val url = URL("http://192.168.42.18:8080/frame")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 1500
                    connection.readTimeout = 1500
                    connection.connect()

                    if (connection.responseCode == 200) {
                        val inputStream = connection.inputStream
                        bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                    }
                    connection.disconnect()

                    if (bitmap != null) {
                        // Выводим кадр на главный поток интерфейса
                        runOnUiThread {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                } catch (e: Exception) {
                    // Игрок/сеть недоступны — цикл просто попробует снова
                }

                try {
                    // Пауза 100 мс (~10 кадров в секунду), чтобы не перегружать процессор и память
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        workerThread?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        workerThread?.interrupt()
    }
}