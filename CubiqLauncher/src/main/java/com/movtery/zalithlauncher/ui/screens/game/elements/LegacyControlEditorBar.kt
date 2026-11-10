package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LegacyControlEditorBar(
    isMojo: Boolean,
    onAddButton: () -> Unit,
    onAddDrawer: () -> Unit = {},
    onAddJoystick: () -> Unit,
    onLoadLayout: () -> Unit = {},
    onSave: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xDD18181E))
            .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mode Title
        Text(
            text = if (isMojo) "Mojo Layout Editor" else "Zalith 1 Layout Editor",
            color = Color(0xFF68B7FF),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )

        // + Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x552A2A38))
                .border(0.5.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                .clickable { onAddButton() }
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "➕", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "Button", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }

        // + Drawer
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x552A2A38))
                .border(0.5.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                .clickable { onAddDrawer() }
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📁", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "Drawer", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }

        // + Joystick
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x552A2A38))
                .border(0.5.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                .clickable { onAddJoystick() }
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🕹️", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "Joystick", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }

        // Load Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x552A2A38))
                .border(0.5.dp, Color(0x35FFFFFF), RoundedCornerShape(12.dp))
                .clickable { onLoadLayout() }
                .padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📂", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "Load", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }

        // Save Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xAA1B5E20))
                .border(0.5.dp, Color(0x884CAF50), RoundedCornerShape(12.dp))
                .clickable { onSave() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "💾", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "Save", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Exit Editor
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xAA8B0000))
                .border(0.5.dp, Color(0x88FF5252), RoundedCornerShape(12.dp))
                .clickable { onExit() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "✕", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "Exit", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

