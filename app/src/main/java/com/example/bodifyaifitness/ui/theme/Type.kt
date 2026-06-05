package com.example.bodifyaifitness.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.bodifyaifitness.R

// ── Font Families ─────────────────────────────────────────────────────────────

/** Logo, display headlines, large numbers (weight, countdown timer). */
val BebasNeue = FontFamily(
    Font(R.font.bebas_neue_regular, FontWeight.Normal)
)

/** General UI — body text, buttons, labels, section titles. Supports Vietnamese. */
val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular,  FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium,   FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold,     FontWeight.Bold),
    Font(R.font.be_vietnam_pro_extrabold, FontWeight.ExtraBold),
)

/** Chat area (AiCoachPage) — comfortable reading with relaxed line height. */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium,  FontWeight.Medium),
    Font(R.font.inter_bold,    FontWeight.Bold),
)

// ── Typography ────────────────────────────────────────────────────────────────

val Typography = Typography(
    // ── Display: Logo, splash screen title ────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),

    // ── Headline: Section headers, screen titles ───────────────────────────
    headlineLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),

    // ── Title: Card headings, dialog titles ───────────────────────────────
    titleLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // ── Body: General description text ────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),

    // ── Label: Buttons, chips, badges, nav items ──────────────────────────
    labelLarge = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BeVietnamPro,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
)

/**
 * TextStyle preset for chat messages (AiCoachPage).
 * Uses Inter with wider line height for readability.
 */
val ChatBodyStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp
)

val ChatBoldStyle = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 22.sp
)
