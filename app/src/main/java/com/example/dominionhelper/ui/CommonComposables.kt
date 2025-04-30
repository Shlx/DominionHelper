package com.example.dominionhelper.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// Display a number in a circle (Used for card costs)
@Composable
fun NumberCircle(number: Int, modifier: Modifier = Modifier) {
    val circleColor = MaterialTheme.colorScheme.primaryContainer
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(24.dp)
        ) {
            drawCircle(
                color = circleColor,
                radius = size.minDimension / 2,
                center = Offset(size.width / 2, size.height / 2)
            )

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = textColor
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isFakeBoldText = true
                }

                val textBounds = android.graphics.Rect()
                paint.getTextBounds(number.toString(), 0, number.toString().length, textBounds)

                canvas.nativeCanvas.drawText(
                    number.toString(),
                    size.width / 2,
                    (size.height / 2) - (textBounds.top + textBounds.bottom) / 2,
                    paint
                )
            }
        }
    }
}

// Display a number in a hexagon (Used for card debt)
@Composable
fun NumberHexagon(number: Int, modifier: Modifier = Modifier) {
    val hexagonColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(25.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2

            // Draw the hexagon
            drawIntoCanvas { canvas ->
                val hexagonPath = android.graphics.Path()
                val angle = 2.0 * Math.PI / 6 // 6 sides

                // Start at the first vertex
                hexagonPath.moveTo(
                    centerX + radius * cos(0.0).toFloat(),
                    centerY + radius * sin(0.0).toFloat()
                )

                // Draw lines to each subsequent vertex
                for (i in 1..6) {
                    hexagonPath.lineTo(
                        centerX + radius * cos(angle * i).toFloat(),
                        centerY + radius * sin(angle * i).toFloat()
                    )
                }

                // Close the path
                hexagonPath.close()
                val paint = android.graphics.Paint()
                paint.color = hexagonColor.toArgb()
                paint.style = android.graphics.Paint.Style.FILL
                canvas.nativeCanvas.drawPath(hexagonPath, paint)

                // Draw the text
                val textPaint = android.graphics.Paint().apply {
                    color = textColor
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isFakeBoldText = true
                }

                val textBounds = android.graphics.Rect()
                textPaint.getTextBounds(
                    number.toString(),
                    0,
                    number.toString().length,
                    textBounds
                )

                canvas.nativeCanvas.drawText(
                    number.toString(),
                    centerX,
                    centerY - (textBounds.top + textBounds.bottom) / 2,
                    textPaint
                )
            }
        }
    }
}
