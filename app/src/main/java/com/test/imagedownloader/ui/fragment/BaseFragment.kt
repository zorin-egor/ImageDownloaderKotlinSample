package com.test.imagedownloader.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.test.imagedownloader.R

abstract class BaseFragment : Fragment() {

    companion object {
        val TAG = BaseFragment::class.java.simpleName
    }

    protected var mToast: Toast? = null
    protected var mFragmentManager: FragmentManager? = null
    protected var mParentFragmentManager: FragmentManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        init(container, savedInstanceState)
        return view
    }

    private fun init(container: ViewGroup?, savedInstanceState: Bundle?) {
        mToast = Toast.makeText(context, "", Toast.LENGTH_LONG)
        mToast?.duration = Toast.LENGTH_SHORT
    }

    protected fun showToast(resource: Int) {
        mToast?.setText(resources.getString(resource))
        mToast?.show()
    }

    protected fun showToast(message: String) {
        mToast?.setText(message)
        mToast?.show()
    }

    protected fun showFragment(fragment: Fragment, tag: String?, isAdd: Boolean) {
        if (mFragmentManager == null) {
            mFragmentManager = fragmentManager
        }
        changeFragment(mFragmentManager!!, fragment, tag, isAdd)
    }

    protected fun showParentFragment(fragment: Fragment, tag: String?) {
        if (mParentFragmentManager == null) {
            if (parentFragment != null) {
                mParentFragmentManager = parentFragment.fragmentManager
            }
        }
        changeFragment(mParentFragmentManager!!, fragment, tag, true)
    }

    private fun changeFragment(fragmentManager: FragmentManager,
                               fragment: Fragment,
                               tag: String?,
                               isAdd: Boolean) {
        if (fragment != null) {
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(R.anim.fragment_open,
                    R.anim.fragment_close,
                    R.anim.fragment_open,
                    R.anim.fragment_close)

            if (isAdd) {
                fragmentTransaction.add(R.id.mContainerFrame, fragment)
            } else {
                fragmentTransaction.replace(R.id.mContainerFrame, fragment)
            }

            if (tag != null) {
                fragmentTransaction.addToBackStack(tag)
            }

            fragmentTransaction.commit()
        }
    }

}
