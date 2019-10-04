package dk.dtu.group22.beeware.view;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.R;

public class GraphPrototype extends AppCompatActivity {

    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_prototype);

        chart = findViewById(R.id.lineChart);
        LineDataSet lineDataSet1 = new LineDataSet(randomEntries(100,-10,10), "TestLine1");
        LineDataSet lineDataSet2 = new LineDataSet(randomEntries(100,-10,10), "TestLine2");
        List<ILineDataSet> listOfSets = new ArrayList<>();
        listOfSets.add(lineDataSet1);
        listOfSets.add(lineDataSet2);

        LineData data = new LineData(listOfSets);

       chart.setData(data);
       chart.invalidate(); // refresh
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
