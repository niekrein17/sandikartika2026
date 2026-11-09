package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayloadClassification
import com.example.ui.theme.TacticalAlertRed
import com.example.ui.theme.TacticalAlertRedDim
import com.example.ui.theme.TacticalBlack
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalCardBorderActive
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalDecoyBlue
import com.example.ui.theme.TacticalDecoySurface
import com.example.ui.theme.TacticalEmerald
import com.example.ui.theme.TacticalEmeraldGlow
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun TacticalHeader(
    onPanicZeroize: () -> Unit,
    onLockCamouflage: () -> Unit,
    onOpenDisguise: () -> Unit = {},
    onOpenWebSuite: () -> Unit = {},
    activeDisguiseName: String = "SANDI-KARTIKA",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TacticalDarkSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top-Center Logo: SANDI on left, LOGO in center, KARTIKA on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SANDI",
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                letterSpacing = 2.sp,
                color = TacticalTextPrimary,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.width(12.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_sandi_kartika),
                contentDescription = "Logo Sandi Kartika",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "KARTIKA",
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                letterSpacing = 2.sp,
                color = TacticalTextPrimary,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. Subtitle / Description: (ENKRIPSI & STEGANOGRAFI - PUSSIBERAD)
        Text(
            text = "(ENKRIPSI & STEGANOGRAFI - PUSSIBERAD)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TacticalEmerald,
            letterSpacing = 0.8.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 4 Feature Icons Row relocated under description: Web Suite, Kamuflase, Kalkulator, Zeroize
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TacticalHeaderActionButton(
                icon = Icons.Default.Visibility,
                label = "WEB SUITE",
                tint = TacticalEmerald,
                borderColor = TacticalEmerald.copy(alpha = 0.6f),
                backgroundColor = TacticalCardSurface,
                testTag = "open_web_suite_button",
                onClick = onOpenWebSuite
            )

            TacticalHeaderActionButton(
                icon = Icons.Default.Lock,
                label = "KAMUFLASE",
                tint = TacticalCyan,
                borderColor = TacticalCyan.copy(alpha = 0.6f),
                backgroundColor = TacticalCardSurface,
                testTag = "app_disguise_button",
                onClick = onOpenDisguise
            )

            TacticalHeaderActionButton(
                icon = Icons.Default.Shield,
                label = "KALKULATOR",
                tint = TacticalGold,
                borderColor = TacticalGold.copy(alpha = 0.6f),
                backgroundColor = TacticalCardSurface,
                testTag = "lock_to_calculator_button",
                onClick = onLockCamouflage
            )

            TacticalHeaderActionButton(
                icon = Icons.Default.DeleteSweep,
                label = "ZEROIZE",
                tint = TacticalAlertRed,
                borderColor = TacticalAlertRed,
                backgroundColor = TacticalAlertRedDim,
                testTag = "panic_zeroize_button",
                onClick = onPanicZeroize
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Air-Gap & Crypto Enclave Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(TacticalCardSurface)
                .border(1.dp, TacticalCardBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(alphaAnim)
                        .clip(CircleShape)
                        .background(TacticalEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STATUS: 100% OFFLINE (AIR-GAPPED)",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TacticalEmerald,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "KEDOK: ${activeDisguiseName.uppercase()}",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = TacticalCyan,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun TacticalHeaderActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    borderColor: Color,
    backgroundColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun TacticalCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    badgeText: String? = null,
    badgeColor: Color = TacticalEmerald,
    borderColor: Color = TacticalCardBorder,
    backgroundColor: Color = TacticalCardSurface,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(3.dp, 14.dp)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = TacticalTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        content()
    }
}

@Composable
fun TacticalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    isMonospace: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: ImageVector? = null,
    testTag: String = ""
) {
    var passwordVisible by remember { mutableStateOf(!isPassword) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TacticalTextSecondary
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TacticalTextMuted
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = TacticalEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = TacticalTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        visualTransformation = if (!passwordVisible && isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        singleLine = singleLine,
        maxLines = maxLines,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TacticalTextPrimary,
            fontSize = 13.sp,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TacticalEmerald,
            unfocusedBorderColor = TacticalCardBorder,
            focusedContainerColor = TacticalDarkSurface,
            unfocusedContainerColor = TacticalDarkSurface,
            cursorColor = TacticalEmerald
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
fun TacticalClassificationBadge(
    classification: PayloadClassification,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (classification) {
        PayloadClassification.TOP_SECRET -> Triple(TacticalAlertRedDim, TacticalAlertRed, TacticalAlertRed)
        PayloadClassification.CONFIDENTIAL -> Triple(TacticalGold.copy(alpha = 0.15f), TacticalGold, TacticalGold)
        PayloadClassification.RESTRICTED -> Triple(TacticalCyan.copy(alpha = 0.15f), TacticalCyan, TacticalCyan)
        PayloadClassification.DECOY_ROUTINE -> Triple(TacticalDecoySurface, TacticalDecoyBlue, TacticalDecoyBlue)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = classification.displayName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CapacityGauge(
    usedBytes: Int,
    maxBytes: Int,
    modifier: Modifier = Modifier
) {
    val ratio = if (maxBytes > 0) (usedBytes.toFloat() / maxBytes.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (ratio * 100f)

    val progressColor = when {
        percent < 10f -> TacticalEmerald
        percent < 40f -> TacticalGold
        else -> TacticalAlertRed
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "KAPASITAS CITRA STEGO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TacticalTextSecondary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "$usedBytes B / ${maxBytes / 1024} KB (${"%.1f".format(percent)}%)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = progressColor,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = TacticalCardBorder
        )
    }
}
