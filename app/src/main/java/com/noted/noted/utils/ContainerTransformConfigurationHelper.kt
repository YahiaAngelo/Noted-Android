package com.noted.noted.utils

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Build.VERSION_CODES

import android.view.animation.Interpolator
import androidx.annotation.RequiresApi
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform


/**
 * A helper class which manages all configuration UI presented in [ ].
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
class ContainerTransformConfigurationHelper() {
    /**
     * Whether or not to a custom container transform should use [ ].
     */
    var isArcMotionEnabled = false
        protected set

    /** The enter duration to be used by a custom container transform.  */
    var enterDuration: Long = 0
        private set

    /** The return duration to be used by a custom container transform.  */
    var returnDuration: Long = 0
        private set

    /** The interpolator to be used by a custom container transform.  */
    var interpolator: Interpolator? = null
        private set
    private var fadeModeButtonId = 0

    /** Whether or not the custom transform should draw debugging lines.  */
    var isDrawDebugEnabled = false
        private set




    /** Set up the transition according to the config helper's parameters.  */
    fun configure(transform: MaterialContainerTransform, entering: Boolean) {
        transform.duration = if (entering) enterDuration else returnDuration
        transform.interpolator = interpolator
        if (isArcMotionEnabled) {
            transform.pathMotion = MaterialArcMotion()
        }
        transform.isDrawDebugEnabled = isDrawDebugEnabled
    }

    /** The fade mode used by a custom container transform.  */

    private fun setUpDefaultValues() {
        setDefaultMotionPath()
        enterDuration = 300
        returnDuration = 275
        interpolator = FastOutSlowInInterpolator()
        fadeModeButtonId = 0
        isDrawDebugEnabled = false
    }

    protected fun setDefaultMotionPath() {
        isArcMotionEnabled = false
    }



    init {
        setUpDefaultValues()
    }
}