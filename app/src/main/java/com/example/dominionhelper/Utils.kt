package com.example.dominionhelper

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dominionhelper.data.Card
import kotlin.random.Random

fun getDrawableId(context: Context, imageName: String): Int {
    val resourceId = context.resources.getIdentifier(
        imageName,
        "drawable",
        context.packageName
    )

    if (resourceId == 0) {
        Log.e("getDrawableId", "Could not find drawable resource with name: $imageName")
        return R.drawable.ic_launcher_foreground
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

fun isPercentChance(percentChance: Double): Boolean {
    require(percentChance in 0.0..100.0) { "percentChance must be between 0.0 and 100.0" }
    return Random.nextDouble(0.0, 100.0) < percentChance
}

// Find the index of a specific reference in a list (even when cards are equal)
fun findIndexOfReference(list: List<Any>, target: Card): Int {
    for ((index, item) in list.withIndex()) {
        if (item === target) return index
    }
    return -1
}


