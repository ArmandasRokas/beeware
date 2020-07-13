package dk.dtu.group22.beeware.presentation;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Lifecycle;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.CustomActivity;
import dk.dtu.group22.beeware.business.implementation.GraphViewModel;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.implementation.NoDataAvailableOnHivetoolException;

import static java.util.Arrays.asList;

public class Graph extends CustomActivity {
    private long fromDate = 0L, toDate = 0L;
    private int spinnerItem;
    private GraphViewModel graphViewModel;
    private Switch weightSwitch, tempSwitch, lightSwitch, humidSwitch;
    private ConstraintLayout progressBarLayout;
    private LineChart lineChart;
    private List<LineDataSet> lineDataSetWeight, lineDataSetTemperature,
            lineDataSetSunlight, lineDataSetHumidity;
    private YAxis rightYAxis, leftYAxis;
    private TextView leftAxisUnit, rightAxisUnit, noGraphSelectedText;

    private int hiveId;
    private String hiveName;
    private float currentWeight;
    private DownloadHiveAsyncTask downloadAsyncTask;
    private DownloadBGHiveAsyncTask downloadBGAsyncTask;
    private FloatingActionButton graphMenuButton;
  //  private FloatingActionButton hiveSettingsButton;
    private final String TAG = "Graph";
    private Toast toastPast;
    private Toast toastLatest;
    private Toast toastLackingData;
    private Toast toastFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        graphViewModel = ViewModelProviders.of(this).get(GraphViewModel.class);
        toastPast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
        toastLatest = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
        toastLackingData = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);
        toastFailed = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);

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
     //   hiveSettingsButton = findViewById(R.id.hiveSettingsButton);
        leftAxisUnit = findViewById(R.id.axisLeftLegend);
        rightAxisUnit = findViewById(R.id.axisRightLegend);
        noGraphSelectedText = findViewById(R.id.noGraphShownTV);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        graphViewModel.setZoomEnabled(true);

        // Button for changing period
        graphMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GraphTimeSelectionFragment gts = new GraphTimeSelectionFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("hiveID", hiveId);
                if (fromDate != 0L && toDate != 0L) {
                    bundle.putLong("selected1", fromDate);
                    bundle.putLong("selected2", toDate);
                    bundle.putInt("spinnerItem", spinnerItem);
                } else {
                    bundle.putLong("selected1", 0L);
                    bundle.putLong("selected2", 0L);
                    bundle.putInt("spinnerItem", 0);
                }
                gts.setArguments(bundle);
                gts.show(getSupportFragmentManager(), "timeDialog");
            }
        });

   /*     hiveSettingsButton.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFromGraph", false);
            bundle.putInt("hiveID", hiveId);
            ConfigurationFragment fragment = new ConfigurationFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "configurationDialog");
        });*/

        // Get current hive and store in graphViewModel. Graph is drawn in 'onPostExecute'
        downloadAsyncTask = new DownloadHiveAsyncTask(); // Download first month
        downloadBGAsyncTask = new DownloadBGHiveAsyncTask(); // Download the rest
        downloadAsyncTask.execute(hiveId);


        if (!Logic.getSingleton().getCachedHive(hiveId).getHasBeenConfigured()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFromGraph", true);
            bundle.putInt("hiveID", hiveId);
            ConfigurationFragment fragment = new ConfigurationFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "configurationDialog");
        }
    }

    /**
     * Setter called from the GraphTimeSelctionFragment to update local fromDate and toDate.
     */
    public void setPeriod(long fromDate, long toDate, int spinnerItem) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.spinnerItem = spinnerItem;
    }

    /**
     * This method is called in DownloadHiveAsyncTask every time the view is updated.
     * Responsibilities:
     * 1. Set all GraphView settings
     * 2. Initiate importing data from Web
     * 3. Set listeners for switches.
     * 4. Render the GraphView
     * @pre A valid ID is aquired, and connection to hive tool is working
     * @post The graphs are shown
     */
    public void renderGraph() {
        // Find chart in xml
        lineChart = findViewById(R.id.lineChart);

        // Chart interaction settings
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleYEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setNoDataText(getString(R.string.noChartDataText));

        // Y-axis
        rightYAxis = lineChart.getAxisRight();
        leftYAxis = lineChart.getAxisLeft();

        // Extract data from Hive
        try {
            lineDataSetWeight = new ArrayList<>();
            lineDataSetTemperature = new ArrayList<>();
            lineDataSetHumidity = new ArrayList<>();
            lineDataSetSunlight = new ArrayList<>();
            long acceptedDelta;
            List<List<Entry>> tmpWeight;
            List<List<Entry>> tmpTemp = new ArrayList<>();
            List<List<Entry>> tmpLight = new ArrayList<>();
            List<List<Entry>> tmpHumid = new ArrayList<>();

            if (!graphViewModel.useMidnightData()) {
                // Split weight dataset to show gaps in data correctly
                acceptedDelta = 30 * 60 * 1000; // 30 minutes
                tmpWeight = graphViewModel.makeMultiListBasedOnDelta(graphViewModel.extractWeight(), acceptedDelta);
                tmpTemp.add(graphViewModel.extractTemperature());
                tmpLight.add(graphViewModel.extractIlluminance());
                tmpHumid.add(graphViewModel.extractHumidity());
            } else {
                acceptedDelta = 3 * 24 * 60 * 60 * 1000; // Three days
                tmpWeight = graphViewModel.makeMultiListBasedOnDelta(graphViewModel.extractMidnightWeight(), acceptedDelta);
                tmpTemp.add(graphViewModel.extractMiddayTemperature());
                tmpLight.add(graphViewModel.extractThreeDailyPointsIlluminance());
                tmpHumid.add(graphViewModel.extractMiddayHumidity());
            }

            // Collect the lists of weight datasets
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

            // Populate datasets other than weight
            for (List<Entry> list : tmpTemp) {
                lineDataSetTemperature.add(new LineDataSet(list, getString(R.string.Temp)));
            }

            for (List<Entry> list : tmpLight) {
                lineDataSetSunlight.add(new LineDataSet(list, getString(R.string.Sunlight)));
            }

            for (List<Entry> list : tmpHumid) {
                lineDataSetHumidity.add(new LineDataSet(list, getString(R.string.Humidity)));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showEmptyDataSets();
        }

        // Format X- Axis values to strings displaying date and time
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(900000f); // minimum axis-step (interval) is 15 minutes
        xAxis.setValueFormatter(new DateFormatter());
        xAxis.setAxisMinimum(graphViewModel.getFromDate().getTime());
        xAxis.setAxisMaximum(graphViewModel.getToDate().getTime());
        // Set text size for dates on x axis
        lineChart.getXAxis().setTextSize(11);

        //Set Y Axis dependencies, left or right
        for (LineDataSet list : lineDataSetWeight) {
            list.setAxisDependency(YAxis.AxisDependency.LEFT);
        }
        for (LineDataSet list : lineDataSetTemperature) {
            list.setAxisDependency(YAxis.AxisDependency.RIGHT);
        }
        for (LineDataSet list : lineDataSetSunlight) {
            list.setAxisDependency(YAxis.AxisDependency.RIGHT);
        }
        for (LineDataSet list : lineDataSetHumidity) {
            list.setAxisDependency(YAxis.AxisDependency.RIGHT);
        }

        // Scale axises based on data max and min
        leftYAxis.setAxisMaximum(graphViewModel.getLeftAxisMax());
        leftYAxis.setAxisMinimum(graphViewModel.getLeftAxisMin());
        rightYAxis.setAxisMaximum(graphViewModel.getRightAxisMax());
        rightYAxis.setAxisMinimum(graphViewModel.getRightAxisMin());

        // Show temperature unit and values if graph visible
        showYAxisDetails(graphViewModel.isTemperatureLineVisible(), 'r');

        // Style weight
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
            // Show point circles if period long
            if (graphViewModel.useMidnightData()) {
                list.setDrawCircles(true);
            } else {
                list.setDrawCircles(false);
            }
        }

        // Style temperature
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

        // Style humidity
        for (LineDataSet list : lineDataSetHumidity) {
            // Set colors and line width
            list.setColors(new int[]{R.color.BEE_graphHumidity}, this);
            list.setLineWidth(1);

            // Smooth Curves
            list.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            // Removing values and circle points from light and humidity graphs
            list.setDrawValues(false);
            list.setDrawCircles(false);
            list.setDrawFilled(true);
            list.setFillColor(Color.BLUE);
            list.setFillAlpha(20); // Transparency of "fill"
        }

        // Style illuminance
        for (LineDataSet list : lineDataSetSunlight) {
            // Set colors and line width
            list.setColors(new int[]{R.color.BEE_graphSunlight}, this);
            list.setLineWidth(1);

            // Smooth Curves
            list.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            // Removing values and circle points from light and humidity graphs
            list.setDrawValues(false);
            list.setDrawCircles(false);
            list.setDrawFilled(true);
            list.setFillColor(Color.YELLOW);
            list.setFillAlpha(30); // traparency
        }

        // Collect all LineDataSets in a superlist
        List<ILineDataSet> lineDataSetList = new ArrayList<>();
        lineDataSetList.addAll(lineDataSetTemperature);
        lineDataSetList.addAll(lineDataSetHumidity);
        lineDataSetList.addAll(lineDataSetSunlight);
        lineDataSetList.addAll(lineDataSetWeight);

        // Feed list of LineDataSets into a LineData object
        LineData lineData = new LineData(lineDataSetList);

        // Remove description text for LineChart
        Description description = new Description();
        description.setText("");

        // Fill chart with data
        lineChart.setData(lineData);
        // Remove yellow crosshairs
        lineChart.getData().setHighlightEnabled(false);

        lineChart.getLegend().setEnabled(false);
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

    /**
     * Show and hide y axis info (values, units and grid lines), based on y-axis specified (left,
     * right or luminocity).
     * @param show
     * Should the axis be shown after running this method.
     * @param yAxis
     * Axis to show or hide.
     */
    private void showYAxisDetails(boolean show, char yAxis) {
        if (show && yAxis == 'r') {
            rightYAxis.setTextColor(Color.BLACK);
            rightAxisUnit.setTextColor(Color.BLACK);
            rightYAxis.setGridColor(R.color.BEE_graphTemperature);
            rightYAxis.setGridDashedLine(new DashPathEffect(new float[]{8, 4}, 2));
        } else if (yAxis == 'r') {
            rightYAxis.setTextColor(Color.alpha(0));
            rightAxisUnit.setTextColor(Color.alpha(0));
            rightYAxis.setGridColor(Color.alpha(0));
        } else if (show && yAxis == 'l') {
            leftYAxis.setTextColor(Color.BLACK);
            leftAxisUnit.setTextColor(Color.BLACK);
            leftYAxis.setGridColor(Color.GRAY);
        } else if (yAxis == 'l') {
            leftYAxis.setTextColor(Color.alpha(0));
            leftAxisUnit.setTextColor(Color.alpha(0));
            leftYAxis.setGridColor(Color.alpha(0));
        }
    }

    /**
     * Updates the view based on the switch for weight graph.
     * @param shown
     * Is the graph already shown.
     */
    public void toggleWeight(boolean shown) {
        for (LineDataSet list : lineDataSetWeight) {
            list.setVisible(!shown);
        }
        graphViewModel.setWeightLineVisible(!shown);
        showYAxisDetails(graphViewModel.isWeightLineVisible(), 'l'); // Datails = unit and values
        weightSwitch.setChecked(!shown);
        handleNoSwitchesChecked();
        lineChart.invalidate();
    }

    /**
     * Updates the view based on switch for temperature graph.
     * @param shown
     * * Is the graph already shown.
     */
    public void toggleTemperature(boolean shown) {
        for (LineDataSet list : lineDataSetTemperature) {
            list.setVisible(!shown);
        }
        graphViewModel.setTemperatureLineVisible(!shown);
        // Toggle y value visibility
        showYAxisDetails(graphViewModel.isTemperatureLineVisible(), 'r');
        tempSwitch.setChecked(!shown);
        handleNoSwitchesChecked();
        lineChart.invalidate();
    }

    /**
     * Updates the view based on switch for luminocity graph.
     * @param shown
     * Is the graph already shown.
     */
    public void toggleSunlight(boolean shown) {
        for (LineDataSet list : lineDataSetSunlight) {
            list.setVisible(!shown);
        }
        graphViewModel.setSunlightLineVisible(!shown);
        lightSwitch.setChecked(!shown);
        handleNoSwitchesChecked();
        lineChart.invalidate();
    }

    /**
     * Updates the view based on switch for humidity graph.
     * @param shown
     * Is the graph already shown.
     */
    public void toggleHumidity(boolean shown) {
        for (LineDataSet list : lineDataSetHumidity) {
            list.setVisible(!shown);
        }
        graphViewModel.setHumidityLineVisible(!shown);
        humidSwitch.setChecked(!shown);
        handleNoSwitchesChecked();
        lineChart.invalidate();
    }

    /**
     * Populate empty datasets for graph if download fails.
     */
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

    /**
     * Updates the graph view with a new selected time interval.
     * @param from
     * Start of period.
     * @param to
     * End of period.
     */
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
  //          DownloadBGHiveAsyncTask bg2 = new DownloadBGHiveAsyncTask();  // Another download just freezes the app
  //          bg2.execute(hiveId);
//            try {  // Commented out because of:  NetworkOnMainThreadException. Creating new Async task resolves problem
//                graphViewModel.downloadHiveData(hiveId);
//                Log.d(TAG, "downloadHiveData: Downloaded hive data for hive " +
//                        hiveId + " from" + fromDate + " to " + toDate + "."); //  check if fromDate and toDate is correct values. If it makes sense that the data is downloaded between these two dates (that time interval)
//                } catch (Exception e) {
//                Log.d(TAG, "showWithNewTimeDelta: Failed to get new data for rendering.");
//                e.printStackTrace();
//            }
            renderGraph();
            System.out.println("Rendered graph from " + graphViewModel.getFromDate() + " to " + to + ".");
        }
    }

    /**
     * Shows an info text in the graph view if no switches are checked.
     */
    private void handleNoSwitchesChecked() {
        if (!weightSwitch.isChecked()
                && !tempSwitch.isChecked()
                && !humidSwitch.isChecked()
                && !lightSwitch.isChecked()) {
            noGraphSelectedText.setVisibility(View.VISIBLE);
        } else {
            noGraphSelectedText.setVisibility(View.INVISIBLE);
        }
    }

    public void hideProgressBar() {
        progressBarLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * Sets listeners for the graph switches (and automatically toggles them based on current state).
     */
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
        // Sync toggle switches
        weightSwitch.setChecked(graphViewModel.isWeightLineVisible());
        tempSwitch.setChecked(graphViewModel.isTemperatureLineVisible());
        lightSwitch.setChecked(graphViewModel.isSunlightLineVisible());
        humidSwitch.setChecked(graphViewModel.isHumidityLineVisible());
    }

    /**
     * Format dates locally for graph X axis
     */
    private class DateFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.GERMAN);//Locale.ENGLISH);
            String date = simpleDateFormat.format(new Date((long) value));
            return date; //.substring(0, 5);
        }
    }

    /**
     * This Class downloads data and initializes drawing of graphs.
     * @pre
     * The Activity has a valid hive ID and connection to Hive tool.
     * @post
     * First week of data is downloaded, and rendergraph() is called.
     */
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
            } catch (NoDataAvailableOnHivetoolException e){
                //Runnable r2 = () -> Toast.makeText(getApplicationContext(), R.string.LackingData, Toast.LENGTH_LONG).show();
                Runnable r2 = () -> {
                    toastLackingData.setText(R.string.LackingData);
                    toastLackingData.show();
                };
                runOnUiThread(r2);
                e.printStackTrace();
            } catch (IOException e) {
                String errMessage = getString(R.string.FailedToGetLatestData) +  graphViewModel.getHive().getName();
              //  Runnable r2 = () -> Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_LONG).show();
                Runnable r2 = () -> {
                    toastLatest.setText(errMessage);
                    toastLatest.show();
                };
                runOnUiThread(r2);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                setSwitchListeners();
                TextView name = findViewById(R.id.hiveNameTextView);
                name.setText(hiveName);
                renderGraph();
                // Start download of whole hive in bg.
                if (!graphViewModel.isBackgroundDownloadInProgress()) {
                    downloadBGAsyncTask.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
                toastFailed.setText(R.string.FailedToGetHive);
                toastFailed.show();
//                        Toast.makeText(getApplicationContext(), R.string.FailedToGetHive, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * This class controls download of the data not immediately shown when a graph view is opened.
     */
    private class DownloadBGHiveAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... id) {
            try {
                graphViewModel.downloadOldDataInBackground(hiveId);
            }catch (NoDataAvailableOnHivetoolException e){
                //Runnable r2 = () -> Toast.makeText(getApplicationContext(), R.string.LackingData, Toast.LENGTH_LONG).show();
                Runnable r2 = () -> {
                    toastLackingData.setText(R.string.LackingData);
                    toastLackingData.show();
                };
                runOnUiThread(r2);
                e.printStackTrace();
            }catch (IOException e) {
                String errMessage = getString(R.string.FailedToGetPastData) +  graphViewModel.getHive().getName();
                //Runnable r2 = () -> Toast.makeText(getApplicationContext(), errMessage, Toast.LENGTH_LONG).show();
                Runnable r2 = () -> {
                    toastPast.setText(errMessage);
                    toastPast.show();
                };
                runOnUiThread(r2);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgressBar();
            // Check if the activity is closed, when Async call is finished. If yes, not display delayed toasts.
            if(getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED){
                toastPast.cancel();
                toastLatest.cancel();
                toastLackingData.cancel();
                toastFailed.cancel();
            }
        }
    }

    public GraphViewModel getGraphViewModel() {
        return graphViewModel;
    }

    @Override
    protected void onPause() {
        toastPast.cancel();
        toastLatest.cancel();
        toastLackingData.cancel();
        toastFailed.cancel();
        super.onPause();
    }

    @Override
    protected void onStop() {
        toastPast.cancel();
        toastLatest.cancel();
        toastLackingData.cancel();
        toastFailed.cancel();
        super.onStop();
    }
}
