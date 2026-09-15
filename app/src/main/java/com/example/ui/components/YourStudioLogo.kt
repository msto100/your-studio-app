package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.platform.LocalContext

@Composable
fun YourStudioLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val width = size.width
        val height = size.height
        val scaleX = width / 512f
        val scaleY = height / 512f

        // Background
        drawRoundRect(
            color = Color(0xFF0B0F19),
            size = Size(width, height),
            cornerRadius = CornerRadius(110f * scaleX, 110f * scaleY)
        )

        // Gradient
        val ysGrad = Brush.linearGradient(
            colors = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
            start = Offset(0f, 0f),
            end = Offset(width, height)
        )

        // Paths
        val path1 = Path().apply {
            moveTo(175f * scaleX, 140f * scaleY)
            lineTo(256f * scaleX, 220f * scaleY)
            lineTo(337f * scaleX, 140f * scaleY)
        }

        val path2 = Path().apply {
            moveTo(256f * scaleX, 220f * scaleY)
            lineTo(256f * scaleX, 265f * scaleY)
            cubicTo(
                256f * scaleX, 295f * scaleY,
                280f * scaleX, 315f * scaleY,
                310f * scaleX, 315f * scaleY
            )
            cubicTo(
                335f * scaleX, 315f * scaleY,
                348f * scaleX, 295f * scaleY,
                348f * scaleX, 280f * scaleY
            )
            cubicTo(
                348f * scaleX, 250f * scaleY,
                256f * scaleX, 240f * scaleY,
                256f * scaleX, 240f * scaleY
            )
        }

        val path3 = Path().apply {
            moveTo(256f * scaleX, 240f * scaleY)
            cubicTo(
                256f * scaleX, 240f * scaleY,
                164f * scaleX, 230f * scaleY,
                164f * scaleX, 195f * scaleY
            )
            cubicTo(
                164f * scaleX, 175f * scaleY,
                178f * scaleX, 155f * scaleY,
                208f * scaleX, 155f * scaleY
            )
            cubicTo(
                238f * scaleX, 155f * scaleY,
                256f * scaleX, 175f * scaleY,
                256f * scaleX, 205f * scaleY
            )
        }

        val stroke = Stroke(
            width = 34f * scaleX,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        
        // Emulate drop shadow (simplistic)
        drawPath(path1, color = Color(0xFF8B5CF6).copy(alpha = 0.25f), style = stroke)
        drawPath(path2, color = Color(0xFF8B5CF6).copy(alpha = 0.25f), style = stroke)
        drawPath(path3, color = Color(0xFF8B5CF6).copy(alpha = 0.25f), style = stroke)

        drawPath(path1, brush = ysGrad, style = stroke)
        drawPath(path2, brush = ysGrad, style = stroke)
        drawPath(path3, brush = ysGrad, style = stroke)

        // Circle
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 9f * scaleX,
            center = Offset(256f * scaleX, 225f * scaleY)
        )

        // Text rendering using nativeCanvas
        drawContext.canvas.nativeCanvas.apply {
            val paintYour = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 46f * scaleX
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
                letterSpacing = -0.01f
            }
            
            // "Your"
            drawText("Your", 256f * scaleX, 415f * scaleY, paintYour)
            
            // "Studio"
            val paintStudio = Paint().apply {
                textSize = 46f * scaleX
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
                letterSpacing = -0.01f
                
                // Shader for gradient
                val shader = android.graphics.LinearGradient(
                    0f, 0f, width, height,
                    intArrayOf(android.graphics.Color.parseColor("#8B5CF6"), android.graphics.Color.parseColor("#06B6D4")),
                    null,
                    android.graphics.Shader.TileMode.CLAMP
                )
                setShader(shader)
            }
            drawText("Studio", 256f * scaleX, 415f * scaleY, paintStudio)
        }
    }
}
