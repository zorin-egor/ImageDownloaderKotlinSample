package com.sample.imagedownloader.ui.activity

import android.os.Bundle
import com.sample.imagedownloader.R
import com.sample.imagedownloader.ui.fragment.ImageFragment

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            showFragment(ImageFragment.newInstance(), null)
        }
    }

}
