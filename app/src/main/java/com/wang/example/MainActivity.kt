package com.wang.example

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.wang.round.RoundedImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val riv = findViewById<RoundedImageView>(R.id.riv_0)
        object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (isFinishing) {
                    return
                }
                toast("切换${msg.what}")
                riv.run {
                    when (msg.what) {
                        1 -> {
                            setCornerRadius(60)
                        }
                        2 -> {
                            setCornerRadius(150, 150, 60, 150)
                            setBorderWidth(9)
                            setBorderColor(0xff0000ff.toInt())
                        }
                        3 -> {
                            setCornerRadius(10000)
                            setBorderWidth(0)
                        }
                        4 -> {
                            setIsOval(true)
                            setBorderWidth(9)
                            setBorderColor(0xff0000ff.toInt())
                        }
                        5 -> {
                            Glide.with(this)
                                .asGif()
                                .load(R.drawable.gif_test)
                                .into(this)
                        }
                        else -> {
                        }
                    }
                }
                if (msg.what < 5) {
                    sendEmptyMessageDelayed(msg.what + 1, 2000);
                }
            }
        }.sendEmptyMessageDelayed(1, 2000)
    }

    fun toast(st: String) {
        Toast.makeText(this, st, Toast.LENGTH_SHORT).show()
    }
}