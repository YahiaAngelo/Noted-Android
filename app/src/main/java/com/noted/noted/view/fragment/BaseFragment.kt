package com.noted.noted.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough

open class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    open fun filterCategories(categoryId : Int){}
    open fun refresh(){}
    open fun filterItem(string:String){}
}