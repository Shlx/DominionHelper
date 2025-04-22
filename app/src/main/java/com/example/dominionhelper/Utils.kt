package com.example.dominionhelper

import android.content.Context
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
