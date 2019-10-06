package dk.dtu.group22.beeware.view;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.dtu.group22.beeware.R;

public class GraphPrototype extends AppCompatActivity {

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_prototype);
        // Find chart in xml
        lineChart = findViewById(R.id.lineChart);

        // Chart interaction settings
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        // Create (import) LineDataSets
        LineDataSet lineDataSetWeight = new LineDataSet(randomEntries(10, 0, 90), "Weight (KG)");
        LineDataSet lineDataSetTemperature = new LineDataSet(randomEntries(10, -24, 42), "Temperature (C\u00B0)");
        LineDataSet lineDataSetLight = new LineDataSet(randomEntries(10, 0, 40), "Sunlight (Lux)");
        LineDataSet lineDataSetHumidity = new LineDataSet(randomEntries(10, 0, 40), "Humidity (%)");


        // Format X- Axis to time string?
        //      yAxis.setValueFormatter(new MyValueFormatter());

        //Set Y Axis dependency
        lineDataSetWeight.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetTemperature.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSetLight.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetHumidity.setAxisDependency(YAxis.AxisDependency.LEFT);

        // Set colors and line width
        lineDataSetWeight.setColors(new int[]{android.R.color.darker_gray}, this);
        lineDataSetTemperature.setColors(new int[]{android.R.color.holo_red_light}, this);
        lineDataSetLight.setColors(new int[]{android.R.color.holo_orange_light}, this);
        lineDataSetHumidity.setColors(new int[]{android.R.color.holo_blue_light}, this);

        lineDataSetWeight.setLineWidth(5);
        lineDataSetTemperature.setLineWidth(5);
        lineDataSetLight.setLineWidth(2);
        lineDataSetHumidity.setLineWidth(2);

        // Set text size
        lineChart.getXAxis().setTextSize(10);
        lineDataSetWeight.setValueTextSize(10);
        lineDataSetTemperature.setValueTextSize(10);

        // Smooth Curves
        //lineDataSetWeight.setCubicIntensity(0.1f);// Higher is more curved
        lineDataSetWeight.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetTemperature.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetLight.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetHumidity.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // Removing values and circle points from light and humidity graphs
        lineDataSetLight.setDrawValues(false);
        lineDataSetLight.setDrawCircles(false);
        lineDataSetHumidity.setDrawValues(false);
        lineDataSetHumidity.setDrawCircles(false);

        // Style the light and humidity graphs
        lineDataSetLight.setDrawFilled(true);
        lineDataSetHumidity.setDrawFilled(true);
        lineDataSetLight.setFillColor(Color.YELLOW);
        lineDataSetHumidity.setFillColor(Color.BLUE);
        //set the transparency
        lineDataSetLight.setFillAlpha(20);
        lineDataSetHumidity.setFillAlpha(10);

        // Collect LineDataSets in a List
        List<ILineDataSet> listOfSets = Arrays.asList(
                lineDataSetWeight,
                lineDataSetTemperature,
                lineDataSetLight,
                lineDataSetHumidity
        );

        // Feed list of LineDataSets into a LineData object
        LineData lineData = new LineData(listOfSets);

        // Set description text
        Description description = new Description();
        description.setTextColor(ColorTemplate.VORDIPLOM_COLORS[2]);
        description.setText("Example Hive Data");

        // Fill chart with data
        lineChart.setData(lineData);
        lineChart.setDescription(description);
        lineChart.invalidate(); // refresh
    }

    protected List<Entry> randomEntries(int n, int minY, int maxY){
        List<Entry> res = new ArrayList<>();
        for(int i = 0; i<n; ++i){
            float randY = (float) Math.random()*(maxY-minY+1)+minY;
            res.add(new Entry((float) i, randY));
        }
        return res;
    }
}
