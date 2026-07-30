package com.simats.neoomfs.utils

import android.app.Activity
import android.content.Intent
import android.os.Build

fun Activity.startActivityNoAnimation(intent: Intent) {
    startActivity(intent)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_OPEN,
            0,
            0
        )
    }
}
