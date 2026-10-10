/*
 * Cubiq Launcher
 * Minecraft PC Launcher Authentic UI Components
 */

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.MinecraftFontFamily
import kotlinx.coroutines.delay

enum class MinecraftButtonStyle {
    GREEN,
    STONE,
    DARK,
    RED
}

/**
 * Authentic 3D Textured Minecraft Java Edition Button
 * Exact 1:1 replica of the official Minecraft Java Edition "PLAY" and "New installation" buttons
 */
@Composable
fun MinecraftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MinecraftButtonStyle = MinecraftButtonStyle.GREEN,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String? = null,
    fontSize: TextUnit = 15.sp,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val actuallyPressed = isPressed && enabled && !isLoading

    // Colors matching official Minecraft UI button textures
    val (outerBorder, highlightBar, shadowBar, gradientBody, textColor, textShadow) = when (style) {
        MinecraftButtonStyle.GREEN -> {
            if (!enabled) {
                Tuple6(
                    Color(0xFF4A4A4A),
                    Color(0xFF26522B),
                    Color(0xFF142B17),
                    listOf(Color(0xFF1E3F23), Color(0xFF17311B)),
                    Color(0xFF6B876F),
                    Color(0xFF0E1A10)
                )
            } else if (actuallyPressed) {
                Tuple6(
                    Color(0xFFCCCCCC),
                    Color(0xFF103F1B), // Inverted shadow on top
                    Color(0xFF42A62C), // Inverted highlight on bottom
                    listOf(Color(0xFF205F1C), Color(0xFF184E15)),
                    Color(0xFFDCDCDC),
                    Color(0xFF081F0E)
                )
            } else {
                Tuple6(
                    Color(0xFF1B3D14), // Dark authentic Minecraft green border
                    Color(0xFF7AE842), // Top bright highlight line
                    Color(0xFF1E520F), // Bottom 3D bevel shadow
                    listOf(Color(0xFF58B62F), Color(0xFF3E8E22)), // Vivid Minecraft green
                    Color.White,
                    Color(0xFF0F2B09) // Dark green text shadow
                )
            }
        }
        MinecraftButtonStyle.STONE -> {
            // Light stone 3D button matching Image 3 (Official PC Launcher "New installation" button)
            if (!enabled) {
                Tuple6(
                    Color(0xFF3A3A38),
                    Color(0xFFB5B4AE),
                    Color(0xFF7A7973),
                    listOf(Color(0xFF9E9D97), Color(0xFF8E8D87)),
                    Color(0xFF686762),
                    Color.Transparent
                )
            } else if (actuallyPressed) {
                Tuple6(
                    Color(0xFF22211F),
                    Color(0xFF78766E), // Pressed top shadow
                    Color(0xFFDEDDD8), // Pressed bottom highlight
                    listOf(Color(0xFFABA9A2), Color(0xFF9E9C95)),
                    Color(0xFF1B2B3A),
                    Color.Transparent
                )
            } else {
                Tuple6(
                    Color(0xFF22211F), // Dark border
                    Color(0xFFF0EFEA), // Bright top edge highlight
                    Color(0xFF7E7C74), // Deep 3D bottom bevel shelf
                    listOf(Color(0xFFD6D5CF), Color(0xFFC7C6BF)), // Light stone face
                    Color(0xFF1B2B3A), // Dark navy launcher text
                    Color.Transparent
                )
            }
        }
        MinecraftButtonStyle.DARK -> {
            if (!enabled) {
                Tuple6(
                    Color(0xFF161618),
                    Color(0xFF2A2A2E),
                    Color(0xFF1A1A1C),
                    listOf(Color(0xFF222226), Color(0xFF1A1A1E)),
                    Color(0xFF666666),
                    Color(0xFF101012)
                )
            } else if (actuallyPressed) {
                Tuple6(
                    Color(0xFF111113),
                    Color(0xFF1C1C20),
                    Color(0xFF4A4A52),
                    listOf(Color(0xFF26262B), Color(0xFF1F1F23)),
                    Color(0xFFD0D0D0),
                    Color(0xFF111113)
                )
            } else {
                Tuple6(
                    Color(0xFF111113),
                    Color(0xFF4A4A52), // Top highlight
                    Color(0xFF18181A), // Bottom shadow
                    listOf(Color(0xFF2E2E34), Color(0xFF232328)),
                    Color.White,
                    Color(0xFF101012)
                )
            }
        }
        MinecraftButtonStyle.RED -> {
            if (actuallyPressed) {
                Tuple6(
                    Color(0xFF220808),
                    Color(0xFF3D0E0E),
                    Color(0xFF8E2626),
                    listOf(Color(0xFF4C1515), Color(0xFF381010)),
                    Color(0xFFD0D0D0),
                    Color(0xFF180606)
                )
            } else {
                Tuple6(
                    Color(0xFF200606),
                    Color(0xFFE24646),
                    Color(0xFF4E1010),
                    listOf(Color(0xFF8A2424), Color(0xFF681A1A)),
                    Color.White,
                    Color(0xFF200606)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, if (actuallyPressed) 2 else 0) }
            .clip(RoundedCornerShape(3.dp))
            .border(BorderStroke(1.5.dp, outerBorder), RoundedCornerShape(3.dp))
            .background(Brush.verticalGradient(gradientBody))
            .drawWithContent {
                drawContent()
                val highlightThickness = 2.5.dp.toPx()
                val shadowThickness = 3.dp.toPx()

                // Top highlight bar
                drawRect(
                    color = highlightBar,
                    topLeft = Offset(1.5f, 1.5f),
                    size = Size(size.width - 3f, highlightThickness)
                )
                // Left subtle highlight
                drawLine(
                    color = highlightBar.copy(alpha = 0.6f),
                    start = Offset(1.5f, 1.5f),
                    end = Offset(1.5f, size.height - shadowThickness),
                    strokeWidth = 2f
                )
                // Bottom shadow bar
                drawRect(
                    color = shadowBar,
                    topLeft = Offset(1.5f, size.height - shadowThickness),
                    size = Size(size.width - 3f, shadowThickness)
                )
                // Right subtle shadow
                drawLine(
                    color = shadowBar.copy(alpha = 0.6f),
                    start = Offset(size.width - 1.5f, highlightThickness),
                    end = Offset(size.width - 1.5f, size.height - 1.5f),
                    strokeWidth = 2f
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            MinecraftPixelDonutSpinner(
                size = 22.dp,
                baseColor = Color.White
            )
        } else if (content != null) {
            content()
        } else if (text != null) {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = MinecraftFontFamily,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = TextStyle(
                    shadow = Shadow(
                        color = textShadow,
                        offset = Offset(2f, 2f),
                        blurRadius = 0f
                    )
                )
            )
        }
    }
}

