package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.repository.CalendarPreferences
import com.example.ui.components.GlassCard
import com.example.ui.components.NexlinkPrismMark
import com.example.ui.components.NexlinkStudioBadge
import com.example.ui.theme.ArcticCyan
import com.example.ui.theme.CrystalMint
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistSilver
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SlateMuted

@Composable
fun AboutScreen(
    userDisplayName: String?,
    userEmail: String?,
    preferences: CalendarPreferences,
    onToggle24Hour: (Boolean) -> Unit,
    onToggleSundayStart: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.displaySmall,
                        color = IceWhite
                    )
                    Text(
                        text = "PREFERENCES & STUDIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = ArcticCyan
                    )
                }
            }

            // Premium NEXLINK Brand & Product Identity Card
            item {
                GlassCard(
                    cornerRadius = 26.dp,
                    elevated = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("about_nexlink_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                GlassCard(cornerRadius = 18.dp, elevated = true) {
                                    Box(
                                        modifier = Modifier.padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        NexlinkPrismMark(size = 30.dp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.brand_app_display),
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        letterSpacing = 2.6.sp,
                                        color = IceWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.brand_tagline),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MistSilver
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = GlassBorderSubtle, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.brand_developed_by),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IceWhite
                                )
                                Text(
                                    text = "Architectural clarity for daily time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateMuted
                                )
                            }
                            NexlinkStudioBadge()
                        }
                    }
                }
            }

            // Application Version Information
            item {
                GlassCard(
                    cornerRadius = 22.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("about_version_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "VERSION INFORMATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                        VersionInfoRow(
                            label = "Application",
                            value = stringResource(R.string.app_name)
                        )
                        VersionInfoRow(
                            label = "Version",
                            value = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
                        )
                        VersionInfoRow(
                            label = "Publisher",
                            value = stringResource(R.string.brand_developer)
                        )
                        VersionInfoRow(
                            label = "Edition",
                            value = if (BuildConfig.DEBUG) "Preview Build" else "Release"
                        )
                    }
                }
            }

            // Calendar Preferences
            item {
                GlassCard(
                    cornerRadius = 22.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "CALENDAR PREFERENCES",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                        PreferenceToggleRow(
                            icon = Icons.Outlined.Schedule,
                            title = "24-Hour Time",
                            subtitle = "Display timestamps in 00:00 – 23:59 format",
                            checked = preferences.use24HourFormat,
                            onCheckedChange = onToggle24Hour,
                            testTag = "pref_24h_switch"
                        )
                        HorizontalDivider(color = GlassBorderSubtle, thickness = 0.5.dp)
                        PreferenceToggleRow(
                            icon = Icons.Outlined.ViewWeek,
                            title = "Start Week on Sunday",
                            subtitle = "Arrange monthly columns from Sunday to Saturday",
                            checked = preferences.weekStartsOnSunday,
                            onCheckedChange = onToggleSundayStart,
                            testTag = "pref_sunday_switch"
                        )
                    }
                }
            }

            // Cloud Account & Sign Out
            item {
                GlassCard(
                    cornerRadius = 22.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "ACCOUNT & SYNC",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val initial = (userDisplayName?.firstOrNull()
                                ?: userEmail?.firstOrNull()
                                ?: 'N').uppercaseChar().toString()
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GlassSurfaceHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = ArcticCyan
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userDisplayName?.takeIf { it.isNotBlank() } ?: "Google Account",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IceWhite
                                )
                                if (!userEmail.isNullOrBlank()) {
                                    Text(
                                        text = userEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MistSilver
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Synced",
                                    tint = CrystalMint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "SYNCED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrystalMint
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(14.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = SolidColor(DangerCoral.copy(alpha = 0.45f))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = null,
                                tint = DangerCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.action_sign_out),
                                style = MaterialTheme.typography.labelLarge,
                                color = DangerCoral
                            )
                        }
                    }
                }
            }

            // Subtle NEXLINK Footer Signature
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NEXLINK",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp,
                        color = SlateMuted
                    )
                    Text(
                        text = "© 2026 NEXLINK. Designed with precision.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MistSilver
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = IceWhite
        )
    }
}

@Composable
private fun PreferenceToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArcticCyan,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianVoid,
                checkedTrackColor = ArcticCyan,
                uncheckedThumbColor = MistSilver,
                uncheckedTrackColor = ObsidianElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
