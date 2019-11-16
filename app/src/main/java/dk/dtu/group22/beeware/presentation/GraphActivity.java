package dk.dtu.group22.beeware.presentation;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dk.dtu.group22.beeware.R;

//import java.time.Instant;

public class GraphActivity extends AppCompatActivity {

    private GraphViewModel graphViewModel;
    private Switch weightSwitch, tempSwitch, lightSwitch, humidSwitch;
    private ProgressBar progressBar;

    private LineChart lineChart;
    private LineDataSet lineDataSetWeight, lineDataSetTemperature,
            lineDataSetSunlight, lineDataSetHumidity;
    private int hiveId;
    private String hiveName;
    private float currentWeight, weightDelta, currentTemp, currentLigth, currentHumidity;
    private int orientation;

    private final String TAG = "GraphActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        graphViewModel = ViewModelProviders.of(this).get(GraphViewModel.class);

        // Get Summary data for weight
        Intent intent = getIntent();
        hiveId = intent.getIntExtra("hiveid", -1);
        hiveName = intent.getStringExtra("hivename");
        currentWeight = intent.getFloatExtra("currentweight", 0);
        weightDelta = Math.abs(intent.getFloatExtra("weightdelta", 0));
        Log.d(TAG, "onCreate: currentWeigth:" + currentWeight);

        if (hiveId == -1) {
            // TODO: Go home, try again message
        }

        progressBar = findViewById(R.id.progressBar);
        weightSwitch = findViewById(R.id.weightSwitch);
        tempSwitch = findViewById(R.id.tempSwitch);
        lightSwitch = findViewById(R.id.lightSwitch);
        humidSwitch = findViewById(R.id.humidSwitch);

        orientation = this.getResources().getConfiguration().orientation;

        // TODO: set these from user defined critical values (with fallback)
        graphViewModel.setLeftAxismax(currentWeight + 15);
        graphViewModel.setLeftAxisMin(currentWeight - 15);
        graphViewModel.setRightAxisMax(40);
        graphViewModel.setRightAxisMin(20);


        // Show / hide activity bar and big switches on rotation
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        graphViewModel.setZoom(101);
        graphViewModel.setZoomEnabled(true);

