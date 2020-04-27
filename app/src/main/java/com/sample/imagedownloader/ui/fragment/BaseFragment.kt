package com.sample.imagedownloader.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    companion object {
        val TAG = BaseFragment::class.java.simpleName
    }

    protected var mToast: Toast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(savedInstanceState)
    }

    private fun init(savedInstanceState: Bundle?) {
        mToast = Toast.makeText(activity, "", Toast.LENGTH_SHORT)
    }

    protected fun showToast(message: String) {
        mToast?.apply {
            setText(message)
            show()
        }
    }

}
