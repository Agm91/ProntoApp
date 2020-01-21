package com.agm91.prontoapp

import androidx.fragment.app.FragmentActivity

abstract class BaseFragmentActivity : FragmentActivity() {
    abstract fun checkPermissions()
}