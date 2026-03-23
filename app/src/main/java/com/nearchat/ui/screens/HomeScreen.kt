package com.nearchat.ui.screens

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearchat.ui.components.UserCard
import com.nearchat.ui.theme.BackgroundDark
import com.nearchat.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenChat: (String, String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val status by viewModel.statusText.collectAsStateWithLifecycle()
    val suggestion by viewModel.suggestedDevice.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(onClick = onOpenProfile) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }
                FloatingActionButton(onClick = { viewModel.discover() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text("NearChat", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
            Text("$status • ${users.size} nearby", color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

            AnimatedVisibility(visible = suggestion != null, enter = fadeIn()) {
                suggestion?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                            .padding(12.dp)
                    ) {
                        Text("Quick reconnect: ${it.displayName}", modifier = Modifier.weight(1f))
                        Text(
                            "Connect",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(users, key = { it.id }) { user ->
                    UserCard(user = user, onConnect = {
                        (context.getSystemService(Vibrator::class.java))?.vibrate(
                            VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                        )
                        viewModel.connect(it)
                        onOpenChat(it.id, it.displayName)
                    })
                }
            }
        }
    }
}
