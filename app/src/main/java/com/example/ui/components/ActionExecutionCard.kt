package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HermesActionLogEntity
import com.example.ui.theme.HermesCyan
import com.example.ui.theme.HermesDarkCardBorder
import com.example.ui.theme.HermesDarkSurfaceVariant
import com.example.ui.theme.HermesError
import com.example.ui.theme.HermesGold
import com.example.ui.theme.HermesSuccess
import com.example.ui.theme.HermesTextPrimary
import com.example.ui.theme.HermesTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActionExecutionCard(
    action: HermesActionLogEntity,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (action.actionType.lowercase()) {
        "make_call" -> Icons.Default.Call
        "draft_email" -> Icons.Default.Email
        "send_sms" -> Icons.Default.Message
        "browse_web" -> Icons.Default.Language
        "open_settings" -> Icons.Default.Settings
        "toggle_flashlight" -> Icons.Default.FlashlightOn
        "set_alarm", "set_timer" -> Icons.Default.Alarm
        "set_ringer_mode" -> Icons.Default.VolumeUp
        "relay_to_hermes", "hermes_relay" -> Icons.Default.Send
        else -> Icons.Default.Settings
    }

    val iconColor = if (action.actionType.contains("hermes", ignoreCase = true)) HermesGold else HermesCyan
    val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(action.timestamp))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = HermesDarkSurfaceVariant.copy(alpha = 0.8f),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HermesDarkCardBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = action.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HermesTextPrimary
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = HermesTextSecondary.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = action.description,
                    fontSize = 12.sp,
                    color = HermesTextSecondary,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (action.isSuccess) HermesSuccess.copy(alpha = 0.2f) else HermesError.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (action.isSuccess) "DONE" else "FAILED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (action.isSuccess) HermesSuccess else HermesError,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
