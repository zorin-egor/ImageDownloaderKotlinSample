package com.test.imagedownloader.ui.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.test.imagedownloader.R


abstract class BaseActivity : AppCompatActivity() {

    companion object {
        val TAG = BaseActivity::class.java.simpleName
    }

    protected var mFragmentManager: FragmentManager? = null
    protected var mToast : Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG)
        mToast?.setDuration(Toast.LENGTH_SHORT)
    }

    protected fun showToast(resource: Int) {
        mToast?.setText(resources.getString(resource))
        mToast?.show()
    }

    protected fun showFragment(fragment: Fragment, tag: String?) {
        if (mFragmentManager == null) {
            mFragmentManager = supportFragmentManager
        }

        if (fragment != null) {
            val fragmentTransaction = mFragmentManager!!.beginTransaction()
            fragmentTransaction!!.setCustomAnimations(R.anim.fragment_open,
                    R.anim.fragment_close,
                    R.anim.fragment_open,
                    R.anim.fragment_close).replace(R.id.mContainerFrame, fragment)

            if (tag != null) {
                fragmentTransaction.addToBackStack(tag)
            }

            fragmentTransaction.commit()
        }
    }

}