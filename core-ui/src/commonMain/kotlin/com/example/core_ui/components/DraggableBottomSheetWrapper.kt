package com.example.core_ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.core_ui.resources.LocalDimensionResources
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DraggableBottomSheetWrapper(
    onDismiss: () -> Unit,
    fillMaxHeight: Float? = null,
    dragOnlyFromHandle: Boolean = false,
    content: @Composable () -> Unit
) {
    val dimensions = LocalDimensionResources.current
    val density = LocalDensity.current
    val screenHeight = with(density) { 800.dp.toPx() } // Approximate screen height
    val sheetHeight = fillMaxHeight?.let { screenHeight * it } ?: (screenHeight * 0.5f) // Fallback for drag calculations
    
    var dragOffsetY by remember { mutableStateOf(0f) }
    val dismissThreshold = sheetHeight * 0.3f // Dismiss if dragged down 30% of sheet height

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        isVisible = true
    }
    
    val entranceOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else sheetHeight,
        animationSpec = tween(durationMillis = 300),
        label = "entranceAnimation"
    )
    
    // Combine entrance animation with drag offset
    val totalOffset = entranceOffset + dragOffsetY

    // Background overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        // Bottom sheet content
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .let { modifier ->
                    if (fillMaxHeight != null) {
                        modifier.fillMaxHeight(fillMaxHeight)
                    } else {
                        modifier.wrapContentHeight()
                    }
                }
                .offset { IntOffset(0, totalOffset.roundToInt()) }
                .clip(RoundedCornerShape(topStart = dimensions.cornerRadiusXLarge, topEnd = dimensions.cornerRadiusXLarge))
                .background(Color.White)
                .clickable { /* Prevent click through */ }
                .let { modifier ->
                    if (!dragOnlyFromHandle) {
                        modifier.pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    var isDragging = false
                                    
                                    drag(down.id) { change ->
                                        val dragAmount = change.position - change.previousPosition
                                        
                                        // If we haven't started dragging yet, check if this is a vertical drag
                                        if (!isDragging) {
                                            if (abs(dragAmount.y) > abs(dragAmount.x) &&
                                                abs(dragAmount.y) > 5f) {
                                                isDragging = true
                                            }
                                        }
                                        
                                        // If we're dragging vertically, handle it
                                        if (isDragging && dragAmount.y > 0) {
                                            val newOffset = dragOffsetY + dragAmount.y
                                            dragOffsetY = if (newOffset > 0) newOffset else 0f
                                            change.consume()
                                        }
                                    }
                                    
                                    // Handle drag end
                                    if (dragOffsetY > dismissThreshold) {
                                        onDismiss()
                                    } else {
                                        dragOffsetY = 0f
                                    }
                                }
                            }
                        }
                    } else {
                        modifier
                    }
                }
        ) {
            Column {
                // Drag handle area - larger touch target
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensions.paddingMedium)
                        .height(32.dp) // Larger touch area
                        .let { modifier ->
                            if (dragOnlyFromHandle) {
                                modifier.pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            var isDragging = false
                                            
                                            drag(down.id) { change ->
                                                val dragAmount = change.position - change.previousPosition
                                                
                                                // If we haven't started dragging yet, check if this is a vertical drag
                                                if (!isDragging) {
                                                    if (abs(dragAmount.y) > abs(dragAmount.x) &&
                                                        abs(dragAmount.y) > 5f) {
                                                        isDragging = true
                                                    }
                                                }
                                                
                                                // If we're dragging vertically, handle it
                                                if (isDragging && dragAmount.y > 0) {
                                                    val newOffset = dragOffsetY + dragAmount.y
                                                    dragOffsetY = if (newOffset > 0) newOffset else 0f
                                                    change.consume()
                                                }
                                            }
                                            
                                            // Handle drag end
                                            if (dragOffsetY > dismissThreshold) {
                                                onDismiss()
                                            } else {
                                                dragOffsetY = 0f
                                            }
                                        }
                                    }
                                }
                            } else {
                                modifier
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(dimensions.dragHandleWidth)
                            .height(dimensions.dragHandleHeight)
                            .background(
                                Color.Gray.copy(alpha = 0.5f),
                                RoundedCornerShape(dimensions.paddingXSmall)
                            )
                    )
                }
                
                // Content
                content()
            }
        }
    }
}