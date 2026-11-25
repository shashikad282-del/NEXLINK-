package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberQuartz
import com.example.ui.theme.ArcticCyan
import com.example.ui.theme.CrystalMint
import com.example.ui.theme.RosePrism
import com.example.ui.theme.VioletFrost

enum class EventCategory(
    val label: String,
    val accent: Color
) {
    WORK("Work", ArcticCyan),
    PERSONAL("Personal", CrystalMint),
    FOCUS("Focus", VioletFrost),
    TRAVEL("Travel", AmberQuartz),
    MILESTONE("Milestone", RosePrism);

    companion object {
        fun fromRaw(raw: String?): EventCategory {
            if (raw.isNullOrBlank()) return WORK
            return entries.firstOrNull { it.name == raw } ?: WORK
        }
    }
}
