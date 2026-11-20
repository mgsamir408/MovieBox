package com.moviebox.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(56.dp)) {
        IconButton(onBack, Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Text(title, Modifier.align(Alignment.Center), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
    HorizontalDivider(color = Divider)
}

@Composable
fun SectionHeader(title: String, right: String, rightAccent: Boolean = false, onRight: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(
            right, fontSize = 14.sp, color = if (rightAccent) Accent else Muted,
            modifier = if (onRight != null) Modifier.clickable { onRight() }.padding(8.dp) else Modifier
        )
    }
}

@Composable
fun CardBox(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 7.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Card), content = content)
}

@Composable
fun NavRow(icon: @Composable () -> Unit, label: String, value: String? = null, badge: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.heightIn(min = 56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(12.dp))
        Text(label, Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Medium)
        if (value != null) Text(value, color = Muted, fontSize = 14.sp)
        if (badge != null) Text(badge, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE5484D)).padding(horizontal = 8.dp, vertical = 2.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Muted)
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, desc: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White)
            if (desc != null) Text(desc, color = Muted, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp, end = 12.dp))
        }
        Switch(
            checked, onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Accent, checkedThumbColor = Color.White, uncheckedTrackColor = Color(0xFF4A4C55), uncheckedThumbColor = Color.White, uncheckedBorderColor = Color.Transparent)
        )
    }
}

@Composable
fun Placeholder(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier.padding(horizontal = 16.dp).fillMaxWidth().heightIn(min = 72.dp)
            .clip(RoundedCornerShape(12.dp)).background(Color.Transparent)
            .then(Modifier.background(Color(0x0DFFFFFF))),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center) }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(Gradient).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(text, color = OnAccent, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun RoundIcon(color: Color, size: Int = 44, content: @Composable () -> Unit) {
    Box(Modifier.size(size.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) { content() }
}
