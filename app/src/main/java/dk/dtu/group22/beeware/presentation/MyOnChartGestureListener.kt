package dk.dtu.group22.beeware.presentation

import android.view.MotionEvent
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.ViewPortHandler


class MyOnChartGestureListener(
        val viewPortHandler: ViewPortHandler,
        val graph: Graph
): OnChartGestureListener {
    var currScaleX: Float = 1.0F
    var currScaleY: Float = 1.0F
  //  var currScaleXmin: Float = 1.0F
  //  var currScaleXmax:  Float = 10.0F

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        currScaleX = viewPortHandler.getScaleX()
        currScaleY = viewPortHandler.getScaleY()
        println("Zoom level $currScaleX $currScaleY");
//        if(currScaleX  < currScaleXmin || currScaleX> currScaleXmax){
//            graph.changeLabels(Graph.LabelsInterval.EVERY_HOUR);
//        }

        if(currScaleX> 23.0){
            graph.changeLabels(Graph.LabelsInterval.EVERY_HOUR);
        } else if(currScaleX>1.0){
            graph.changeLabels(Graph.LabelsInterval.EVERY_DAY);
        }
    }

    override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
    }

    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
    }

    override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
    }

    override fun onChartLongPressed(me: MotionEvent?) {
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
    }
}