package dk.dtu.group22.beeware.presentation;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.sql.Timestamp;
import java.util.Calendar;

import dk.dtu.group22.beeware.R;

public class GraphTimeAdvancedSelectionFrag extends DialogFragment implements View.OnClickListener {
    private Fragment calendarFragment;
    private TextView  viewPeriod; //, resetButton; // fromDate, toDate,
    private TextView toBasicFragmentButton;
    private Calendar calendarObj = Calendar.getInstance();
    private long spinnerSelection;
    private int daysEntered;
    private long fromDate, toDate;
    private NumberPicker dayPicker100, dayPicker10, dayPicker1;
    private long fromDateChosenInBasic;
    private int skipTwice = 3;
    private int hiveid;

    // Default empty constructor
    public GraphTimeAdvancedSelectionFrag() {
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
        return inflater.inflate(R.layout.fragment_newtime_advanced_selection, container, false);
    }

    // DialogFragment boilerplate - last to run
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((Graph) getActivity()).setLastSelectionFragmentBasic(false);

        // Initialisation
        viewPeriod = view.findViewById(R.id.newTime_viewperiod_btn);
        toBasicFragmentButton = view.findViewById(R.id.toBasicButton);
        viewPeriod.setOnClickListener(this);
        toBasicFragmentButton.setOnClickListener(view1 -> {
            GraphTimeSelectionFragment gts = new GraphTimeSelectionFragment();
            gts.show(getFragmentManager(), "timeDialog");
            // Delayed in order to avoid the flashing screen when dismiss() is called
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 100);
        });
        hiveid = ((Graph) getActivity()).getHiveId();

        // Set starting from and to date or just to date
        fromDate = ((Graph) getActivity()).getFromDate();
        toDate = ((Graph) getActivity()).getToDate();
        setupPickers(view);
        createCalendar();
    }

    private void setupPickers(View view) {
        dayPicker100  = view.findViewById(R.id.dayPicker100);
        dayPicker10  = view.findViewById(R.id.dayPicker10);
        dayPicker1  = view.findViewById(R.id.dayPicker1);

        dayPicker100.setMinValue(0);
        dayPicker100.setMaxValue(9);
        dayPicker10.setMinValue(0);
        dayPicker10.setMaxValue(9);
        dayPicker1.setMinValue(0);
        dayPicker1.setMaxValue(9);

        int selectedNumberOfDays = (int)((toDate-fromDate) / (1000*60*60*24));
        dayPicker100.setValue(selectedNumberOfDays/100);
        dayPicker10.setValue(selectedNumberOfDays % 100 / 10);
        dayPicker1.setValue(selectedNumberOfDays % 100 % 10);
    }

    private void createCalendar() {
        calendarFragment = new GraphTimeCalendarFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.newTime_calendar_frame, calendarFragment).commit();
    }

    /**
     * Handles input from the calendar TextView and the time selection Spinner.
     * @param v
     */
    @Override
    public void onClick(View v) {
            if (v == viewPeriod) {
                 calculateToDate();
            ((Graph) getActivity())
                    .setPeriod(fromDate,toDate);
            ((Graph) getActivity()).showWithNewTimeDelta(new Timestamp(fromDate),
                    new Timestamp(toDate));
            this.dismiss();
        }
    }

    private void calculateToDate() {
        daysEntered = dayPicker1.getValue() + dayPicker10.getValue()*10 + dayPicker100.getValue()*100;
        long insertedToDate = fromDate + daysEntered*DateUtils.DAY_IN_MILLIS;
        toDate = Math.min(insertedToDate, System.currentTimeMillis());
    }

    /**
     * Set by the calendar fragment, and when the spinner changes. Updates the fromDate and toDate
     * according to period and start date.
     * @param  fromDate
     */
    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }
}
