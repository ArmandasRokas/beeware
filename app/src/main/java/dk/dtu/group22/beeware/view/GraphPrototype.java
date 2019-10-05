package dk.dtu.group22.beeware.view;

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
import java.util.List;

import dk.dtu.group22.beeware.R;

public class GraphPrototype extends AppCompatActivity {

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_prototype);
        // Link chart in xml
        lineChart = findViewById(R.id.lineChart);

        // Create (import) LineDataSets
        LineDataSet lineDataSet1 = new LineDataSet(randomEntries(10, 0, 90), "Weight (KG)");
        LineDataSet lineDataSet2 = new LineDataSet(randomEntries(10, -10, 42), "Temperature (C\u00B0)");

        // Format X- Axis to time
        //yAxis.setValueFormatter(new MyValueFormatter());

        //Set Y Axis dependency
        YAxis leftAxis = lineChart.getAxis(YAxis.AxisDependency.LEFT);
        YAxis rightAxis = lineChart.getAxis(YAxis.AxisDependency.RIGHT);
        lineDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet2.setAxisDependency(YAxis.AxisDependency.RIGHT);

        // Set colors
        lineDataSet1.setColors(new int[]{android.R.color.darker_gray}, this);
        lineDataSet2.setColors(new int[]{android.R.color.holo_orange_dark}, this);

        // Collect LineDataSets in a List
        List<ILineDataSet> listOfSets = new ArrayList<>();
        listOfSets.add(lineDataSet1);
        listOfSets.add(lineDataSet2);

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
