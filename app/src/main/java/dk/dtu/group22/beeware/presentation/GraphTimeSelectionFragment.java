package dk.dtu.group22.beeware.presentation;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Measurement;

public class GraphTimeSelectionFragment extends DialogFragment implements View.OnClickListener {
    private Fragment calendarFragment;
    private TextView viewPeriod; //fromDate, toDate, , resetButton
    private TextView toAdvancedButton;
    private TextView availableFromDateTV;
   // private Spinner spinner;
    private Calendar calendarObj = Calendar.getInstance();
    private long spinnerSelection;
    private long selectedFromDate;
    private int spinnerItem;
    private int skipTwice = 3;
    private int hiveid;
    private NumberPicker periodPicker;
    private String[] periods;
    private Logic logic = Logic.getSingleton();
    private Thread updateAvailableDateFrom;
    private Timestamp availableFromDate;
    private long availablePeriod;
    private final long[] periodsLong = {
            DateUtils.WEEK_IN_MILLIS,
            DateUtils.YEAR_IN_MILLIS / 12,
            DateUtils.YEAR_IN_MILLIS / 12 * 3,
            DateUtils.YEAR_IN_MILLIS / 12 * 6,
            DateUtils.YEAR_IN_MILLIS};

    // Default empty constructor
    public GraphTimeSelectionFragment() {
    }

    // DialogFragment boilerplate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // DialogFragment boilerplate
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_newtime_selection, container, false);
    }

    // DialogFragment boilerplate - last to run
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((Graph) getActivity()).setLastSelectionFragmentBasic(true);
        hiveid = ((Graph) getActivity()).getHiveId();

        updateAvailableDateFrom = new Thread (){
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        System.out.println("Thread is running");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateFromDate();
                                updatePeriodPicker();
                            }
                        });
                        if(!logic.isBackgroundDownloadInProgress()){
                            interrupt();
                        }
                        Thread.sleep(3000);
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        };


        updateAvailableDateFrom.start();

        // Initialisation
        viewPeriod = view.findViewById(R.id.newTime_viewperiod_btn);
        toAdvancedButton = view.findViewById(R.id.toAdvancedButton);
        availableFromDateTV = view.findViewById(R.id.availableFromDateTV);
        viewPeriod.setOnClickListener(this);
        toAdvancedButton.setOnClickListener(view1 -> {
            GraphTimeAdvancedSelectionFrag gts = new GraphTimeAdvancedSelectionFrag();
            gts.show(getFragmentManager(), "timeAdvancedDialog");
            // Delayed in order to avoid the flashing screen when dismiss() is called
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 100);
        });

        periodPicker  = view.findViewById(R.id.periodPicker);
    }

    public void updatePeriodPicker(){
        this.availablePeriod = System.currentTimeMillis() - availableFromDate.getTime();
        int maxValue = getPickerMaxValue(availablePeriod);

        periods = getResources().getStringArray(R.array.time_period);
        periods = Arrays.copyOf(periods, periods.length-(5-maxValue));
        periodPicker.setDisplayedValues(periods);

        periodPicker.setMinValue(0);
        periodPicker.setMaxValue(maxValue);
        periodPicker.setValue(spinnerItem);
        periodPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
            spinnerItem = numberPicker.getValue();
            extractFromDateFromSpinnerItem();
            ((Graph) getActivity()).setSpinnerItem(spinnerItem);
            ((Graph) getActivity()).setFromDate(selectedFromDate);
        });
    }

    private void extractFromDateFromSpinnerItem() {
        switch (spinnerItem) {
            case 0:
                spinnerSelection = availablePeriod;
                break;
            case 1:
                spinnerSelection = periodsLong[0]; // 1 week
                break;
            case 2:
                spinnerSelection = periodsLong[1]; // 1 month
                break;
            case 3:
                spinnerSelection = periodsLong[2]; // 3 months
                break;
            case 4:
                spinnerSelection = periodsLong[3]; // 6 months
                break;
            case 5:
                spinnerSelection = periodsLong[4]; // 1 year
                break;
        }
        selectedFromDate = calendarObj.getTimeInMillis() - spinnerSelection;
    }

    private int getPickerMaxValue(long availablePeriod) {
        int maxValue = 1;
        for(int i = 0; i < periodsLong.length; i++){
            if(availablePeriod > periodsLong[i]){
                maxValue = i+1;
            }
        }
        return maxValue;
    }

    public void updateFromDate(){
        List<Measurement> minMaxMeasurements = logic.fetchMinMaxMeasurementsByTimestamp(hiveid);
        availableFromDate = new Timestamp(minMaxMeasurements.get(0).getTimestamp().getTime() + 1000*60*60*24); // Adds one day just in case

        String availableFromDateText = "";
        if(logic.isBackgroundDownloadInProgress()){
            availableFromDateText +=  getString(R.string.ThisDataIsStillDownloading);
        } else {
            availableFromDateText += getString(R.string.DownloadingIsFinished);
        }
        availableFromDateText += "\n" + getString(R.string.AvailableFromDate) +" " + availableFromDate.toString().substring(0,10) ;

        availableFromDateTV.setText(availableFromDateText);
    }


    /**
     * Handles input from the calendar TextView and the time selection Spinner.
     * @param v
     */
    @Override
    public void onClick(View v) {
            if (v == viewPeriod) {
                ((Graph) getActivity())
                        .setPeriod(selectedFromDate, System.currentTimeMillis(), spinnerItem);
                ((Graph) getActivity()).showWithNewTimeDelta(new Timestamp(selectedFromDate),
                        new Timestamp(System.currentTimeMillis()));
            this.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(updateAvailableDateFrom!=null){
            updateAvailableDateFrom.interrupt();
        }
    }
}
