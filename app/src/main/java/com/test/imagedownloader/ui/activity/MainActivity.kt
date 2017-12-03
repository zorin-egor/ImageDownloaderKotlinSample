package com.test.imagedownloader.ui.activity

import android.os.Bundle
import com.test.imagedownloader.R
import com.test.imagedownloader.ui.fragment.ImageFragment

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            showFragment(ImageFragment.newInstance(), ImageFragment.TAG)
        }
    }

}
