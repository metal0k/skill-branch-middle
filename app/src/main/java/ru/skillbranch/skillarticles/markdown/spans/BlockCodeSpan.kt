package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.markdown.Element


class BlockCodeSpan(
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val bgColor: Int,
    @Px
    private val cornerRadius: Float,
    @Px
    private val padding: Float,
    private val type: Element.BlockCode.Type
) : ReplacementSpan() {
//    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
//    var rect = RectF()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var path = Path()

    var measureWidth: Int = 0


    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        paint.forText {
            val measureText = paint.measureText(text.toString(), start, end)
            measureWidth = (measureText + 2 * padding).toInt()
            if (fm != null)
                when (type) {
                    Element.BlockCode.Type.SINGLE -> {
                        fm.ascent = paint.ascent().minus(2 * padding).toInt()
                        fm.descent = paint.descent().plus(2 * padding).toInt()
                    }
                    Element.BlockCode.Type.START -> {
                        fm.ascent = paint.ascent().minus(2 * padding).toInt()
                        fm.descent = paint.descent().toInt()
                    }

                    Element.BlockCode.Type.MIDDLE -> {
                        fm.ascent = paint.ascent().toInt()
                        fm.descent = paint.descent().toInt()
                    }

                    Element.BlockCode.Type.END -> {
                        fm.ascent = paint.ascent().toInt()
                        fm.descent = paint.descent().plus(2 * padding).toInt()
                    }
                }
        }
        return if (type == Element.BlockCode.Type.SINGLE) 0 else measureWidth
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {

        paint.forBackground {
            path.reset()
            when (type) {
                Element.BlockCode.Type.SINGLE ->
                    path.addRoundRect(
                        RectF(
                            0f,
                            top + padding,
                            canvas.width.toFloat(),
                            bottom - padding
                        ),
                        cornerRadius, cornerRadius,
                        Path.Direction.CW
                    )
                Element.BlockCode.Type.START ->
                    path.addRoundRect(
                        RectF(
                            0f,
                            top + padding,
                            canvas.width.toFloat(),
                            bottom.toFloat()
                        ),
                        floatArrayOf(
                            cornerRadius, cornerRadius, // Top left radius in px
                            cornerRadius, cornerRadius, // Top right radius in px
                            0f, 0f, // Bottom right radius in px
                            0f, 0f // Bottom left radius in px
                        ),
                        Path.Direction.CW
                    )
                Element.BlockCode.Type.MIDDLE ->
                    path.addRect(
                        RectF(
                            0f,
                            top.toFloat(),
                            canvas.width.toFloat(),
                            bottom.toFloat()
                        ),
                        Path.Direction.CW
                    )
                Element.BlockCode.Type.END ->
                    path.addRoundRect(
                        RectF(
                            0f,
                            top.toFloat(),
                            canvas.width.toFloat(),
                            bottom - padding
                        ),
                        floatArrayOf(
                            0f, 0f,
                            0f, 0f,
                            cornerRadius, cornerRadius,
                            cornerRadius, cornerRadius
                        ),
                        Path.Direction.CW
                    )
            }
            canvas.drawPath(path, paint)
        }

        paint.forText {
            canvas.drawText(text, start, end, padding, y.toFloat(), paint)
        }
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldSize = textSize
        val oldStyle = typeface?.style ?: 0
        val oldFont = typeface
        val oldColor = color

        color = textColor
        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)
        textSize *= 0.85f

        block()

        color = oldColor
        typeface = oldFont
        textSize = oldSize
    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style

        color = bgColor
        style = Paint.Style.FILL

        block()

        color = oldColor
        style = oldStyle
    }
}