/**
 * Authentic Pixel-Bordered Minecraft Button (1:1 Replica of Image 2)
 */
@Composable
fun MinecraftPixelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String? = null,
    fontSize: TextUnit = 16.sp,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    MinecraftButton(
        onClick = onClick,
        modifier = modifier,
        style = MinecraftButtonStyle.GREEN,
        enabled = enabled,
        isLoading = isLoading,
        text = text,
        fontSize = fontSize,
        content = content
    )
}

/**
 * PC Minecraft Launcher Circular Donut Rotating Loading Animation
 */
@Composable
fun MinecraftPixelDonutSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    baseColor: Color = Color.White
) {
    AsyncImage(
        model = R.drawable.mc_loading_spinner,
        contentDescription = "Loading",
        modifier = modifier.size(size)
    )
}

/**
 * Authentic Minecraft Stone/Bedrock Card with 3D pixel bevel
 */
@Composable
fun MinecraftBlockCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1E1E1E),
    borderColor: Color = Color(0xFF383838),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(3.dp))
            .background(backgroundColor)
            .drawWithContent {
                drawContent()
                // Top highlight
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(0f, 1f),
                    end = Offset(size.width, 1f),
                    strokeWidth = 2f
                )
                // Bottom shadow
                drawLine(
                    color = Color(0x66000000),
                    start = Offset(0f, size.height - 1f),
                    end = Offset(size.width, size.height - 1f),
                    strokeWidth = 2f
                )
            }
    ) {
        content()
    }
}

/**
 * 1:1 Replica of the Minecraft PC Mobile Launcher Bottom Navigation Bar
 * Options: News | Versions | Mods | Profile
 * Matches Image 1: Light Grey PC Launcher tabs with 3D bottom bevels and Green active tab
 */
@Composable
fun MinecraftPCBottomNavBar(
    selectedItem: Int, // 0: News, 1: Versions, 2: Mods, 3: Profile
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    playerHeadContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1B1B1E))
            .border(BorderStroke(1.5.dp, Color(0xFF2C2C32)), RoundedCornerShape(6.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // 0: News
            MinecraftPCNavItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_news),
                        contentDescription = "News",
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = "News",
                isSelected = selectedItem == 0,
                onClick = { onItemSelected(0) }
            )

            // 1: Versions
            MinecraftPCNavItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_versions),
                        contentDescription = "Versions",
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = "Versions",
                isSelected = selectedItem == 1,
                onClick = { onItemSelected(1) }
            )

            // 2: Mods
            MinecraftPCNavItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_mods),
                        contentDescription = "Mods",
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = "Mods",
                isSelected = selectedItem == 2,
                onClick = { onItemSelected(2) }
            )

            // 3: Profile
            MinecraftPCNavItem(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_profile),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                },
                label = "Profile",
                isSelected = selectedItem == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}

