package com.smarttrip.app.ui.language

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val flag: String, val label: String) {
    FRENCH("fr", "🇫🇷", "FR"),
    ENGLISH("en", "🇬🇧", "EN"),
    SPANISH("es", "🇪🇸", "ES"),
    CHINESE("zh", "🇨🇳", "ZH"),
    JAPANESE("ja", "🇯🇵", "JA")
}

object LanguageManager {
    private val _order = listOf(
        AppLanguage.FRENCH,
        AppLanguage.ENGLISH,
        AppLanguage.SPANISH,
        AppLanguage.CHINESE,
        AppLanguage.JAPANESE
    )
    private val _language = MutableStateFlow(AppLanguage.FRENCH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun toggle() {
        val idx = _order.indexOf(_language.value)
        _language.value = _order[(idx + 1) % _order.size]
    }
}

@Composable
fun LanguageToggleButton(modifier: Modifier = Modifier) {
    val language by LanguageManager.language.collectAsState()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .clickable { LanguageManager.toggle() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = language.flag,
            fontSize = 14.sp
        )
        Text(
            text = language.label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}
