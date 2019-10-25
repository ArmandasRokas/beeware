package dk.dtu.group22.beeware.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.businessImpl.HiveBusinessImpl;
import dk.dtu.group22.beeware.business.interfaceBusiness.HiveBusiness;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;
import dk.dtu.group22.beeware.data.repositories.repoImpl.HiveRepoArrayListImpl;

//import java.time.Instant;

public class GraphActivity extends AppCompatActivity {

    private GraphViewModel graphViewModel;

    private Button weightToggle;
    private Button tempToggle;
    private Button lightToggle;
    private Button humidToggle;

    private LineChart lineChart;
    private LineDataSet lineDataSetWeight;
    private LineDataSet lineDataSetTemperature;
    private LineDataSet lineDataSetSunlight;
    private LineDataSet lineDataSetHumidity;

    private final String TAG = "GraphActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_prototype);
        // Model class for this activity. Saves state.
        graphViewModel = ViewModelProviders.of(this).get(GraphViewModel.class);

        Intent intent = getIntent();

        String idString = intent.getStringExtra("idString");

        Log.d(TAG, "onCreate: Got " + idString);


        // Toggle buttons
        weightToggle = findViewById(R.id.weightButton);
        tempToggle = findViewById(R.id.tempButton);
        lightToggle = findViewById(R.id.lightButton);
        humidToggle = findViewById(R.id.humidButton);

        weightToggle.setOnClickListener(v -> {
            toggleWeight(lineDataSetWeight.isVisible());
        });
        tempToggle.setOnClickListener(v -> {
            toggleTemperature(lineDataSetTemperature.isVisible());
        });
        lightToggle.setOnClickListener(v -> {
            toggleSunlight(lineDataSetSunlight.isVisible());
        });
        humidToggle.setOnClickListener(v -> {
            toggleHumidity(lineDataSetHumidity.isVisible());
        });

