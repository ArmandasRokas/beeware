package dk.dtu.group22.beeware.presentation

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class SpecificPositionLabelsXAxisRenderer(
        viewPortHandler: ViewPortHandler,
        xAxis: XAxis,
        trans: Transformer,
        private val specificLabelPositions: FloatArray
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

    override fun drawLabels(c: Canvas, pos: Float, anchor: MPPointF) {
        val labelRotationAngleDegrees = mXAxis.labelRotationAngle
        val positions = FloatArray(specificLabelPositions.size * 2)
        for (i in 0 until positions.size step 2) {
            positions[i] = specificLabelPositions[i / 2]
        }

        mTrans.pointValuesToPixel(positions)

        for (i in 0 until positions.size step 2) {
            var x = positions[i]
            if (mViewPortHandler.isInBoundsX(x)) {
                val label = mXAxis.valueFormatter.getFormattedValue(specificLabelPositions[i / 2], mXAxis)
//                val label = mXAxis.valueFormatter.getFormattedValue(specificLabelPositions[i / 2]);
                if (mXAxis.isAvoidFirstLastClippingEnabled) {
                    // avoid clipping of the last
                    if (i == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
                        val width = Utils.calcTextWidth(mAxisLabelPaint, label)
                        if (width > mViewPortHandler.offsetRight() * 2 && x + width > mViewPortHandler.chartWidth)
                            x -= width / 2
                        // avoid clipping of the first
                    } else if (i == 0) {
                        val width = Utils.calcTextWidth(mAxisLabelPaint, label)
                        x += width / 2
                    }
                }

                drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees)
            }
        }
    }

    override fun renderGridLines(c: Canvas) {
        if (!mXAxis.isDrawGridLinesEnabled() || !mXAxis.isEnabled()) return
        val clipRestoreCount = c.save()
        c.clipRect(getGridClippingRect()!!)
//        if (mRenderGridLinesBuffer.size != mAxis.mEntryCount * 2) {
//            mRenderGridLinesBuffer = FloatArray(mXAxis.mEntryCount * 2)
//        }
        val positions: FloatArray = FloatArray(specificLabelPositions.size * 2)
        run {
            var i = 0
            while (i < positions.size) {
//                positions[i] = mXAxis.mEntries.get(i / 2)
//                positions[i + 1] = mXAxis.mEntries.get(i / 2)
                positions[i] = specificLabelPositions.get(i / 2)
                positions[i + 1] = specificLabelPositions.get(i / 2)
                i += 2
            }
        }
        mTrans.pointValuesToPixel(positions)
        setupGridPaint()
        val gridLinePath: Path = mRenderGridLinesPath
        gridLinePath.reset()
        var i = 0
        while (i < positions.size) {
            drawGridLine(c, positions[i], positions[i + 1], gridLinePath)
            i += 2
        }
        c.restoreToCount(clipRestoreCount)
    }
}