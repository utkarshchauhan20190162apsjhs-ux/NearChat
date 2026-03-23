package com.nearchat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nearchat.data.model.ChatMessage
import com.nearchat.ui.theme.BubbleMe
import com.nearchat.ui.theme.BubbleRemote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    val alignment = if (message.outgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.outgoing) BubbleMe else BubbleRemote
    val formatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    AnimatedVisibility(true, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })) {
        Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = alignment) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .background(bubbleColor, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(message.body, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
            Text(formatted, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}
