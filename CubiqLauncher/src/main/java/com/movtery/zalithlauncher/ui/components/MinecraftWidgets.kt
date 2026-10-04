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
    RED
}

/**
 * Authentic 3D Textured Minecraft Java Edition Button
 * Exact 1:1 replica of the official Minecraft Java Edition "PLAY" button
 */
@Composable
fun MinecraftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MinecraftButtonStyle = MinecraftButtonStyle.GREEN,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String? = null,
    fontSize: TextUnit = 17.sp,
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
                    Color(0xFF0F1E12),
                    Color(0xFF26522B),
                    Color(0xFF142B17),
                    listOf(Color(0xFF1E3F23), Color(0xFF17311B)),
                    Color(0xFF6B876F),
                    Color(0xFF0E1A10)
                )
            } else if (actuallyPressed) {
                Tuple6(
                    Color(0xFF082C13),
                    Color(0xFF0E4A1F), // Inverted shadow on top
                    Color(0xFF38A026), // Inverted highlight on bottom
                    listOf(Color(0xFF125C24), Color(0xFF0E481D)),
                    Color(0xFFE2E2E2),
                    Color(0xFF082C13)
                )
            } else {
                Tuple6(
                    Color(0xFF082C13), // 1px dark edge
                    Color(0xFF55C832), // Top bright highlight line
                    Color(0xFF07441B), // Bottom shadow edge
                    listOf(Color(0xFF1E8D38), Color(0xFF13732D)), // Textured green gradient
                    Color.White,
                    Color(0xFF073315)
                )
            }
        }
        MinecraftButtonStyle.STONE -> {
            if (!enabled) {
                Tuple6(
                    Color(0xFF181818),
                    Color(0xFF383838),
                    Color(0xFF202020),
                    listOf(Color(0xFF303030), Color(0xFF262626)),
                    Color(0xFF6E6E6E),
                    Color(0xFF121212)
                )
            } else if (actuallyPressed) {
                Tuple6(
                    Color(0xFF141414),
                    Color(0xFF222222),
                    Color(0xFF666666),
                    listOf(Color(0xFF323232), Color(0xFF282828)),
                    Color(0xFFD0D0D0),
                    Color(0xFF141414)
                )
            } else {
                Tuple6(
                    Color(0xFF141414),
                    Color(0xFF8C8C8C), // Top highlight
                    Color(0xFF1C1C1C), // Bottom shadow
                    listOf(Color(0xFF4E4E4E), Color(0xFF3C3C3C)),
                    Color.White,
                    Color(0xFF151515)
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
                    topLeft = Offset(1f, 1f),
                    size = Size(size.width - 2f, highlightThickness)
                )
                // Left subtle highlight
                drawLine(
                    color = highlightBar.copy(alpha = 0.6f),
                    start = Offset(1.5f, 1f),
                    end = Offset(1.5f, size.height - shadowThickness),
                    strokeWidth = 2f
                )
                // Bottom shadow bar
                drawRect(
                    color = shadowBar,
                    topLeft = Offset(1f, size.height - shadowThickness),
                    size = Size(size.width - 2f, shadowThickness)
                )
                // Right subtle shadow
                drawLine(
                    color = shadowBar.copy(alpha = 0.6f),
                    start = Offset(size.width - 1.5f, highlightThickness),
                    end = Offset(size.width - 1.5f, size.height - 1f),
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
                letterSpacing = 0.8.sp,
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
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141416))
            .border(BorderStroke(1.dp, Color(0xFF26262A)), RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 0: News
            MinecraftPCNavItem(
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_news),
                        contentDescription = "News",
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(if (isSel) 1.0f else 0.55f)
                    )
                },
                label = "News",
                isSelected = selectedItem == 0,
                onClick = { onItemSelected(0) }
            )

            // 1: Versions
            MinecraftPCNavItem(
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_versions),
                        contentDescription = "Versions",
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(if (isSel) 1.0f else 0.55f)
                    )
                },
                label = "Versions",
                isSelected = selectedItem == 1,
                onClick = { onItemSelected(1) }
            )

            // 2: Mods
            MinecraftPCNavItem(
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_mods),
                        contentDescription = "Mods",
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(if (isSel) 1.0f else 0.55f)
                    )
                },
                label = "Mods",
                isSelected = selectedItem == 2,
                onClick = { onItemSelected(2) }
            )

            // 3: Profile
            MinecraftPCNavItem(
                icon = { isSel ->
                    Image(
                        painter = painterResource(R.drawable.ic_mc_pc_profile),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .alpha(if (isSel) 1.0f else 0.55f)
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
    icon: @Composable (isSelected: Boolean) -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgModifier = if (isPressed) {
        Modifier.background(Color(0xFF28282B), RoundedCornerShape(10.dp))
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(bgModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    com.movtery.zalithlauncher.ui.sound.MinecraftSoundHelper.playClickSound()
                    onClick()
                }
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(isSelected)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF7A7A7E),
            fontFamily = MinecraftFontFamily,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(3.dp))
        // Active indicator line directly beneath label matching media_1791004464013.png
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(Color(0xFF38D122))
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
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
