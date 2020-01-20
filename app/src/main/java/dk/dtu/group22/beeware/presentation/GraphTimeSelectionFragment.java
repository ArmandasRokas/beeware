package dk.dtu.group22.beeware.presentation;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import dk.dtu.group22.beeware.R;

public class GraphTimeSelectionFragment extends DialogFragment implements View.OnClickListener {
    private Fragment calendarFragment;
    private TextView fromDate, toDate, viewPeriod, resetButton;
    private ImageView settingsButton;
    private Spinner spinner;
    private Calendar calendarObj = Calendar.getInstance();
    private long spinnerSelection;
    private long selectedDate = 0L;
    private int spinnerItem = 0;
    private int skipTwice = 3;

    // Apparently needed
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

        // Initialisation
        fromDate = view.findViewById(R.id.newTime_from_text);
        toDate = view.findViewById(R.id.newTime_to_text);
        spinner = view.findViewById(R.id.newTime_spinner);
        viewPeriod = view.findViewById(R.id.newTime_viewperiod_btn);
        settingsButton = view.findViewById(R.id.newTime_settings);
        resetButton = view.findViewById(R.id.newTimeResetButton);

        fromDate.setOnClickListener(this);
        viewPeriod.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);

        // Set starting from and to date or just to date
        long givenFromDate = this.getArguments().getLong("selected1");
        long givenToDate = this.getArguments().getLong("selected2");
        spinnerItem = this.getArguments().getInt("spinnerItem");
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
        if (givenFromDate != 0L && givenToDate != 0L) {
            skipTwice = 0;
            fromDate.setText(dateFormat.format(givenFromDate));
            toDate.setText(dateFormat.format(givenToDate));
            selectedDate = givenFromDate;
            spinnerSelection = givenToDate - givenFromDate;
        }

        spinnerHandler();
    }

    public void setFromDate() {
        if (calendarFragment != null) {
        } else {
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
            if (selectedDate != 0L &&
                    (selectedDate + spinnerSelection) < calendarObj.getTimeInMillis()) {
                fromDate.setText(dateFormat.format(selectedDate));
            } else {
                fromDate.setText(dateFormat
                        .format(calendarObj.getTimeInMillis() - spinnerSelection));
                selectedDate = calendarObj.getTimeInMillis() - spinnerSelection;
            }
        }
    }

    public void refreshCalendar() {
        if (calendarFragment != null) {
            // If the calendar was already open, then it needs to be closed so when it opens
            // again, it is refreshed
            getChildFragmentManager().beginTransaction().remove(calendarFragment).commit();
        } else {
            // The code reaches this point, if the spinner is changed with the calendar closed
            return;
        }
        // Refreshes (re-opens) the calendar fragment
        Bundle bundle = new Bundle();
        calendarFragment = new GraphTimeCalendarFragment();
        bundle.putLong("min", (calendarObj.getTimeInMillis() - DateUtils.YEAR_IN_MILLIS));
        bundle.putLong("max", (calendarObj.getTimeInMillis() - spinnerSelection));

        if (selectedDate > (calendarObj.getTimeInMillis() - spinnerSelection)) {
            // If the selected date is closer to 'today' when the spinner says it should not be
            selectedDate = calendarObj.getTimeInMillis() - spinnerSelection;
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
            fromDate.setText(dateFormat.format(selectedDate));
        } else {
            // If it is not closer to 'today' then send where it is at so the calendar fragment
            // can show it.
            bundle.putLong("selected", selectedDate);
        }
        calendarFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().replace(R.id.newTime_calendar_frame,
                calendarFragment).commit();
    }

    @Override
    public void onClick(View v) {
        if (v == fromDate) {
            // User wants to see calendar to change date
            if (calendarFragment != null) {
                // If the user wants to close the shown calendar
                getChildFragmentManager().beginTransaction().remove(calendarFragment).commit();
                calendarFragment = null;
            } else {
                // If the user wants to open the calendar
                Bundle bundle = new Bundle();
                calendarFragment = new GraphTimeCalendarFragment();
                bundle.putLong("min", (calendarObj.getTimeInMillis() - DateUtils.YEAR_IN_MILLIS));
                bundle.putLong("max", (calendarObj.getTimeInMillis() - spinnerSelection));
                if (selectedDate != 0L) {
                    bundle.putLong("selected", selectedDate);
                }
                calendarFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.newTime_calendar_frame, calendarFragment).commit();
            }
        } else if (v == viewPeriod) {
            // User wants to close fragment and see the updated time period
            ((Graph) getActivity())
                    .setPeriod(selectedDate, (selectedDate + spinnerSelection), spinnerItem);
            ((Graph) getActivity()).showWithNewTimeDelta(new Timestamp(selectedDate),
                    new Timestamp(selectedDate + spinnerSelection));
            this.dismiss();
        } else if (v == settingsButton) {
            new ConfigNewtimeFragment().show(getFragmentManager(), "configurationDialog");
        } else if (v == resetButton) {
            spinnerSelection = 0;
            selectedDate = calendarObj.getTimeInMillis() - DateUtils.WEEK_IN_MILLIS;
            spinnerItem = 0;
            skipTwice = 3;
            spinnerHandler();
        }
    }

    // Needed for class to know what is selected in the spinner
    public void spinnerHandler() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.time_period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (skipTwice < 2) {
                    if (spinnerItem == 0) {
                        skipTwice = 3;
                    }
                    spinner.setSelection(spinnerItem);
                    skipTwice++;
                    return;
                }
                spinnerItem = position;
                switch (spinnerItem) {
                    case 0:
                        spinnerSelection = DateUtils.WEEK_IN_MILLIS; // 1 week
                        break;
                    case 1:
                        spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12; // 1 month
                        break;
                    case 2:
                        spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12 * 3; // 3 months
                        break;
                    case 3:
                        spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12 * 6; // 6 months
                        break;
                    case 4:
                        spinnerSelection = DateUtils.YEAR_IN_MILLIS; // 1 year
                        break;
                }
                refreshCalendar();

                setFromDate();
                setSelectedDate(selectedDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // This is set by the calendar fragment and at spinner changes
    public void setSelectedDate(long selectedDate) {
        this.selectedDate = selectedDate;
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
        fromDate.setText(dateFormat.format(selectedDate));
        toDate.setText(dateFormat.format(selectedDate + spinnerSelection));
    }

}
