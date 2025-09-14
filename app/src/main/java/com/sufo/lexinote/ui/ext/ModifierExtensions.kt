package com.sufo.lexinote.ui.ext

/**
 * Created by sufo on 2025/8/9
 *
 */
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 为组件添加虚线边框
 *
 * @param strokeWidth 边框宽度
 * @param color 边框颜色
 * @param dashLength 虚线长度
 * @param gapLength 间隙长度
 * @param cornerRadius 圆角半径
 * @param dashPattern 自定义虚线模式，如果提供则忽略 dashLength 和 gapLength
 * @param phase 虚线偏移量，用于动画
 */
fun Modifier.dashedBorder(
    strokeWidth: Dp = 2.dp,
    color: Color = Color.Gray,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp,
    cornerRadius: Dp = 0.dp,
    dashPattern: FloatArray? = null,
    phase: Float = 0f
) = this.drawBehind {
    val pattern = dashPattern ?: floatArrayOf(
        dashLength.toPx(),
        gapLength.toPx()
    )

    val pathEffect = PathEffect.dashPathEffect(pattern, phase)
    val strokeWidthPx = strokeWidth.toPx()
    val halfStroke = strokeWidthPx / 2

    // 计算边框的实际绘制区域，避免超出边界
    val adjustedSize = androidx.compose.ui.geometry.Size(
        width = size.width - strokeWidthPx,
        height = size.height - strokeWidthPx
    )

    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(halfStroke, halfStroke),
        size = adjustedSize,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = pathEffect
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

/**
 * 只为指定边添加虚线边框
 *
 * @param strokeWidth 边框宽度
 * @param color 边框颜色
 * @param dashLength 虚线长度
 * @param gapLength 间隙长度
 * @param top 是否显示顶部边框
 * @param bottom 是否显示底部边框
 * @param left 是否显示左侧边框
 * @param right 是否显示右侧边框
 */
fun Modifier.dashedBorderSides(
    strokeWidth: Dp = 2.dp,
    color: Color = Color.Gray,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp,
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false
) = this.drawBehind {
    val pathEffect = PathEffect.dashPathEffect(
        floatArrayOf(dashLength.toPx(), gapLength.toPx()),
        0f
    )
    val strokeWidthPx = strokeWidth.toPx()

    // 顶部边框
    if (top) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, strokeWidthPx / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, strokeWidthPx / 2),
            strokeWidth = strokeWidthPx,
            pathEffect = pathEffect
        )
    }

    // 底部边框
    if (bottom) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height - strokeWidthPx / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height - strokeWidthPx / 2),
            strokeWidth = strokeWidthPx,
            pathEffect = pathEffect
        )
    }

    // 左侧边框
    if (left) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(strokeWidthPx / 2, 0f),
            end = androidx.compose.ui.geometry.Offset(strokeWidthPx / 2, size.height),
            strokeWidth = strokeWidthPx,
            pathEffect = pathEffect
        )
    }

    // 右侧边框
    if (right) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width - strokeWidthPx / 2, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width - strokeWidthPx / 2, size.height),
            strokeWidth = strokeWidthPx,
            pathEffect = pathEffect
        )
    }
}

/**
 * 渐变色虚线边框
 *
 * @param strokeWidth 边框宽度
 * @param brush 渐变画笔
 * @param dashLength 虚线长度
 * @param gapLength 间隙长度
 * @param cornerRadius 圆角半径
 */
fun Modifier.dashedBorderGradient(
    strokeWidth: Dp = 2.dp,
    brush: Brush,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp,
    cornerRadius: Dp = 0.dp
) = this.drawBehind {
    val pathEffect = PathEffect.dashPathEffect(
        floatArrayOf(dashLength.toPx(), gapLength.toPx()),
        0f
    )
    val strokeWidthPx = strokeWidth.toPx()
    val halfStroke = strokeWidthPx / 2

    // 计算边框的实际绘制区域，避免超出边界
    val adjustedSize = androidx.compose.ui.geometry.Size(
        width = size.width - strokeWidthPx,
        height = size.height - strokeWidthPx
    )

    drawRoundRect(
        brush = brush,
        topLeft = androidx.compose.ui.geometry.Offset(halfStroke, halfStroke),
        size = adjustedSize,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = pathEffect
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

/**
 * 虚线边框预设样式枚举
 */
enum class DashedBorderStyle {
    SHORT_DASH,    // 短虚线
    MEDIUM_DASH,   // 中等虚线
    LONG_DASH,     // 长虚线
    DOT_DASH,      // 点划线
    CUSTOM         // 自定义
}

/**
 * 使用预设样式的虚线边框
 *
 * @param style 虚线样式
 * @param strokeWidth 边框宽度
 * @param color 边框颜色
 * @param cornerRadius 圆角半径
 * @param customPattern 自定义模式（仅当 style 为 CUSTOM 时使用）
 */
fun Modifier.dashedBorderStyled(
    style: DashedBorderStyle = DashedBorderStyle.MEDIUM_DASH,
    strokeWidth: Dp = 2.dp,
    color: Color = Color.Gray,
    cornerRadius: Dp = 0.dp,
    customPattern: FloatArray? = null
) = this.drawBehind {
    val pattern = when (style) {
        DashedBorderStyle.SHORT_DASH -> floatArrayOf(4.dp.toPx(), 4.dp.toPx())
        DashedBorderStyle.MEDIUM_DASH -> floatArrayOf(8.dp.toPx(), 6.dp.toPx())
        DashedBorderStyle.LONG_DASH -> floatArrayOf(16.dp.toPx(), 8.dp.toPx())
        DashedBorderStyle.DOT_DASH -> floatArrayOf(2.dp.toPx(), 4.dp.toPx(), 8.dp.toPx(), 4.dp.toPx())
        DashedBorderStyle.CUSTOM -> customPattern ?: floatArrayOf(8.dp.toPx(), 6.dp.toPx())
    }

    val pathEffect = PathEffect.dashPathEffect(pattern, 0f)
    val strokeWidthPx = strokeWidth.toPx()
    val halfStroke = strokeWidthPx / 2

    // 计算边框的实际绘制区域，避免超出边界
    val adjustedSize = androidx.compose.ui.geometry.Size(
        width = size.width - strokeWidthPx,
        height = size.height - strokeWidthPx
    )

    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(halfStroke, halfStroke),
        size = adjustedSize,
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = pathEffect
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}





