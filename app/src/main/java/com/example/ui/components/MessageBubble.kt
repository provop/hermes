package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HermesMessageEntity
import com.example.ui.theme.HermesCyan
import com.example.ui.theme.HermesDarkCardBorder
import com.example.ui.theme.HermesDarkSurfaceVariant
import com.example.ui.theme.HermesGold
import com.example.ui.theme.HermesTextPrimary
import com.example.ui.theme.HermesTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: HermesMessageEntity,
    onSpeakClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender.equals("USER", ignoreCase = true)
    val isHermesTelegram = message.sender.equals("HERMES_TELEGRAM", ignoreCase = true)
    val isSystem = message.sender.equals("SYSTEM", ignoreCase = true)
    val clipboardManager = LocalClipboardManager.current

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleBg = when {
        isUser -> Color(0xFF003844)
        isHermesTelegram -> Color(0xFF2B1D00)
        isSystem -> Color(0xFF24141E)
        else -> HermesDarkSurfaceVariant
    }
    val borderColor = when {
        isUser -> HermesCyan.copy(alpha = 0.5f)
        isHermesTelegram -> HermesGold.copy(alpha = 0.6f)
        else -> HermesDarkCardBorder
    }

    val senderLabel = when (message.sender) {
        "USER" -> "You"
        "GEMINI" -> "Hermes AI"
        "HERMES_TELEGRAM" -> "Hermes Agent (Telegram)"
        "SYSTEM" -> "System"
        else -> message.sender
    }

    val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isUser -> HermesCyan
                            isHermesTelegram -> HermesGold
                            else -> HermesCyan
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isUser -> Icons.Default.Person
                        isHermesTelegram -> Icons.Default.Send
                        else -> Icons.Default.Android
                    },
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = senderLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isUser -> HermesCyan
                    isHermesTelegram -> HermesGold
                    else -> HermesTextSecondary
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeString,
                fontSize = 10.sp,
                color = HermesTextSecondary.copy(alpha = 0.6f)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = bubbleBg,
            modifier = Modifier
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = HermesTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                if (!message.actionType.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Action: ${message.actionType}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = HermesCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUser) {
                        IconButton(
                            onClick = { onSpeakClick(message.content) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Listen to message",
                                tint = HermesCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(message.content)) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = HermesTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
