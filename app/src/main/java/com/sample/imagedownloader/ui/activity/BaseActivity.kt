package com.sample.imagedownloader.ui.activity

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sample.imagedownloader.R

abstract class BaseActivity : AppCompatActivity() {

    protected fun showFragment(fragment: Fragment, tag: String?) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.fragment_open, R.anim.fragment_close, R.anim.fragment_open,
                    R.anim.fragment_close).replace(R.id.containerFrame, fragment)

            if (tag != null) {
                addToBackStack(tag)
            }

        }.commit()
    }

}