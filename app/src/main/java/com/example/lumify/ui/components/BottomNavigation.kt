package com.example.lumify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lumify.ui.theme.LumifyTheme

enum class BottomNavItem(
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    HOME("gallery", Icons.Filled.Home, Icons.Outlined.Home, "Home"),
    EDITED("edited", Icons.Filled.Download, Icons.Outlined.Download, "Edited"),
    CAMERA("camera", Icons.Filled.Camera, Icons.Filled.Camera, "Camera")
}

@Composable
fun LumifyBottomNavigation(
    currentRoute: String,
    onNavItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black,
        contentColor = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.values().forEach { navItem ->
                val isSelected = currentRoute == navItem.route

                // Special case for camera
                if (navItem == BottomNavItem.CAMERA) {
                    CameraNavItem(
                        isSelected = isSelected,
                        onClick = { onNavItemClick(navItem.route) }
                    )
                } else {
                    RegularNavItem(
                        icon = if (isSelected) navItem.selectedIcon else navItem.unselectedIcon,
                        label = navItem.label,
                        isSelected = isSelected,
                        onClick = { onNavItemClick(navItem.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RegularNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFFFF8F41) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color(0xFFFF8F41) else Color.Gray
        )
    }
}

@Composable
private fun CameraNavItem(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFFFEEDE4))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Camera,
            contentDescription = "Camera",
            tint = Color(0xFFFF8F41),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LumifyBottomNavigationPreview() {
    LumifyTheme {
        Surface(color = Color.Black) {
            LumifyBottomNavigation(
                currentRoute = BottomNavItem.HOME.route,
                onNavItemClick = {}
            )
        }
    }
}