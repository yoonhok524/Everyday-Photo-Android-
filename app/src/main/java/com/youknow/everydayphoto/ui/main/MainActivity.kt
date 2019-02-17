package com.youknow.everydayphoto.ui.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.youknow.everydayphoto.R

class MainActivity : AppCompatActivity(), ExitListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun exit() {
        finish()
    }
}