//package com.example.lumify.ui.screens
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.statusBars
//import androidx.compose.material3.BottomSheetDefaults.DragHandle
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MaterialBottomSheet() {
//    // Current Material 3 implementation
//    val sheetState = rememberModalBottomSheetState()
//    val scope = rememberCoroutineScope()
//
//
//    ModalBottomSheet(
//        onDismissRequest = { /* dismiss sheet */ },
//        sheetState = sheetState,
//        // Limited customization options
//        containerColor = MaterialTheme.colorScheme.surface,
//        contentColor = MaterialTheme.colorScheme.onSurface,
//        tonalElevation = 8.dp,
//        scrimColor = Color.Black.copy(alpha = 0.4f),
//        dragHandle = { DragHandle() } // Limited customization
//    ) {
//        // Content
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text("Bottom Sheet Title", style = MaterialTheme.typography.titleLarge)
//            Spacer(Modifier.height(16.dp))
//            Text("Bottom Sheet Content", style = MaterialTheme.typography.bodyMedium)
//
//            // Sheet actions
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                TextButton(onClick = {
//                    scope.launch { sheetState.hide() }
//                }) {
//                    Text("Cancel")
//                }
//                Button(onClick = {
//                    // Perform action
//                    scope.launch { sheetState.hide() }
//                }) {
//                    Text("Confirm")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun BottomSheetBuilding() {
//    // Building blocks approach
//// 1. Separate behavior controller
//    val sheetController = rememberSheetController(
//        initialValue = SheetValue.Hidden,
//        // Customizable behavior params
//        snapPoints = listOf(0f, 0.5f, 1f), // Hidden, Half-expanded, Fully-expanded
//        resistance = SheetResistance.medium,
//        animationSpec = spring(
//            dampingRatio = 0.8f,
//            stiffness = 400f
//        ),
//        onDismissRequest = { /* dismiss sheet */ }
//    )
//
//// 2. Layout container
//    SheetLayout(
//        controller = sheetController,
//        // Advanced layout configuration
//        edgeToEdge = true,
//        avoidImeInsets = true,
//        gestureDetector = SheetGestureDetector.dragAndTap()
//    ) { sheetState ->
//        // 3. Visual container component
//        MaterialSurface(
//            // Visual styling with tokens
//            shape = MaterialTokens.Shape.extraLarge.copy(
//                bottomStart = CornerSize(0.dp),
//                bottomEnd = CornerSize(0.dp)
//            ),
//            tonalElevation = MaterialTokens.Elevation.level3,
//            // State-based properties
//            color = MaterialTokens.Color.surface,
//            // Control scrim separately
//            scrim = {
//                SheetScrim(
//                    color = MaterialTokens.Color.scrim.copy(
//                        alpha = 0.4f * sheetState.visibleFraction
//                    ),
//                    onTap = { sheetController.hide() }
//                )
//            }
//        ) {
//            // 4. Internal layout structure with slots
//            SheetContent(
//                // Optional handle slot with full customization
//                handle = { handleState ->
//                    SheetHandle(
//                        // State-based styling
//                        width = 32.dp,
//                        height = 4.dp,
//                        color = if (handleState.isDragging)
//                            MaterialTokens.Color.onSurfaceVariant
//                        else
//                            MaterialTokens.Color.onSurfaceVariant.copy(alpha = 0.4f),
//                        // Custom animations
//                        animateOnStateChange = true
//                    )
//                },
//                header = {
//                    SheetHeader(
//                        title = { Text("Bottom Sheet Title") },
//                        // Optional close button
//                        closeButton = {
//                            IconButton(onClick = { sheetController.hide() }) {
//                                Icon(Icons.Default.Close, contentDescription = "Close")
//                            }
//                        }
//                    )
//                },
//                // Main content area with Material guidelines
//                content = {
//                    SheetBody(
//                        contentPadding = PaddingValues(16.dp)
//                    ) {
//                        Text("Bottom Sheet Content")
//
//                        // Access to sheet state for animations
//                        AnimatedVisibility(
//                            visible = sheetState.currentValue == SheetValue.Expanded
//                        ) {
//                            Text("Additional content visible when fully expanded")
//                        }
//                    }
//                },
//                // Actions area with Material specifications
//                actions = {
//                    SheetActions(
//                        arrangement = SheetActionsArrangement.end,
//                        spacing = 8.dp
//                    ) {
//                        TextButton(onClick = { sheetController.hide() }) {
//                            Text("Cancel")
//                        }
//                        Button(onClick = {
//                            // Perform action
//                            sheetController.hide()
//                        }) {
//                            Text("Confirm")
//                        }
//                    }
//                }
//            )
//        }
//    }
//}
//
//@Preview
//@Composable
//private fun TryoutPreview() {
//    MaterialBottomSheet()
//}