        // Get current hive and store in graphViewModel. Graph is drawn in 'onPostExecute'
        DownloadHiveAsyncTask asyncTask = new DownloadHiveAsyncTask();
        asyncTask.execute(hiveId);
    }


    // Renders the graph and sets listeners. Called in DownloadHiveAsyncTask
    public void renderGraph() {

        // Update values in summary TODO: Calculate in Viewodel
        currentTemp = 0;
        currentLigth = 0;
        currentHumidity = 0;

        // Find chart in xml
        lineChart = findViewById(R.id.lineChart);

        // Chart interaction settings
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleYEnabled(false);
        lineChart.setScaleXEnabled(true);
        //lineChart.setPinchZoom(graphViewModel.isZoomEnabled());

        try {
            lineDataSetWeight = new LineDataSet(graphViewModel.extractWeight(), "Weight");
            lineDataSetTemperature = new LineDataSet(graphViewModel.extractTemperature(), "Temperature");
            lineDataSetSunlight = new LineDataSet(graphViewModel.extractIlluminance(), "Sunlight");
            lineDataSetHumidity = new LineDataSet(graphViewModel.extractHumidity(), "Humidity");
            Log.d(TAG, "onCreate: TEST: " + graphViewModel.extractTemperature().toString());
        } catch (Exception e) {
            e.printStackTrace();
            showEmptyDatasets();
        }

        // Format X- Axis to time string
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(new DateFormatter());

        //Set Y Axis dependency
        lineDataSetWeight.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetTemperature.setAxisDependency(YAxis.AxisDependency.RIGHT);
        lineDataSetSunlight.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetHumidity.setAxisDependency(YAxis.AxisDependency.LEFT);

        // Scale axises
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        leftAxis.setAxisMaximum(graphViewModel.getLeftAxismax());
        leftAxis.setAxisMinimum(graphViewModel.getLeftAxisMin());
        rightAxis.setAxisMaximum(graphViewModel.getRightAxisMax());
        rightAxis.setAxisMinimum(graphViewModel.getRightAxisMin());

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
        lineChart.getXAxis().setTextSize(9);
        lineDataSetWeight.setValueTextSize(10);
        lineDataSetTemperature.setValueTextSize(10);

        // Smooth Curves
        lineDataSetWeight.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetTemperature.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetSunlight.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSetHumidity.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // Removing values and circle points from weight and temp graphs in landscape
        lineDataSetWeight.setDrawValues(false);
        lineDataSetWeight.setDrawCircles(false);
        lineDataSetTemperature.setDrawValues(false);
        lineDataSetTemperature.setDrawCircles(false);

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
        lineDataSetSunlight.setFillAlpha(30);
        lineDataSetHumidity.setFillAlpha(20);

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
        description.setText(hiveName);

        // Fill chart with data
        lineChart.setData(lineData);
        lineChart.setDescription(description);

        // Default zoom to one week or 'deafultZoomInDays'
        lineChart.zoom(graphViewModel.getZoom(), 0, graphViewModel.getxCenter(), 0);
        lineChart.centerViewTo(graphViewModel.getxCenter(), (float) 0, lineDataSetWeight.getAxisDependency());
        lineChart.invalidate(); // refresh

        // Get lineDataSet visibility from state.
        lineDataSetWeight.setVisible(graphViewModel.isWeightLineVisible());
        lineDataSetTemperature.setVisible(graphViewModel.isTemperatureLineVisible());
        lineDataSetSunlight.setVisible(graphViewModel.isSunlightLineVisible());
        lineDataSetHumidity.setVisible(graphViewModel.isHumidityLineVisible());

    }


    // Replaces action bar with custom_toolbar and sets the title of the activity right
    public void setupToolbar() {
        // Sets the custom_toolbar for the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Calculate ActionBar's height
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(hiveName);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        // Parent Activity defined in AndroidManifest.xml
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


    // Handles layout xml for screen orientation
    private void setPortraitMode() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        graphViewModel.setZoom(14);
        graphViewModel.setZoomEnabled(false);
        setupToolbar();
    }

    // Graph switch listeners use these methods
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

    // Saving state of chart on screen rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            float xCenter = lineChart.getLowestVisibleX() + lineChart.getVisibleXRange() / 2;
            graphViewModel.setxCenter(xCenter);
        } catch (Exception e) {
            Log.d(TAG, "onSaveInstanceState: Could not store zoom.");
        }
    }

    // Restore scrolled point when rotating screen
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Get the saved x center and show
        try {
            lineChart.centerViewTo(graphViewModel.getxCenter(), 0, lineDataSetWeight.getAxisDependency());
        } catch (Exception e) {
            Log.d(TAG, "onRestoreInstanceState: Could not find zoom.");
        }
    }

    // Showing empty graph if downloading fails
    void showEmptyDatasets() {
        Log.d(TAG, "onCreate: Could not load hive data.");
        Toast.makeText(this, "Could not load hive data.", Toast.LENGTH_SHORT).show();
        List<Entry> nullEntries = new ArrayList<>();
        nullEntries.add(new Entry(0, 0));
        lineDataSetWeight = new LineDataSet(nullEntries, "Weight");
        lineDataSetTemperature = new LineDataSet(nullEntries, "Temperature");
        lineDataSetSunlight = new LineDataSet(nullEntries, "Sunlight");
        lineDataSetHumidity = new LineDataSet(nullEntries, "Humidity");
    }

    private String previousDay;

    // Format dates for graph X axis
    private class DateFormatter extends ValueFormatter {

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.GERMAN);//Locale.ENGLISH);
            String date = simpleDateFormat.format(new Date((long) value));

            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return date.substring(0, 5);
            }
            return date;
        }
    }

    // This task downloads data and initalizes drawing of graphs.
    private class DownloadHiveAsyncTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... id) {
            // Todo: pass the real hive
            try {
                // Download data once
                if (graphViewModel.getHive() == null) {
                    graphViewModel.downloadHiveData(hiveId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                setSwitchListeners();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        renderGraph();
                    }
                });
                progressBar.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not get hive data.", Toast.LENGTH_LONG).show();
            }
        }

        private void setSwitchListeners() {
            // Set listener for small switches after download of Hive.
            weightSwitch.setOnClickListener(v -> {
                toggleWeight(lineDataSetWeight.isVisible());
            });
            tempSwitch.setOnClickListener(v -> {
                toggleTemperature(lineDataSetTemperature.isVisible());
            });
            lightSwitch.setOnClickListener(v -> {
                toggleSunlight(lineDataSetSunlight.isVisible());
            });
            humidSwitch.setOnClickListener(v -> {
                toggleHumidity(lineDataSetHumidity.isVisible());
            });
            weightSwitch.setChecked(graphViewModel.isWeightLineVisible());
            tempSwitch.setChecked(graphViewModel.isTemperatureLineVisible());
            lightSwitch.setChecked(graphViewModel.isSunlightLineVisible());
            humidSwitch.setChecked(graphViewModel.isHumidityLineVisible());
        }
    }
}

