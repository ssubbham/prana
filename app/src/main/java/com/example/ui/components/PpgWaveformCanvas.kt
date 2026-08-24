package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BotanicalGreen
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalSurface

@Composable
fun PpgWaveformCanvas(
    waveform: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = BotanicalGreen,
    showGrid: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(NaturalSurface)
            .border(1.dp, NaturalBorder, RoundedCornerShape(20.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw subtle background natural grid lines
            if (showGrid) {
                val gridColor = Color(0xFF1B1C17).copy(alpha = 0.05f)
                val gridSpacing = 24.dp.toPx()

                var x = 0f
                while (x < width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                    x += gridSpacing
                }

                var y = 0f
                while (y < height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += gridSpacing
                }
            }

            if (waveform.size < 2) return@Canvas

            // Find min/max for dynamic scaling
            var minVal = waveform.minOrNull() ?: -1f
            var maxVal = waveform.maxOrNull() ?: 1f
            if (maxVal - minVal < 0.2f) {
                maxVal += 0.5f
                minVal -= 0.5f
            }

            val range = (maxVal - minVal).coerceAtLeast(0.001f)
            val stepX = width / (waveform.size - 1)
            val centerY = height / 2f

            val path = Path()
            val fillPath = Path()

            waveform.forEachIndexed { index, value ->
                val xPos = index * stepX
                // Invert Y axis: higher amplitude goes up
                val normalizedY = (value - minVal) / range
                val yPos = height - (normalizedY * (height * 0.75f) + height * 0.125f)

                if (index == 0) {
                    path.moveTo(xPos, yPos)
                    fillPath.moveTo(xPos, height)
                    fillPath.lineTo(xPos, yPos)
                } else {
                    path.lineTo(xPos, yPos)
                    fillPath.lineTo(xPos, yPos)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw under-waveform gradient glow
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.25f),
                        lineColor.copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw main PPG pulsatile trace
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw current pulsating head indicator
            val lastIdx = waveform.size - 1
            val lastX = lastIdx * stepX
            val lastNormY = (waveform[lastIdx] - minVal) / range
            val lastY = height - (lastNormY * (height * 0.75f) + height * 0.125f)

            drawCircle(
                color = lineColor.copy(alpha = 0.4f),
                radius = 7.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(lastX, lastY)
            )
        }
    }
}