        // Show / hide menu and status bar
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setPortraitMode();
        } else {
            setLandscapeMode();
        }

        // Find chart in xml
        lineChart = findViewById(R.id.lineChart);

        // Simulate hive data
        HiveRepository hiveRepoArrayList = new HiveRepoArrayListImpl();
        HiveBusiness hiveBusiness = new HiveBusinessImpl(hiveRepoArrayList);
        Hive newHive = new Hive();
        newHive.setId(102);
        Hive rawHiveData = hiveBusiness.getHive(newHive, new Timestamp(0), new Timestamp(System.currentTimeMillis()));

        // Chart interaction settings
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(false);

        // Import LineDataSets
        int numOfDays = 365;
        //lineDataSetWeight = new LineDataSet(randomEntries(numOfDays, 0, 90), "Weight");
        //lineDataSetTemperature = new LineDataSet(randomEntries(numOfDays, -24, 42), "Temperature");
        //lineDataSetSunlight = new LineDataSet(randomEntries(numOfDays, 0, 40), "Sunlight");
        //lineDataSetHumidity = new LineDataSet(randomEntries(numOfDays, 0, 40), "Humidity");
        lineDataSetWeight = new LineDataSet(graphViewModel.extractWeight(rawHiveData), "Weight");
        lineDataSetTemperature = new LineDataSet(graphViewModel.extractTemperature(rawHiveData), "Temperature");
        lineDataSetSunlight = new LineDataSet(graphViewModel.extractIlluminance(rawHiveData), "Sunlight");
        lineDataSetHumidity = new LineDataSet(graphViewModel.extractHumidity(rawHiveData), "Humidity");
        Log.d(TAG, "onCreate: TEST: " + graphViewModel.extractTemperature(rawHiveData).toString());

        // Format X- Axis to time string?
        //      yAxis.setValueFormatter(new MyValueFormatter());


        //Set Y Axis dependency
        lineDataSetWeight.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetTemperature.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSetSunlight.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetHumidity.setAxisDependency(YAxis.AxisDependency.LEFT);

        // Set colors and line width
        lineDataSetWeight.setColors(new int[]{R.color.BEE_graphWeight}, this);
        lineDataSetTemperature.setColors(new int[]{R.color.BEE_graphTemperature}, this);
        lineDataSetSunlight.setColors(new int[]{R.color.BEE_graphSunlight}, this);
        lineDataSetHumidity.setColors(new int[]{R.color.BEE_graphHumidity}, this);

        lineDataSetWeight.setLineWidth(5);
        lineDataSetTemperature.setLineWidth(5);
        lineDataSetSunlight.setLineWidth(1);
        lineDataSetHumidity.setLineWidth(1);

        // Set text size
        lineChart.getXAxis().setTextSize(10);
        lineDataSetWeight.setValueTextSize(10);
        lineDataSetTemperature.setValueTextSize(10);

        // Smooth Curves
        lineDataSetWeight.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetTemperature.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetSunlight.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetHumidity.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // Removing values and circle points from light and humidity graphs
        lineDataSetSunlight.setDrawValues(false);
        lineDataSetSunlight.setDrawCircles(false);
        lineDataSetHumidity.setDrawValues(false);
        lineDataSetHumidity.setDrawCircles(false);

        // Style the light and humidity graphs
        lineDataSetSunlight.setDrawFilled(true);
        lineDataSetHumidity.setDrawFilled(true);
        lineDataSetSunlight.setFillColor(Color.YELLOW);
        lineDataSetHumidity.setFillColor(Color.BLUE);

        //set the transparency of light and humidity
        lineDataSetSunlight.setFillAlpha(20);
        lineDataSetHumidity.setFillAlpha(10);

        // Collect LineDataSets in a List
        List<ILineDataSet> LineDataSetList = Arrays.asList(
                lineDataSetWeight,
                lineDataSetTemperature,
                lineDataSetSunlight,
                lineDataSetHumidity
        );

        // Feed list of LineDataSets into a LineData object
        LineData lineData = new LineData(LineDataSetList);

        // Set description text for LineChart
        Description description = new Description();
        description.setTextColor(ColorTemplate.VORDIPLOM_COLORS[4]);
        description.setText("Hive name");

        // Fill chart with data
        lineChart.setData(lineData);
        lineChart.setDescription(description);

        // Default zoom to one week or 'deafultZoomInDays'
        lineChart.zoom(graphViewModel.getZoom(), 0, graphViewModel.getxCenter(), 0);
        lineChart.centerViewTo((float) numOfDays, (float) 0, lineDataSetWeight.getAxisDependency());
        lineChart.invalidate(); // refresh

        // Get lineDataSet visibility from state.
        lineDataSetWeight.setVisible(graphViewModel.isWeightLineVisible());
        lineDataSetTemperature.setVisible(graphViewModel.isTemperatureLineVisible());
        lineDataSetSunlight.setVisible(graphViewModel.isSunlightLineVisible());
        lineDataSetHumidity.setVisible(graphViewModel.isHumidityLineVisible());
    }

    protected List<Entry> randomEntries(int n, int minY, int maxY) {
        List<Entry> res = new ArrayList<>();
        for (int i = 0; i <= n; ++i) {
            float randY = (float) Math.random() * (maxY - minY + 1) + minY;
            res.add(new Entry((float) i, randY));
        }
        return res;
    }

    private void setPortraitMode() {
        getSupportActionBar().show();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void setLandscapeMode() {
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public void toggleWeight(boolean shown) {
        lineDataSetWeight.setVisible(!shown);
        graphViewModel.setWeightLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleTemperature(boolean shown) {
        lineDataSetTemperature.setVisible(!shown);
        graphViewModel.setTemperatureLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleSunlight(boolean shown) {
        lineDataSetSunlight.setVisible(!shown);
        graphViewModel.setSunlightLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleHumidity(boolean shown) {
        lineDataSetHumidity.setVisible(!shown);
        graphViewModel.setHumidityLineVisible(!shown);
        lineChart.invalidate();
    }

    // Saving state of chart
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        float xCenter = lineChart.getLowestVisibleX() + lineChart.getVisibleXRange() / 2;
        graphViewModel.setxCenter(xCenter);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Get the saved x center and show.
        lineChart.centerViewTo(graphViewModel.getxCenter(), 0, lineDataSetWeight.getAxisDependency());
    }
}

