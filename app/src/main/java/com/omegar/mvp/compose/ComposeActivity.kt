package com.omegar.mvp.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import com.omegar.mvp.MvpAppCompatActivity

class ComposeActivity: MvpAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }

}