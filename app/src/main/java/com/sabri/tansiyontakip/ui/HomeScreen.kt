package com.sabri.tansiyontakip.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sabri.tansiyontakip.data.BloodPressureRepository
import com.sabri.tansiyontakip.data.Reading
import com.sabri.tansiyontakip.ui.components.AddReadingSheet
import com.sabri.tansiyontakip.ui.components.ChartPoint
import com.sabri.tansiyontakip.ui.components.TrendChart
import com.sabri.tansiyontakip.ui.components.buildChartPoints
import com.sabri.tansiyontakip.ui.theme.AppBackground
import com.sabri.tansiyontakip.ui.theme.BlueLine
import com.sabri.tansiyontakip.ui.theme.Navy
import com.sabri.tansiyontakip.ui.theme.NavyDeep
import com.sabri.tansiyontakip.ui.theme.NavyMid
import com.sabri.tansiyontakip.ui.theme.NavySoft
import com.sabri.tansiyontakip.ui.theme.PureWhite
import com.sabri.tansiyontakip.ui.theme.TextMuted
import com.sabri.tansiyontakip.ui.theme.Yellow
import com.sabri.tansiyontakip.ui.theme.YellowDeep
import com.sabri.tansiyontakip.ui.theme.YellowSoft
import com.sabri.tansiyontakip.util.BpCategory
import com.sabri.tansiyontakip.util.DAY_MILLIS
import com.sabri.tansiyontakip.util.dayLabel
import com.sabri.tansiyontakip.util.formatFullDate
import com.sabri.tansiyontakip.util.formatTime
import com.sabri.tansiyontakip.util.isSevere
import com.sabri.tansiyontakip.util.startOfDay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(repository: BloodPressureRepository) {
    val readings by repository.readings.collectAsState()
    val sortedReadings = remember(readings) { readings.sortedByDescending { it.timestamp } }
    val latest = sortedReadings.firstOrNull()
    val previous = sortedReadings.getOrNull(1)

    var showAddSheet by remember { mutableStateOf(false) }
    var rangeDays by remember { mutableStateOf(7) }
    var pendingDelete by remember { mutableStateOf<Reading?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val points = remember(readings, rangeDays) { buildChartPoints(readings, rangeDays) }
    val lastSevenDays = remember(readings) {
        readings.filter { it.timestamp >= startOfDay() - 6 * DAY_MILLIS }
    }
    val averageSystolic = lastSevenDays
        .takeIf { it.isNotEmpty() }
        ?.map { it.systolic }
        ?.average()
        ?.roundToInt()
    val averageDiastolic = lastSevenDays
        .takeIf { it.isNotEmpty() }
        ?.map { it.diastolic }
        ?.average()
        ?.roundToInt()
    val todayCount = remember(readings) {
        readings.count { startOfDay(it.timestamp) == startOfDay() }
    }
    val grouped = remember(sortedReadings) {
        sortedReadings
            .groupBy { startOfDay(it.timestamp) }
            .toSortedMap(compareByDescending<Long> { it })
    }

    Scaffold(
        containerColor = AppBackground,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Navy,
                    contentColor = PureWhite,
                    actionColor = Yellow,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .navigationBarsPadding()
                    .border(1.5.dp, Yellow, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                containerColor = Navy,
                contentColor = PureWhite,
                icon = {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                },
                text = {
                    Text(text = "Ölçüm Ekle", fontWeight = FontWeight.SemiBold)
                }
            )
        }
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            item {
                TopHeader(todayCount = todayCount, latest = latest)
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(16.dp))
                    LatestReadingCard(latest = latest, previous = previous)
                    Spacer(Modifier.height(14.dp))
                    StatsRow(
                        averageSystolic = averageSystolic,
                        averageDiastolic = averageDiastolic,
                        todayCount = todayCount,
                        latestCategory = latest?.category
                    )
                    Spacer(Modifier.height(14.dp))
                    TrendCard(
                        points = points,
                        rangeDays = rangeDays,
                        onRangeChange = { rangeDays = it }
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ölçüm Geçmişi",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )
                        Text(
                            text = "${readings.size} kayıt",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            if (grouped.isEmpty()) {
                item { EmptyHistory() }
            } else {
                grouped.forEach { (day, dayReadings) ->
                    item(key = "header-$day") {
                        DayHeader(day = day, count = dayReadings.size)
                    }
                    items(dayReadings, key = { it.id }) { reading ->
                        ReadingRow(
                            reading = reading,
                            onDelete = { pendingDelete = reading }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(18.dp))
                InfoNote()
            }
        }
    }

    if (showAddSheet) {
        AddReadingSheet(
            onDismiss = { showAddSheet = false },
            onSave = { systolic, diastolic, timestamp ->
                repository.addReading(systolic, diastolic, timestamp)
                showAddSheet = false
                scope.launch { snackbarHostState.showSnackbar("Ölçüm kaydedildi") }
            }
        )
    }

    pendingDelete?.let { reading ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = PureWhite,
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = Navy
                )
            },
            title = {
                Text(
                    text = "Ölçüm silinsin mi?",
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
            },
            text = {
                Text(
                    text = "${reading.systolic}/${reading.diastolic} mmHg • " +
                        "${formatFullDate(reading.timestamp)} ${formatTime(reading.timestamp)}",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteReading(reading.id)
                        pendingDelete = null
                        scope.launch { snackbarHostState.showSnackbar("Ölçüm silindi") }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = "Vazgeç", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun TopHeader(todayCount: Int, latest: Reading?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(Brush.linearGradient(listOf(NavyDeep, NavyMid)))
            .drawBehind {
                drawCircle(
                    color = Yellow.copy(alpha = 0.08f),
                    radius = size.width * 0.48f,
                    center = Offset(size.width * 1.02f, size.height * 0.02f)
                )
                drawCircle(
                    color = PureWhite.copy(alpha = 0.04f),
                    radius = size.width * 0.30f,
                    center = Offset(size.width * 0.12f, size.height * 1.05f)
                )
            }
            .statusBarsPadding()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 26.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hoşgeldin",
                    color = Yellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Sabri Baba",
                    color = PureWhite,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = formatFullDate(System.currentTimeMillis()),
                    color = PureWhite.copy(alpha = 0.62f),
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(PureWhite.copy(alpha = 0.10f))
                    .border(1.5.dp, Yellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SB",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeaderPill(text = "Bugün $todayCount ölçüm")
            latest?.let {
                HeaderPill(text = "Son ${formatTime(it.timestamp)}")
            }
        }
    }
}

@Composable
private fun HeaderPill(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PureWhite.copy(alpha = 0.10f))
            .border(0.5.dp, PureWhite.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(Yellow)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = PureWhite.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LatestReadingCard(latest: Reading?, previous: Reading?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = PureWhite,
        border = BorderStroke(1.5.dp, Yellow.copy(alpha = 0.72f)),
        shadowElevation = 2.dp
    ) {
        if (latest == null) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Henüz ölçüm yok",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "İlk ölçümünü ekleyerek takibe başla.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SON ÖLÇÜM",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text(
                            text = "${latest.systolic}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "/",
                            fontSize = 26.sp,
                            color = TextMuted,
                            modifier = Modifier
                                .alignByBaseline()
                                .padding(horizontal = 2.dp)
                        )
                        Text(
                            text = "${latest.diastolic}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyMid,
                            modifier = Modifier.alignByBaseline()
                        )
                        Text(
                            text = "mmHg",
                            fontSize = 10.sp,
                            color = TextMuted,
                            modifier = Modifier
                                .alignByBaseline()
                                .padding(start = 5.dp, bottom = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${dayLabel(latest.timestamp)} • ${formatTime(latest.timestamp)}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    CategoryBadge(category = latest.category)
                    Spacer(Modifier.height(10.dp))
                    TrendDelta(latest = latest, previous = previous)
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: BpCategory, modifier: Modifier = Modifier) {
    val background = when (category) {
        BpCategory.NORMAL, BpCategory.LOW -> NavySoft
        BpCategory.ELEVATED -> YellowSoft
        BpCategory.STAGE1 -> Yellow.copy(alpha = 0.55f)
        BpCategory.STAGE2, BpCategory.CRISIS -> Navy
    }
    val foreground = if (category.isSevere()) Yellow else Navy

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = category.shortLabel,
            color = foreground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun TrendDelta(latest: Reading, previous: Reading?) {
    if (previous == null) {
        Text(
            text = "İlk kayıt",
            color = TextMuted,
            fontSize = 10.sp
        )
        return
    }

    val difference = latest.systolic - previous.systolic
    val decreased = difference < 0
    val text = when {
        difference == 0 -> "Değişim yok"
        decreased -> "▼ ${abs(difference)} mmHg"
        else -> "▲ $difference mmHg"
    }

    Text(
        text = text,
        color = if (decreased) NavyMid else YellowDeep,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun StatsRow(
    averageSystolic: Int?,
    averageDiastolic: Int?,
    todayCount: Int,
    latestCategory: BpCategory?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label = "7 GÜN ORT.",
            value = if (averageSystolic != null && averageDiastolic != null) {
                "$averageSystolic/$averageDiastolic"
            } else {
                "—"
            },
            sub = "mmHg",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "BUGÜN",
            value = "$todayCount",
            sub = "ölçüm",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "DURUM",
            value = latestCategory?.shortLabel ?: "—",
            sub = "son ölçüm",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, BlueLine)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 0.7.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                color = Navy,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TrendCard(
    points: List<ChartPoint>,
    rangeDays: Int,
    onRangeChange: (Int) -> Unit
) {
    val averageSystolic = points.takeIf { it.isNotEmpty() }?.map { it.systolic }?.average()?.roundToInt()
    val averageDiastolic = points.takeIf { it.isNotEmpty() }?.map { it.diastolic }?.average()?.roundToInt()
    val minSystolic = points.minOfOrNull { it.systolic }?.roundToInt()
    val maxSystolic = points.maxOfOrNull { it.systolic }?.roundToInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, BlueLine)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Tansiyon Trendi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Text(
                        text = "Günlük ortalama • son $rangeDays gün",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LegendDot(color = Navy, label = "Büyük")
                    LegendDot(color = YellowDeep, label = "Küçük")
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 14, 30).forEach { days ->
                    RangeChip(
                        text = "$days gün",
                        selected = days == rangeDays,
                        onClick = { onRangeChange(days) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            TrendChart(
                points = points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BlueLine, thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (averageSystolic != null && averageDiastolic != null) {
                        "Ort. $averageSystolic/$averageDiastolic mmHg"
                    } else {
                        "Ortalama veri yok"
                    },
                    fontSize = 11.sp,
                    color = Navy,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (minSystolic != null && maxSystolic != null) {
                        "Aralık $minSystolic–$maxSystolic"
                    } else {
                        "Hedef 80–120"
                    },
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = label, fontSize = 9.sp, color = TextMuted)
    }
}

@Composable
private fun RangeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Navy else PureWhite)
            .border(
                width = 1.dp,
                color = if (selected) Yellow else BlueLine,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) PureWhite else TextMuted
        )
    }
}

@Composable
private fun DayHeader(day: Long, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayLabel(day),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Navy
        )
        Text(
            text = "$count ölçüm",
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun ReadingRow(reading: Reading, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, BlueLine.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 11.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(48.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = formatTime(reading.timestamp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Text(
                    text = "saat",
                    fontSize = 8.sp,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (reading.category.isSevere()) Navy else Yellow
                    )
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${reading.systolic}/${reading.diastolic}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "mmHg",
                        fontSize = 9.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Text(
                    text = "• ${reading.category.shortLabel}",
                    fontSize = 10.sp,
                    color = if (reading.category.isSevere()) YellowDeep else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Ölçümü sil",
                    tint = TextMuted.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistory() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, BlueLine)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Henüz kayıt yok",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Sağ alttaki butondan ilk ölçümünü ekle.",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun InfoNote() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = YellowSoft.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Yellow.copy(alpha = 0.85f))
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(50))
                    .background(YellowDeep)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Bu uygulama yalnızca kişisel tansiyon takibi içindir. " +
                    "Ölçümler tıbbi tanı yerine geçmez; düzenli kontrollerini ve " +
                    "tedavini doktorunla planla.",
                fontSize = 10.sp,
                lineHeight = 15.sp,
                color = Navy.copy(alpha = 0.78f)
            )
        }
    }
}
