package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Centralized Branding for Student360.
 * Requirement 1: Usage of Student360 Logo.
 * 
 * Updated: Using the vector 'app_logo' which also serves as the app icon.
 */
object Student360Branding {
    
    @Composable
    fun Logo(
        modifier: Modifier = Modifier,
        size: Dp = 80.dp,
        showText: Boolean = true,
        textColor: Color = Color.White
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Student360 Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            if (showText) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "STUDENT360",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
