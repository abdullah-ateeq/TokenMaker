package com.example.tokenmaker.ui.util

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticHelper {
    fun performClick(haptic: HapticFeedback, enabled: Boolean) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun performSuccess(haptic: HapticFeedback, enabled: Boolean) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
