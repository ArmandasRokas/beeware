package dk.dtu.group22.beeware.presentation;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dk.dtu.group22.beeware.R;

import static java.util.Arrays.asList;

//import java.time.Instant;

public class GraphActivity extends AppCompatActivity {

    private GraphViewModel graphViewModel;
    private Switch weightSwitch, tempSwitch, lightSwitch, humidSwitch;
    private ProgressBar progressBar;
    private FloatingActionButton floatingActionButton;

    private LineChart lineChart;
    //private LineDataSet lineDataSetWeight, lineDataSetTemperature,
    //        lineDataSetSunlight, lineDataSetHumidity;
    private List<LineDataSet> lineDataSetWeight, lineDataSetTemperature,
            lineDataSetSunlight, lineDataSetHumidity;
    private int hiveId;
    private String hiveName;
    private float currentWeight;

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
        Log.d(TAG, "onCreate: currentWeigth:" + currentWeight);

        if (hiveId == -1) {
            // TODO: Go home, try again message
        }

        progressBar = findViewById(R.id.progressBar);
        //floatingActionButton = findViewById(R.id.floatingActionButton);
        weightSwitch = findViewById(R.id.weightSwitch);
        tempSwitch = findViewById(R.id.tempSwitch);
        lightSwitch = findViewById(R.id.lightSwitch);
        humidSwitch = findViewById(R.id.humidSwitch);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Default zoom is set here
        graphViewModel.setZoom(100);
        graphViewModel.setZoomEnabled(true);

        //floatingActionButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Log.d(TAG, "onClick: Calendar button clicked");
        //    }
        //});

