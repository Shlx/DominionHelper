package com.example.dominionhelper

import android.content.Context
import android.content.Intent
import android.util.Log

fun getDrawableId(context: Context, imageName: String): Int {
    val resourceId = context.resources.getIdentifier(
        imageName,
        "drawable",
        context.packageName
    )

    if (resourceId == 0) {
        Log.e("getDrawableId", "Could not find drawable resource with name: $imageName")
        // TODO: Return a default drawable
    }

    return resourceId
}

fun navigateToActivity(context: Context, activityClass: Class<*>) {
    val intent = Intent(context, activityClass).apply {
        flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    context.startActivity(intent)
}
