package dk.dtu.group22.beeware.presentation;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dk.dtu.group22.beeware.R;

import static java.util.Arrays.asList;

public class GraphActivity extends AppCompatActivity {

    private GraphViewModel graphViewModel;
    private Switch weightSwitch, tempSwitch, lightSwitch, humidSwitch;
    private ConstraintLayout progressBarLayout;
    private LineChart lineChart;
    private List<LineDataSet> lineDataSetWeight, lineDataSetTemperature,
            lineDataSetSunlight, lineDataSetHumidity;

    private int hiveId;
    private String hiveName;
    private float currentWeight;
    private DownloadHiveAsyncTask downloadAsyncTask;
    private DownloadBGHiveAsyncTask downloadBGAsyncTask;
    private FloatingActionButton graphMenuButton;
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

        progressBarLayout = findViewById(R.id.progressBarLayout);
        weightSwitch = findViewById(R.id.weightSwitch);
        tempSwitch = findViewById(R.id.tempSwitch);
        lightSwitch = findViewById(R.id.lightSwitch);
        humidSwitch = findViewById(R.id.humidSwitch);
        graphMenuButton = findViewById(R.id.graphMenuButton);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        graphViewModel.setZoomEnabled(true);

        // Menu button

        graphMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GraphPeriodFragment().show(getSupportFragmentManager(), "timeDialog");
            }
        });

        // Get current hive and store in graphViewModel. Graph is drawn in 'onPostExecute'
        downloadAsyncTask = new DownloadHiveAsyncTask(); // Download first month
        downloadBGAsyncTask = new DownloadBGHiveAsyncTask(); // Download the rest
        downloadAsyncTask.execute(hiveId);
    }

    // Renders the graph and sets listeners. Called in DownloadHiveAsyncTask
    public void renderGraph() {
        // Find chart in xml
        lineChart = findViewById(R.id.lineChart);

        // Chart interaction settings
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleYEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setPinchZoom(true);

        try {
            lineDataSetWeight = new ArrayList<>();
            lineDataSetTemperature = new ArrayList<>();
            lineDataSetHumidity = new ArrayList<>();
            lineDataSetSunlight = new ArrayList<>();
            // Weight has to be split when delta is greater than thirty minutes
            long thirtyMinInMillis = 30 * 60 * 1000;
            List<List<Entry>> tmpWeight = graphViewModel.makeMultiListBasedOnDelta(graphViewModel.extractWeight(), thirtyMinInMillis);

            // We do not care about the delta for the rest of the data
            List<List<Entry>> tmpTemp = new ArrayList<>();
            tmpTemp.add(graphViewModel.extractTemperature());
            List<List<Entry>> tmpLight = new ArrayList<>();
            tmpLight.add(graphViewModel.extractIlluminance());
            List<List<Entry>> tmpHumid = new ArrayList<>();
            tmpHumid.add(graphViewModel.extractHumidity());

            for (int i = 0; i < tmpWeight.size(); ++i) {
                List<Entry> list = tmpWeight.get(i);
                if (i == 0) {
                    lineDataSetWeight.add(new LineDataSet(list, getString(R.string.Weight)));
                } else {
                    LineDataSet tmp = new LineDataSet(list, "");
                    tmp.setForm(Legend.LegendForm.NONE);
                    lineDataSetWeight.add(tmp);
                }
            }

            for (List<Entry> list : tmpTemp) {
                lineDataSetTemperature.add(new LineDataSet(list, getString(R.string.Temp)));
            }

            for (List<Entry> list : tmpLight) {
                lineDataSetSunlight.add(new LineDataSet(list, getString(R.string.Sunlight)));
            }

            for (List<Entry> list : tmpHumid) {
                lineDataSetHumidity.add(new LineDataSet(list, getString(R.string.Humidity)));
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
        xAxis.setAxisMinimum(graphViewModel.getFromDate().getTime());
        xAxis.setAxisMaximum(graphViewModel.getToDate().getTime());


        //Set Y Axis dependency
        for (LineDataSet list : lineDataSetWeight) {
            list.setAxisDependency(YAxis.AxisDependency.LEFT);
        }

        for (LineDataSet list : lineDataSetTemperature) {
            list.setAxisDependency(YAxis.AxisDependency.RIGHT);
        }

        for (LineDataSet list : lineDataSetSunlight) {
            list.setAxisDependency(YAxis.AxisDependency.LEFT);
        }

        for (LineDataSet list : lineDataSetHumidity) {
            list.setAxisDependency(YAxis.AxisDependency.LEFT);
        }

        // Scale axises
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        leftAxis.setAxisMaximum(graphViewModel.getLeftAxisMax());
        leftAxis.setAxisMinimum(graphViewModel.getLeftAxisMin());
        rightAxis.setAxisMaximum(graphViewModel.getRightAxisMax());
        rightAxis.setAxisMinimum(graphViewModel.getRightAxisMin());

        // Weight
        for (LineDataSet list : lineDataSetWeight) {
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
        for (LineDataSet list : lineDataSetTemperature) {
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
        for (LineDataSet list : lineDataSetHumidity) {
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
        lineChart.getXAxis().setTextSize(11);

        // Collect LineDataSets in a List
        List<ILineDataSet> lineDataSetList = new ArrayList<>();
        lineDataSetList.addAll(lineDataSetTemperature);
        lineDataSetList.addAll(lineDataSetHumidity);
        lineDataSetList.addAll(lineDataSetSunlight);
        lineDataSetList.addAll(lineDataSetWeight);

        // Feed list of LineDataSets into a LineData object
        LineData lineData = new LineData(lineDataSetList);

        // Set description text for LineChart
        Description description = new Description();
        description.setTextColor(ColorTemplate.VORDIPLOM_COLORS[4]);
        description.setText(hiveName);

        // Fill chart with data
        lineChart.setData(lineData);
        // Remove yellow crosshairs
        lineChart.getData().setHighlightEnabled(false);
        lineChart.setDescription(description);
        lineChart.invalidate(); // refresh

        // Get lineDataSet visibility from state.
        for (LineDataSet list : lineDataSetWeight) {
            list.setVisible(graphViewModel.isWeightLineVisible());
        }

        for (LineDataSet list : lineDataSetTemperature) {
            list.setVisible(graphViewModel.isTemperatureLineVisible());
        }

        for (LineDataSet list : lineDataSetSunlight) {
            list.setVisible(graphViewModel.isSunlightLineVisible());
        }

        for (LineDataSet list : lineDataSetHumidity) {
            list.setVisible(graphViewModel.isHumidityLineVisible());
        }
    }

    // Graph switch listeners use these methods
    public void toggleWeight(boolean shown) {
        for (LineDataSet list : lineDataSetWeight) {
            list.setVisible(!shown);
        }
        graphViewModel.setWeightLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleTemperature(boolean shown) {
        for (LineDataSet list : lineDataSetTemperature) {
            list.setVisible(!shown);
        }

        graphViewModel.setTemperatureLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleSunlight(boolean shown) {
        for (LineDataSet list : lineDataSetSunlight) {
            list.setVisible(!shown);
        }
        graphViewModel.setSunlightLineVisible(!shown);
        lineChart.invalidate();
    }

    public void toggleHumidity(boolean shown) {
        for (LineDataSet list : lineDataSetHumidity) {
            list.setVisible(!shown);
        }
        graphViewModel.setHumidityLineVisible(!shown);
        lineChart.invalidate();
    }

    // Showing empty graph if downloading fails
    void showEmptyDataSets() {
        Log.d(TAG, "onCreate: Could not load hive data.");
        Toast.makeText(this, R.string.CouldNotLoadHiveData, Toast.LENGTH_SHORT).show();
        List<Entry> nullEntries = new ArrayList<>();
        nullEntries.add(new Entry(0, 0));
        lineDataSetWeight = new ArrayList<>(asList(new LineDataSet(nullEntries, getString(R.string.Weight))));
        lineDataSetTemperature = new ArrayList<>(asList(new LineDataSet(nullEntries, getString(R.string.Temp))));
        lineDataSetSunlight = new ArrayList<>(asList(new LineDataSet(nullEntries, getString(R.string.Sunlight))));
        lineDataSetHumidity = new ArrayList<>(asList(new LineDataSet(nullEntries, getString(R.string.Humidity))));
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

    // Update the graph with the new interval
    public void showWithNewTimeDelta(Timestamp from, Timestamp to) {
        graphViewModel.updateTimePeriod(from, to);
        // Get hive and render with new from- and to-dates.
        if (graphViewModel.getHive() != null &&
                from.before(graphViewModel.getHive().getMeasurements().get(0).getTimestamp()) &&
                graphViewModel.isBackgroundDownloadInProgress()) {
            // If hive exists and requested from date is not present AND data is still downloading:
            Toast.makeText(this, R.string.ThisDataIsStillDownloading, Toast.LENGTH_LONG).show();
        } else {
            // Check if some data is not found, and inform the user. This should only happen if there is incomplete data at the source (hivetool)
            if (graphViewModel.getHive() != null && from.before(graphViewModel.getHive().getMeasurements().get(0).getTimestamp())) {
                // If hive exists and requested from date is not present, but no download is in progress
                Toast.makeText(this, R.string.LackingData, Toast.LENGTH_LONG).show();
            }
            // Try another download just in case.
            try {
                graphViewModel.downloadHiveData(hiveId);
            } catch (Exception e) {
                Log.d(TAG, "showWithNewTimeDelta: Failed to get new data for rendering.");
                e.printStackTrace();
            }
            renderGraph();
            System.out.println("Rendered graph from " + from + " to " + to + ".");
        }
    }

    public void hideProgressBar() {
        progressBarLayout.setVisibility(View.INVISIBLE);
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

    // This task downloads data and initializes drawing of graphs.
    private class DownloadHiveAsyncTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... id) {
            try {
                if (graphViewModel.getHive() == null) {
                    // Downloads recent data.
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
                renderGraph();
                // Start download of whole hive in bg.
                if (!graphViewModel.isBackgroundDownloadInProgress()) {
                    downloadBGAsyncTask.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.FailedToGetHive, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class DownloadBGHiveAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... id) {
            graphViewModel.downloadOldDataInBackground(hiveId);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgressBar();
        }
    }
}


