package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Centralized Branding for Student360.
 * 
 * Requirement: The result contains ONLY the logo/icon, centered, with no text.
 */
object Student360Branding {
    
    @Composable
    fun Logo(
        modifier: Modifier = Modifier,
        size: Dp = 100.dp
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit
            )
        }
    }
}