        // Get current hive and store in graphViewModel. Graph is drawn in 'onPostExecute'
        DownloadHiveAsyncTask asyncTask = new DownloadHiveAsyncTask();
        asyncTask.execute(hiveId);
    }

    // Renders the graph and sets listeners. Called in DownloadHiveAsyncTask
    public void renderGraph() {
        // Find chart in xml
        lineChart = findViewById(R.id.lineChart);

        // Chart interaction settings
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleYEnabled(false);
        lineChart.setScaleXEnabled(true);
        //lineChart.setPinchZoom(graphViewModel.isZoomEnabled());

        try {
            lineDataSetWeight = new ArrayList<>();
            lineDataSetTemperature = new ArrayList<>();
            lineDataSetHumidity = new ArrayList<>();
            lineDataSetSunlight = new ArrayList<>();
            // Weight has to be split when delta is greater than thirty minutes
            long thirtyMinInMillis = 30*60*1000;
            List<List<Entry>> tmpWeight = graphViewModel.makeMultiListBasedOnDelta(graphViewModel.extractWeight(), thirtyMinInMillis);

            // We do not care about the delta for the rest of the data
            List<List<Entry>> tmpTemp = new ArrayList<>();
            tmpTemp.add(graphViewModel.extractTemperature());
            List<List<Entry>> tmpLight = new ArrayList<>();
            tmpLight.add(graphViewModel.extractIlluminance());
            List<List<Entry>> tmpHumid = new ArrayList<>();
            tmpHumid.add(graphViewModel.extractHumidity());

            boolean firstSet = false;
            for (int i = 0; i < tmpWeight.size(); ++i) {
                List<Entry> list = tmpWeight.get(i);
                if (!firstSet) {
                    lineDataSetWeight.add(new LineDataSet(list, "Weight"));
                    firstSet = true;
                } else {
                    LineDataSet tmp = new LineDataSet(list, "shouldNotBeSeen");
                    tmp.setDrawValues(false);
                    lineDataSetWeight.add(tmp);
                }
            }

            for(List<Entry> list : tmpTemp) {
                lineDataSetTemperature.add(new LineDataSet(list, "Temperature"));
            }

            for(List<Entry> list : tmpLight) {
                lineDataSetSunlight.add(new LineDataSet(list, "Sunlight"));
            }

            for(List<Entry> list : tmpHumid){
                lineDataSetHumidity.add(new LineDataSet(list, "Humidity"));
            }
            //Log.d(TAG, "onCreate: TEST: " + graphViewModel.extractTemperature().toString());
        } catch (Exception e) {
            e.printStackTrace();
            showEmptyDataSets();
        }

        // Format X- Axis to time string
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(new DateFormatter());

        //Set Y Axis dependency
        for(LineDataSet list : lineDataSetWeight) {
            list.setAxisDependency(YAxis.AxisDependency.LEFT);
        }

        for(LineDataSet list : lineDataSetTemperature) {
            list.setAxisDependency(YAxis.AxisDependency.RIGHT);
        }

        for(LineDataSet list : lineDataSetSunlight) {
            list.setAxisDependency(YAxis.AxisDependency.LEFT);
        }

        for(LineDataSet list : lineDataSetHumidity){
            list.setAxisDependency(YAxis.AxisDependency.LEFT);
        }

        // Scale axises
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        leftAxis.setAxisMaximum(graphViewModel.getxAxisMax());
        leftAxis.setAxisMinimum(graphViewModel.getxAxisMin());
        rightAxis.setAxisMaximum(graphViewModel.getyAxisMax());
        rightAxis.setAxisMinimum(graphViewModel.getyAxisMin());

        // Weight
        for(LineDataSet list : lineDataSetWeight) {
            // Set colors and line width
            list.setColors(new int[]{R.color.BEE_graphWeight}, this);
            list.setLineWidth(5);
            // Set text size
            list.setValueTextSize(10);
            // Smooth Curves
            list.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            // Removing values and circle points from weight and temp graphs in landscape
            list.setDrawValues(false);
            list.setDrawCircles(false);
        }

        // Temperature
        for(LineDataSet list : lineDataSetTemperature) {
            // Set colors and line width
            list.setColors(new int[]{R.color.BEE_graphTemperature}, this);
            list.setLineWidth(5);

            // Set text size
            list.setValueTextSize(10);

            // Smooth Curves
            list.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            // Removing values and circle points from weight and temp graphs in landscape
            list.setDrawValues(false);
            list.setDrawCircles(false);
        }

        // Humidity
        for(LineDataSet list : lineDataSetHumidity) {
            // Set colors and line width
            list.setColors(new int[]{R.color.BEE_graphHumidity}, this);
            list.setLineWidth(1);

            // Smooth Curves
            list.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            // Removing values and circle points from light and humidity graphs
            list.setDrawValues(false);
            list.setDrawCircles(false);

            // Style the light and humidity graphs
            list.setDrawFilled(true);
            list.setFillColor(Color.BLUE);

            //set the transparency of light and humidity
            list.setFillAlpha(20);
        }

        // Illuminance

        for (LineDataSet list : lineDataSetSunlight) {
            // Set colors and line width
            list.setColors(new int[]{R.color.BEE_graphSunlight}, this);

            list.setLineWidth(1);

            // Smooth Curves
            list.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            // Removing values and circle points from light and humidity graphs
            list.setDrawValues(false);
            list.setDrawCircles(false);

            // Style the light and humidity graphs
            list.setDrawFilled(true);
            list.setFillColor(Color.YELLOW);

            //set the transparency of light and humidity
            list.setFillAlpha(30);
        }



        // Set text size
        lineChart.getXAxis().setTextSize(9);

        // Removing values and circle points from weight and temp graphs in landscape


        // Collect LineDataSets in a List
        List<ILineDataSet> lineDataSetList = new ArrayList<>();
        lineDataSetList.addAll(lineDataSetWeight);
        lineDataSetList.addAll(lineDataSetTemperature);
        lineDataSetList.addAll(lineDataSetHumidity);
        lineDataSetList.addAll(lineDataSetSunlight);

        // Feed list of LineDataSets into a LineData object
        LineData lineData = new LineData(lineDataSetList);

        // Set description text for LineChart
        Description description = new Description();
        description.setTextColor(ColorTemplate.VORDIPLOM_COLORS[4]);
        description.setText(hiveName);

        // Fill chart with data
        lineChart.setData(lineData);
        lineChart.setDescription(description);

        lineChart.setMaxVisibleValueCount(4);
        // You can set default zoom in GraphViewModel
        //lineChart.zoom(graphViewModel.getZoom(), 0, graphViewModel.getxCenter(), 0);
        //lineChart.centerViewTo(graphViewModel.getxCenter(), (float) 0, lineDataSetWeight.getAxisDependency());
        lineChart.invalidate(); // refresh

        // Get lineDataSet visibility from state.
        for(LineDataSet list : lineDataSetWeight){
            list.setVisible(graphViewModel.isWeightLineVisible());
        }

        for(LineDataSet list : lineDataSetTemperature){
            list.setVisible(graphViewModel.isTemperatureLineVisible());
        }

        for(LineDataSet list : lineDataSetSunlight){
            list.setVisible(graphViewModel.isSunlightLineVisible());
        }

        for(LineDataSet list : lineDataSetHumidity){
            list.setVisible(graphViewModel.isHumidityLineVisible());
        }
    }

    // Graph switch listeners use these methods
    public void toggleWeight(boolean shown) {
        for(LineDataSet list : lineDataSetWeight){
            list.setVisible(!shown);
        }
        graphViewModel.setWeightLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleTemperature(boolean shown) {
        for(LineDataSet list : lineDataSetTemperature){
            list.setVisible(!shown);
        }

        graphViewModel.setTemperatureLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleSunlight(boolean shown) {
        for(LineDataSet list : lineDataSetSunlight){
            list.setVisible(!shown);
        }
        graphViewModel.setSunlightLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleHumidity(boolean shown) {
        for(LineDataSet list : lineDataSetHumidity){
            list.setVisible(!shown);
        }
        graphViewModel.setHumidityLineVisible(!shown);
        lineChart.invalidate();
    }

    // Showing empty graph if downloading fails
    void showEmptyDataSets() {
        Log.d(TAG, "onCreate: Could not load hive data.");
        Toast.makeText(this, "Could not load hive data.", Toast.LENGTH_SHORT).show();
        List<Entry> nullEntries = new ArrayList<>();
        nullEntries.add(new Entry(0, 0));
        lineDataSetWeight = new ArrayList<>(asList(new LineDataSet(nullEntries, "Weight")));
        lineDataSetTemperature = new ArrayList<>(asList(new LineDataSet(nullEntries, "Temperature")));
        lineDataSetSunlight = new ArrayList<>(asList(new LineDataSet(nullEntries, "Sunlight")));
        lineDataSetHumidity = new ArrayList<>(asList(new LineDataSet(nullEntries, "Humidity")));
    }

    // Format dates for graph X axis
    private class DateFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.GERMAN);//Locale.ENGLISH);
            String date = simpleDateFormat.format(new Date((long) value));
            return date; //.substring(0, 5);
        }
    }

    // This task downloads data and initializes drawing of graphs.
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
                new Handler().post(() -> renderGraph());
                progressBar.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not get hive data.", Toast.LENGTH_LONG).show();
            }
        }

        private void setSwitchListeners() {
            // Set listener for small switches after download of Hive.
            weightSwitch.setOnClickListener(v -> {
                toggleWeight(lineDataSetWeight.get(0).isVisible());
            });
            tempSwitch.setOnClickListener(v -> {
                toggleTemperature(lineDataSetTemperature.get(0).isVisible());
            });
            lightSwitch.setOnClickListener(v -> {
                toggleSunlight(lineDataSetSunlight.get(0).isVisible());
            });
            humidSwitch.setOnClickListener(v -> {
                toggleHumidity(lineDataSetHumidity.get(0).isVisible());
            });
            weightSwitch.setChecked(graphViewModel.isWeightLineVisible());
            tempSwitch.setChecked(graphViewModel.isTemperatureLineVisible());
            lightSwitch.setChecked(graphViewModel.isSunlightLineVisible());
            humidSwitch.setChecked(graphViewModel.isHumidityLineVisible());
        }
    }
}

