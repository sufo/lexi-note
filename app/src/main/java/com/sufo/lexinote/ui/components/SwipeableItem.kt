package com.sufo.lexinote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Created by sufo on 2025/8/10 00:24.
 *
 */
@Composable
fun SwipeableItem(
    modifier: Modifier = Modifier,
    maxSwipeDistance: Float = 200f,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    offsetX = (offsetX + delta).coerceIn(-maxSwipeDistance, maxSwipeDistance)
                },
                onDragStopped = {
                    when {
                        offsetX > (maxSwipeDistance / 2) -> {
                            onSwipeRight()
                            offsetX = 0f
                        }
                        offsetX < -maxSwipeDistance / 2 -> {
                            onSwipeLeft()
                            offsetX = 0f
                        }
                        else -> {
                            // 回弹动画
                            offsetX = 0f
                        }
                    }
                }
            )
    ) {
        content()
    }
}


@Composable
fun SimpleSwipeItem(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val actionWidth = 160f // 两个按钮总宽度

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        // 整体内容容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset { IntOffset((offsetX + actionWidth).roundToInt(), 0) } // 初始位置偏移
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < -actionWidth / 2) {
                                actionWidth // 完全展开
                            } else {
                                0f // 收起
                            }
                        }
                    ) { _, dragAmount ->
                        val newOffset = offsetX + dragAmount
                        offsetX = newOffset.coerceIn(actionWidth, 0f)
                    }
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                // 主内容区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    content()
                }

                // 操作按钮区域（在左侧）
                Row(
                    modifier = Modifier.width(with(LocalDensity.current) { actionWidth.toDp() })
                ) {
                    Button(
                        onClick = {
                            onEdit()
                            offsetX = 0f
                        },
                        colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Blue),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            onDelete()
                            offsetX = 0f
                        },
                        colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Red),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }


            }
        }
    }
}
