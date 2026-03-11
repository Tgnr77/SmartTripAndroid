package com.smarttrip.app.ui.screens.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttrip.app.ui.language.AppStrings
import com.smarttrip.app.ui.language.LanguageManager
import com.smarttrip.app.ui.language.LanguageToggleButton
import com.smarttrip.app.ui.theme.Blue600
import com.smarttrip.app.ui.theme.Blue700
import com.smarttrip.app.ui.theme.Blue900

@Composable
fun LandingScreen(
    onNavigateAsGuest: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Blue900,
                    0.55f to Blue700,
                    1f to Blue600
                )
            )
    ) {
        // Decorative background circles
        Box(
            modifier = Modifier
                .size(340.dp)
                .offset(x = 120.dp, y = (-60).dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-80).dp, y = 400.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("✈ SmartTrip", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = strings.landingTitle,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 46.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = strings.landingSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                FeaturePill("🧠", strings.featureAI)
                FeaturePill("📊", strings.featureCompa)
                FeaturePill("🌍", strings.featureInspiration)
            }

            Button(
                onClick = onNavigateAsGuest,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Blue600
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.btnGuestLogin, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.btnSignIn, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }

        // Language selector – top right
        LanguageToggleButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        )
    }
}

@Composable
private fun FeaturePill(emoji: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 22.sp)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}