@Composable
private fun MinecraftPCNavItem(
    modifier: Modifier = Modifier,
    icon: @Composable (isSelected: Boolean) -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Selected = Minecraft Green Tab, Unselected = Dark Stone PC Tab
    val outerBorder = if (isSelected) {
        if (isPressed) Color(0xFF143D0F) else Color(0xFF1B4E12)
    } else {
        if (isPressed) Color(0xFF18181B) else Color(0xFF24242A)
    }

    val topHighlight = if (isSelected) {
        if (isPressed) Color(0xFF194411) else Color(0xFF5AC636)
    } else {
        if (isPressed) Color(0xFF1E1E22) else Color(0xFF383842)
    }

    val bottomShadow = if (isSelected) {
        if (isPressed) Color(0xFF5AC636) else Color(0xFF194411)
    } else {
        if (isPressed) Color(0xFF383842) else Color(0xFF121215)
    }

    val bgGradient = if (isSelected) {
        if (isPressed) listOf(Color(0xFF286D19), Color(0xFF205814))
        else listOf(Color(0xFF388E23), Color(0xFF2C741B))
    } else {
        if (isPressed) listOf(Color(0xFF222226), Color(0xFF1B1B1F))
        else listOf(Color(0xFF2C2C34), Color(0xFF222228))
    }

    val textColor = if (isSelected) Color.White else Color(0xFFCCCCCC)
    val textShadow = if (isSelected) Color(0xFF0F2B0A) else Color(0xFF101012)

    Column(
        modifier = modifier
            .offset { IntOffset(0, if (isPressed) 2 else 0) }
            .clip(RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, outerBorder), RoundedCornerShape(4.dp))
            .background(Brush.verticalGradient(bgGradient))
            .drawWithContent {
                drawContent()
                val highlightThickness = 2.dp.toPx()
                val shadowThickness = 3.dp.toPx()

                // Top highlight line (or pressed shadow)
                drawRect(
                    color = topHighlight,
                    topLeft = Offset(0.5f, 0.5f),
                    size = Size(size.width - 1f, highlightThickness)
                )
                // Left border highlight
                drawLine(
                    color = topHighlight.copy(alpha = 0.7f),
                    start = Offset(1f, 0.5f),
                    end = Offset(1f, size.height - shadowThickness),
                    strokeWidth = 1.5f
                )
                // Bottom shadow shelf (gives 3D physical height)
                drawRect(
                    color = bottomShadow,
                    topLeft = Offset(0.5f, size.height - shadowThickness),
                    size = Size(size.width - 1f, shadowThickness)
                )
                // Right border shadow
                drawLine(
                    color = bottomShadow.copy(alpha = 0.7f),
                    start = Offset(size.width - 1f, highlightThickness),
                    end = Offset(size.width - 1f, size.height - 0.5f),
                    strokeWidth = 1.5f
                )

                // White accent underline if selected (matching Image 1)
                if (isSelected && !isPressed) {
                    val accentWidth = size.width * 0.55f
                    val accentLeft = (size.width - accentWidth) / 2f
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(accentLeft, size.height - shadowThickness - 2.dp.toPx()),
                        size = Size(accentWidth, 2.dp.toPx())
                    )
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    onClick()
                }
            )
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(isSelected)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = textColor,
            fontFamily = MinecraftFontFamily,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            style = TextStyle(
                shadow = Shadow(
                    color = textShadow,
                    offset = Offset(1.5f, 1.5f),
                    blurRadius = 0f
                )
            )
        )
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)

@Composable
fun MinecraftLinearProgressBar(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = Color(0xFF55FF55),
    trackColor: Color = Color(0xFF141416),
    borderColor: Color = Color(0xFF383838),
    height: Dp = 8.dp
) {
    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(trackColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(2.dp))
    ) {
        val totalWidth = maxWidth
        if (progress != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(color)
            )
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "mc_progress")
            val offsetFraction by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "mc_progress_offset"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(totalWidth * 0.3f)
                    .offset(x = totalWidth * offsetFraction)
                    .background(color)
            )
        }
    }
}

@Composable
fun MinecraftLoadingBlock(
    message: String = "LOADING...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MinecraftLinearProgressBar(
            modifier = Modifier.width(168.dp),
            height = 8.dp
        )
        Text(
            text = message,
            color = Color(0xFFAAAAAA),
            fontFamily = MinecraftFontFamily,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MinecraftLoadingBlock(
    modifier: Modifier
) {
    MinecraftLoadingBlock(message = "LOADING...", modifier = modifier)
}
