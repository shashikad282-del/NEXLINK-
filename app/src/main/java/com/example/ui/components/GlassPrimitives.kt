package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ArcticCyan
import com.example.ui.theme.CrystalMint
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceLow
import com.example.ui.theme.GlassSurfaceMedium
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistSilver
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SlateMuted

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_glass_ambient_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.42f,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianVoid.copy(alpha = 0.72f),
                            ObsidianBase.copy(alpha = 0.48f),
                            ObsidianVoid.copy(alpha = 0.86f)
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    elevated: Boolean = false,
    accentBorderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fillBrush = remember(elevated) {
        Brush.linearGradient(
            colors = if (elevated) {
                listOf(
                    Color(0x2BFFFFFF),
                    Color(0x16FFFFFF),
                    Color(0x120EA5E9)
                )
            } else {
                listOf(
                    GlassSurfaceMedium,
                    GlassSurfaceLow
                )
            }
        )
    }
    val borderBrush = remember(accentBorderColor) {
        if (accentBorderColor != null) {
            Brush.linearGradient(
                colors = listOf(
                    accentBorderColor.copy(alpha = 0.65f),
                    GlassBorderSubtle,
                    accentBorderColor.copy(alpha = 0.25f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    GlassBorderBright,
                    GlassBorderSubtle,
                    ArcticCyan.copy(alpha = 0.16f)
                )
            )
        }
    }

    val clickableModifier = if (onClick != null) {
        val interactionSource = remember { MutableInteractionSource() }
        Modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = ArcticCyan),
                onClick = onClick
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(fillBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(clickableModifier),
        content = content
    )
}

/**
 * Geometric NEXLINK brand emblem rendered as interlocking beveled crystal planes forming an abstract 'N'.
 */
@Composable
fun NexlinkPrismMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val pad = w * 0.14f
        val leftX = pad
        val rightX = w - pad
        val topY = pad
        val bottomY = h - pad
        val barWidth = w * 0.17f

        // Left vertical crystal blade
        val leftBlade = Path().apply {
            moveTo(leftX, bottomY)
            lineTo(leftX, topY + barWidth * 0.35f)
            lineTo(leftX + barWidth, topY)
            lineTo(leftX + barWidth, bottomY - barWidth * 0.35f)
            close()
        }
        drawPath(
            path = leftBlade,
            brush = Brush.verticalGradient(
                colors = listOf(IceWhite.copy(alpha = 0.92f), ArcticCyan.copy(alpha = 0.45f))
            )
        )

        // Right vertical crystal blade
        val rightBlade = Path().apply {
            moveTo(rightX - barWidth, bottomY)
            lineTo(rightX - barWidth, topY + barWidth * 0.35f)
            lineTo(rightX, topY)
            lineTo(rightX, bottomY - barWidth * 0.35f)
            close()
        }
        drawPath(
            path = rightBlade,
            brush = Brush.verticalGradient(
                colors = listOf(ArcticCyan.copy(alpha = 0.85f), CrystalMint.copy(alpha = 0.40f))
            )
        )

        // Diagonal interconnecting prism beam
        val diagonalBeam = Path().apply {
            moveTo(leftX + barWidth * 0.18f, topY + barWidth * 0.35f)
            lineTo(leftX + barWidth, topY)
            lineTo(rightX - barWidth * 0.18f, bottomY - barWidth * 0.35f)
            lineTo(rightX - barWidth, bottomY)
            close()
        }
        drawPath(
            path = diagonalBeam,
            brush = Brush.linearGradient(
                colors = listOf(
                    IceWhite.copy(alpha = 0.96f),
                    ArcticCyan.copy(alpha = 0.88f)
                ),
                start = Offset(leftX, topY),
                end = Offset(rightX, bottomY)
            )
        )

        // Precision specular hairline frame around outer prism bounds
        drawPath(
            path = leftBlade,
            color = IceWhite.copy(alpha = 0.38f),
            style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = rightBlade,
            color = ArcticCyan.copy(alpha = 0.45f),
            style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun NexlinkStudioBadge(
    modifier: Modifier = Modifier,
    prefix: String? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(GlassSurfaceLow)
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NexlinkPrismMark(size = 14.dp)
        if (!prefix.isNullOrBlank()) {
            Text(
                text = prefix,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )
        }
        Text(
            text = "NEXLINK",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 2.4.sp,
            color = MistSilver
        )
    }
}
