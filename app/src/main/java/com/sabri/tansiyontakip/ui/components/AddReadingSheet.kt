package com.sabri.tansiyontakip.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sabri.tansiyontakip.ui.theme.BlueLine
import com.sabri.tansiyontakip.ui.theme.Navy
import com.sabri.tansiyontakip.ui.theme.NavyMid
import com.sabri.tansiyontakip.ui.theme.PureWhite
import com.sabri.tansiyontakip.ui.theme.TextMuted
import com.sabri.tansiyontakip.ui.theme.Yellow
import com.sabri.tansiyontakip.ui.theme.YellowDeep
import com.sabri.tansiyontakip.ui.theme.YellowSoft
import com.sabri.tansiyontakip.util.formatDate
import com.sabri.tansiyontakip.util.formatTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingSheet(
    onDismiss: () -> Unit,
    onSave: (systolic: Int, diastolic: Int, timestamp: Long) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var systolicText by remember { mutableStateOf("") }
    var diastolicText by remember { mutableStateOf("") }
    var timestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhite,
        contentColor = Navy,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Yellow)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Yeni Ölçüm",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Navy
                    )
                    Text(
                        text = "Büyük ve küçük tansiyon değerlerini gir",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Kapat",
                        tint = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BpValueField(
                    value = systolicText,
                    onValueChange = { input ->
                        systolicText = input.filter { it.isDigit() }.take(3)
                    },
                    label = "Büyük (Sistolik)",
                    placeholder = "120",
                    accent = Navy,
                    modifier = Modifier.weight(1f)
                )
                BpValueField(
                    value = diastolicText,
                    onValueChange = { input ->
                        diastolicText = input.filter { it.isDigit() }.take(3)
                    },
                    label = "Küçük (Diyastolik)",
                    placeholder = "80",
                    accent = YellowDeep,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PickerField(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "TARİH",
                    value = formatDate(timestamp),
                    modifier = Modifier.weight(1f)
                ) {
                    showDatePicker(context, timestamp) { timestamp = it }
                }
                PickerField(
                    icon = Icons.Outlined.Schedule,
                    label = "SAAT",
                    value = formatTime(timestamp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    showTimePicker(context, timestamp) { timestamp = it }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { timestamp = System.currentTimeMillis() }) {
                    Text(
                        text = "Şimdi",
                        color = NavyMid,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            errorMessage?.let { message ->
                Surface(
                    color = YellowSoft,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Yellow)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        fontSize = 11.sp,
                        color = Navy
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    val systolic = systolicText.toIntOrNull()
                    val diastolic = diastolicText.toIntOrNull()
                    errorMessage = when {
                        systolic == null || diastolic == null ->
                            "Lütfen büyük ve küçük tansiyon değerlerini gir."
                        systolic !in 60..260 ->
                            "Büyük tansiyon 60-260 arasında olmalı."
                        diastolic !in 30..160 ->
                            "Küçük tansiyon 30-160 arasında olmalı."
                        systolic <= diastolic ->
                            "Büyük tansiyon, küçük tansiyondan yüksek olmalı."
                        timestamp > System.currentTimeMillis() + 60_000L ->
                            "Gelecekteki bir saat seçilemez."
                        else -> null
                    }
                    if (errorMessage == null && systolic != null && diastolic != null) {
                        onSave(systolic, diastolic, timestamp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Navy,
                    contentColor = PureWhite
                )
            ) {
                Text(
                    text = "Ölçümü Kaydet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Yellow,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Değerler yalnızca kişisel takip amaçlıdır, tıbbi tanı yerine geçmez.",
                fontSize = 10.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BpValueField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label, fontSize = 10.sp) },
        placeholder = {
            Text(text = placeholder, fontSize = 26.sp, color = BlueLine)
        },
        textStyle = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Navy,
            textAlign = TextAlign.Center
        ),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = BlueLine,
            focusedLabelColor = accent,
            unfocusedLabelColor = TextMuted,
            cursorColor = accent,
            focusedContainerColor = PureWhite,
            unfocusedContainerColor = Color(0xFFFAFBFE),
            focusedTextColor = Navy,
            unfocusedTextColor = Navy
        )
    )
}

@Composable
private fun PickerField(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAFBFE))
            .border(1.dp, BlueLine, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = YellowDeep,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = Navy,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

private fun showTimePicker(context: Context, initial: Long, onPicked: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initial }
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val updated = Calendar.getInstance().apply {
                timeInMillis = initial
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onPicked(updated.timeInMillis)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

private fun showDatePicker(context: Context, initial: Long, onPicked: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initial }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val updated = Calendar.getInstance().apply {
                timeInMillis = initial
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onPicked(updated.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = System.currentTimeMillis()
    }.show()
}
