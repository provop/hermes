package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import com.example.ui.HermesViewModel
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.DeviceControlScreen
import com.example.ui.screens.HardwareTriggersScreen
import com.example.ui.screens.HermesBridgeScreen
import com.example.ui.theme.HermesCyan
import com.example.ui.theme.HermesDarkCardBorder
import com.example.ui.theme.HermesDarkSurface
import com.example.ui.theme.HermesTextPrimary
import com.example.ui.theme.HermesTextSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HermesViewModel by viewModels()

    companion object {
        const val EXTRA_START_LISTENING = "com.example.hermes.START_LISTENING"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleTriggerIntent(intent)

        setContent {
            MyApplicationTheme {
                val permissionsToRequest = remember {
                    val list = mutableListOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.CAMERA
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        list.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    list.toTypedArray()
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { /* permissions checked */ }

                LaunchedEffect(Unit) {
                    val needsPrompt = permissionsToRequest.any {
                        ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                    }
                    if (needsPrompt) {
                        permissionLauncher.launch(permissionsToRequest)
                    }
                }

                HermesMainApp(
                    viewModel = viewModel,
                    onRequestRecordAudio = {
                        val hasAudio = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasAudio) {
                            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleTriggerIntent(intent)
    }

    private fun handleTriggerIntent(intent: Intent?) {
        if (intent == null) return
        val shouldListen = intent.getBooleanExtra(EXTRA_START_LISTENING, false) ||
                intent.action == Intent.ACTION_ASSIST ||
                intent.action == Intent.ACTION_VOICE_COMMAND

        if (shouldListen) {
            viewModel.startListening()
        }
    }
}

data class NavigationTabItem(
    val title: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun HermesMainApp(
    viewModel: HermesViewModel,
    onRequestRecordAudio: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavigationTabItem("Assistant", Icons.Default.GraphicEq, "nav_tab_assistant"),
        NavigationTabItem("Hermes Hub", Icons.Default.Send, "nav_tab_hermes"),
        NavigationTabItem("Device Tasks", Icons.Default.PhoneAndroid, "nav_tab_device"),
        NavigationTabItem("Triggers", Icons.Default.SettingsSuggest, "nav_tab_triggers")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A0F1D),
        bottomBar = {
            NavigationBar(
                containerColor = HermesDarkSurface,
                modifier = Modifier.testTag("hermes_bottom_navigation")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) HermesCyan else HermesTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                color = if (isSelected) HermesCyan else HermesTextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = HermesCyan.copy(alpha = 0.2f),
                            selectedIconColor = HermesCyan,
                            selectedTextColor = HermesCyan,
                            unselectedIconColor = HermesTextSecondary,
                            unselectedTextColor = HermesTextSecondary
                        ),
                        modifier = Modifier.testTag(item.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> AssistantScreen(
                viewModel = viewModel,
                onRequestRecordAudioPermission = onRequestRecordAudio,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> HermesBridgeScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> DeviceControlScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> HardwareTriggersScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
