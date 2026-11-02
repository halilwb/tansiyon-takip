package com.sabri.tansiyontakip.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sabri.tansiyontakip.data.Reading
import com.sabri.tansiyontakip.ui.theme.BlueLine
import com.sabri.tansiyontakip.ui.theme.Navy
import com.sabri.tansiyontakip.ui.theme.PureWhite
import com.sabri.tansiyontakip.ui.theme.TextMuted
import com.sabri.tansiyontakip.ui.theme.Yellow
import com.sabri.tansiyontakip.ui.theme.YellowDeep
import com.sabri.tansiyontakip.util.DAY_MILLIS
import com.sabri.tansiyontakip.util.formatDateShort
import com.sabri.tansiyontakip.util.startOfDay
import kotlin.math.roundToInt

data class ChartPoint(
    val timestamp: Long,
    val systolic: Float,
    val diastolic: Float
)

/** Seçilen gün aralığı için günlük ortalama noktalarını üretir. */
fun buildChartPoints(readings: List<Reading>, days: Int): List<ChartPoint> {
    val end = startOfDay() + DAY_MILLIS
    val start = end - days * DAY_MILLIS

    return readings
        .filter { it.timestamp in start until end }
        .groupBy { startOfDay(it.timestamp) }
        .toSortedMap()
        .map { (day, dayReadings) ->
            ChartPoint(
                timestamp = day,
                systolic = dayReadings.map { it.systolic }.average().toFloat(),
                diastolic = dayReadings.map { it.diastolic }.average().toFloat()
            )
        }
}

/**
 * Lacivert (büyük tansiyon) ve sarı (küçük tansiyon) çizgileriyle
 * yükseliş/düşüş trendini gösteren özel çizgi grafiği.
 */
@Composable
fun TrendChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "Grafik için henüz yeterli veri yok.",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        return
    }

    Canvas(modifier) {
        val rightPadding = 38.dp.toPx()
        val bottomPadding = 22.dp.toPx()
        val topPadding = 10.dp.toPx()
        val chartWidth = size.width - rightPadding
        val chartHeight = size.height - bottomPadding - topPadding
        if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

        val allValues = points.flatMap { listOf(it.systolic, it.diastolic) }
        var minValue = ((allValues.min().toInt() / 10) * 10 - 10).toFloat().coerceAtLeast(40f)
        var maxValue = (((allValues.max().toInt() / 10) + 1) * 10 + 10).toFloat()
        if (maxValue - minValue < 40f) maxValue = minValue + 40f
        val valueRange = maxValue - minValue

        fun xFor(index: Int): Float =
            if (points.size == 1) chartWidth / 2f
            else chartWidth * index / (points.size - 1).toFloat()

        fun yFor(value: Float): Float =
            topPadding + chartHeight * (1f - (value - minValue) / valueRange)

        val labelPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = TextMuted.toArgb()
            textSize = 9.5.sp.toPx()
        }
        val datePaint = android.graphics.Paint(labelPaint).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 9.sp.toPx()
        }
        val nativeCanvas = drawContext.canvas.nativeCanvas

        // Normal kabul edilen 80-120 mmHg aralığı hafif sarı bir bantla vurgulanır.
        val bandTop = yFor(120f.coerceIn(minValue, maxValue))
        val bandBottom = yFor(80f.coerceIn(minValue, maxValue))
        if (bandBottom > bandTop) {
            drawRect(
                color = Yellow.copy(alpha = 0.16f),
                topLeft = Offset(0f, bandTop),
                size = Size(chartWidth, bandBottom - bandTop)
            )
        }

        // Yatay ızgara ve sağdaki değer etiketleri.
        for (step in 0..4) {
            val value = minValue + valueRange * step / 4f
            val y = yFor(value)
            drawLine(
                color = BlueLine,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )
            nativeCanvas.drawText(
                value.roundToInt().toString(),
                chartWidth + 7.dp.toPx(),
                y + 3.dp.toPx(),
                labelPaint
            )
        }

        val systolicOffsets = points.mapIndexed { index, point ->
            Offset(xFor(index), yFor(point.systolic))
        }
        val diastolicOffsets = points.mapIndexed { index, point ->
            Offset(xFor(index), yFor(point.diastolic))
        }

        // Büyük tansiyonun altındaki lacivert gradyan dolgu.
        if (systolicOffsets.size > 1) {
            val fillPath = Path().apply {
                moveTo(systolicOffsets.first().x, topPadding + chartHeight)
                lineTo(systolicOffsets.first().x, systolicOffsets.first().y)
                for (index in 0 until systolicOffsets.size - 1) {
                    val from = systolicOffsets[index]
                    val to = systolicOffsets[index + 1]
                    val midX = (from.x + to.x) / 2f
                    cubicTo(midX, from.y, midX, to.y, to.x, to.y)
                }
                lineTo(systolicOffsets.last().x, topPadding + chartHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Navy.copy(alpha = 0.20f), Navy.copy(alpha = 0.01f)),
                    startY = topPadding,
                    endY = topPadding + chartHeight
                )
            )
        }

        drawPath(
            path = smoothPath(diastolicOffsets),
            color = YellowDeep,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8.dp.toPx(), 6.dp.toPx())
                )
            )
        )
        drawPath(
            path = smoothPath(systolicOffsets),
            color = Navy,
            style = Stroke(
                width = 2.6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        systolicOffsets.forEachIndexed { index, offset ->
            val last = index == systolicOffsets.lastIndex
            val radius = if (last) 5.dp.toPx() else 3.4.dp.toPx()
            if (last) {
                drawCircle(color = Navy, radius = radius, center = offset)
                drawCircle(color = PureWhite, radius = 2.2.dp.toPx(), center = offset)
            } else {
                drawCircle(color = PureWhite, radius = radius, center = offset)
                drawCircle(color = Navy, radius = radius, center = offset, style = Stroke(width = 1.8.dp.toPx()))
            }
        }
        diastolicOffsets.forEachIndexed { index, offset ->
            val last = index == diastolicOffsets.lastIndex
            val radius = if (last) 4.dp.toPx() else 2.8.dp.toPx()
            if (last) {
                drawCircle(color = YellowDeep, radius = radius, center = offset)
            } else {
                drawCircle(color = PureWhite, radius = radius, center = offset)
                drawCircle(color = YellowDeep, radius = radius, center = offset, style = Stroke(width = 1.4.dp.toPx()))
            }
        }

        // Alt eksende tarih etiketleri.
        val labelIndices = if (points.size <= 4) {
            points.indices.toList()
        } else {
            listOf(0, points.size / 3, points.size * 2 / 3, points.size - 1).distinct()
        }
        labelIndices.forEach { index ->
            val x = xFor(index).coerceIn(16.dp.toPx(), chartWidth - 16.dp.toPx())
            nativeCanvas.drawText(
                formatDateShort(points[index].timestamp),
                x,
                size.height - 5.dp.toPx(),
                datePaint
            )
        }
    }
}

private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (index in 0 until points.size - 1) {
        val from = points[index]
        val to = points[index + 1]
        val midX = (from.x + to.x) / 2f
        path.cubicTo(midX, from.y, midX, to.y, to.x, to.y)
    }
    return path
}
