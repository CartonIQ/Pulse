package com.cartoniq.pulse.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.pulse.R
import com.cartoniq.pulse.ads.AdManager
import com.cartoniq.pulse.data.PreferencesManager
import com.cartoniq.pulse.ui.components.ControlButton
import com.cartoniq.pulse.ui.components.PulseTopBar
import com.cartoniq.pulse.ui.theme.DeepSpace
import com.cartoniq.pulse.ui.theme.ElectricCyan
import com.cartoniq.pulse.ui.theme.NeonPink
import com.cartoniq.pulse.ui.theme.StatusError
import com.cartoniq.pulse.ui.theme.SurfaceBorder
import com.cartoniq.pulse.ui.theme.SurfaceCard
import com.cartoniq.pulse.ui.theme.TextMuted
import com.cartoniq.pulse.ui.theme.TextPrimary
import com.cartoniq.pulse.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSoundEnabled by remember { mutableStateOf(preferencesManager.isSoundEnabled) }
    var isHapticsEnabled by remember { mutableStateOf(preferencesManager.isHapticsEnabled) }
    var magThreshold by remember { mutableFloatStateOf(preferencesManager.magneticThreshold) }
    var vibThreshold by remember { mutableFloatStateOf(preferencesManager.vibrationThreshold) }
    var sampleRateMode by remember { mutableStateOf(preferencesManager.sampleRateMode) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showHowToUseDialog by remember { mutableStateOf(false) }
    var showUseCasesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PulseTopBar(
                title = stringResource(R.string.settings_title),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = DeepSpace,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group 1: Audio & Haptics Alerts
            SettingsCard(title = stringResource(R.string.settings_group_alerts)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_sound_title),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.settings_sound_desc),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = {
                            isSoundEnabled = it
                            preferencesManager.isSoundEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceBorder
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_haptic_title),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.settings_haptic_desc),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Switch(
                        checked = isHapticsEnabled,
                        onCheckedChange = {
                            isHapticsEnabled = it
                            preferencesManager.isHapticsEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricCyan,
                            checkedTrackColor = ElectricCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceBorder
                        )
                    )
                }
            }

            // Group 2: Threshold Limits
            SettingsCard(title = stringResource(R.string.settings_group_thresholds)) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_magnetic_threshold, magThreshold),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = magThreshold,
                        onValueChange = {
                            magThreshold = it
                            preferencesManager.magneticThreshold = it
                        },
                        valueRange = 30f..300f,
                        steps = 26,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = SurfaceBorder
                        )
                    )

                    Text(
                        text = stringResource(R.string.settings_vibration_threshold, vibThreshold),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Slider(
                        value = vibThreshold,
                        onValueChange = {
                            vibThreshold = it
                            preferencesManager.vibrationThreshold = it
                        },
                        valueRange = 0.5f..20.0f,
                        steps = 38,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonPink,
                            activeTrackColor = NeonPink,
                            inactiveTrackColor = SurfaceBorder
                        )
                    )
                }
            }

            // Group 3: Sampling Frequency
            SettingsCard(title = stringResource(R.string.settings_group_sampling)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = sampleRateMode == PreferencesManager.RATE_NORMAL,
                            onClick = {
                                sampleRateMode = PreferencesManager.RATE_NORMAL
                                preferencesManager.sampleRateMode = PreferencesManager.RATE_NORMAL
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ElectricCyan,
                                unselectedColor = TextMuted
                            )
                        )
                        Text(
                            text = stringResource(R.string.settings_rate_normal),
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = sampleRateMode == PreferencesManager.RATE_GAME,
                            onClick = {
                                sampleRateMode = PreferencesManager.RATE_GAME
                                preferencesManager.sampleRateMode = PreferencesManager.RATE_GAME
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ElectricCyan,
                                unselectedColor = TextMuted
                            )
                        )
                        Text(
                            text = stringResource(R.string.settings_rate_game),
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Group 4: UMP Consent & Privacy Options
            SettingsCard(title = "Privacy & Monetization Consent") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.settings_privacy_note),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    ControlButton(
                        text = stringResource(R.string.btn_manage_privacy),
                        onClick = {
                            showPrivacyDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Group 5: How to Use
            SettingsCard(title = "App Guide") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ControlButton(
                        text = "How to Use Pulse",
                        onClick = { showHowToUseDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ControlButton(
                        text = "Who is this for? (Use Cases)",
                        onClick = { showUseCasesDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Group 6: Reset All Defaults
            SettingsCard(title = stringResource(R.string.settings_group_reset)) {
                ControlButton(
                    text = stringResource(R.string.btn_reset_defaults),
                    onClick = { showResetDialog = true },
                    accentColor = StatusError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showHowToUseDialog) {
        AlertDialog(
            onDismissRequest = { showHowToUseDialog = false },
            title = {
                Text(
                    text = "How to Use Pulse",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pulse turns your phone into a pocket scientific instrument. Here is how to use each tool:\n\n" +
                               "🧲 METAL DETECTOR:\nHold the top edge of your phone close to a wall or object. Slowly move it in a figure-8 motion to calibrate. The needle and color will spike when near metal (like pipes, studs, or wires).\n\n" +
                               "📳 VIBRATION METER:\nPlace your phone flat on a surface (like a washing machine, 3D printer, or desk). Turn on the appliance or tap the table. The real-time graph will show the exact vibration strength.\n\n" +
                               "💡 LIGHT METER:\nPoint the tiny sensor at the top of your phone (near the front camera) directly at a light source or window. Check the Lux number to see if a room has enough light for plants or reading.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHowToUseDialog = false }) {
                    Text(text = "Got it", color = ElectricCyan)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacy & Ads Consent",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Last updated: October 2026\n\n" +
                               "CartoniQ operates the Pulse application.\n\n" +
                               "1. 100% Offline: Pulse uses your phone's hardware sensors. We do NOT collect, store, or share any sensor data, personal information, or location data.\n\n" +
                               "2. Advertisements: We use Google AdMob to display ads. AdMob may collect your device's advertising ID to serve personalized ads. You can manage your ad preferences using the Google settings on your device.\n\n" +
                               "3. Limitation of Liability: The app is provided \"AS IS\". CartoniQ is not liable for any damages or data loss arising from the use of this app.\n\n" +
                               "4. Contact: cartoniq.business@gmail.com",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(text = "Got it", color = ElectricCyan)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showUseCasesDialog) {
        AlertDialog(
            onDismissRequest = { showUseCasesDialog = false },
            title = {
                Text(
                    text = "Who uses Pulse?",
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Here is why people download Pulse:\n\n" +
                               "🧰 DIY & Home:\nFind studs in walls, dropped screws, or metal pipes before drilling.\n\n" +
                               "🔬 Mechanics & Vibration:\nCheck if a washing machine or 3D printer is vibrating too much.\n\n" +
                               "💡 Lighting & Plants:\nMeasure Lux (light) for houseplants, study desks, or photography.\n\n" +
                               "🎓 Education:\nUse the metal detector to show how magnets work for science projects.\n\n" +
                               "📱 Hardware Testing:\nCheck if a used phone's sensors are broken before buying it.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showUseCasesDialog = false }) {
                    Text(text = "Got it", color = ElectricCyan)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_reset_confirm_message),
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        preferencesManager.resetToDefaults()
                        isSoundEnabled = preferencesManager.isSoundEnabled
                        isHapticsEnabled = preferencesManager.isHapticsEnabled
                        magThreshold = preferencesManager.magneticThreshold
                        vibThreshold = preferencesManager.vibrationThreshold
                        sampleRateMode = preferencesManager.sampleRateMode
                        showResetDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.btn_ok), color = StatusError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = stringResource(R.string.btn_cancel), color = TextMuted)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = ElectricCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
