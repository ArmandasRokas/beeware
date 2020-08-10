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
    // private Spinner spinner;
    private Calendar calendarObj = Calendar.getInstance();
    private long spinnerSelection;
    private int daysEntered;
//    private EditText daysEditText;
    private long fromDate, toDate;
    private NumberPicker dayPicker100, dayPicker10, dayPicker1;
    private long fromDateChosenInBasic;
//    private int spinnerItem = 0;
    private int skipTwice = 3;
    private int hiveid;
  //  private NumberPicker periodPicker;
//    private String[] periods;

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
//        fromDate = view.findViewById(R.id.newTime_from_text);
//        toDate = view.findViewById(R.id.newTime_to_text);
        // spinner = view.findViewById(R.id.newTime_spinner);
        viewPeriod = view.findViewById(R.id.newTime_viewperiod_btn);
        toBasicFragmentButton = view.findViewById(R.id.toBasicButton);
//        resetButton = view.findViewById(R.id.newTimeResetButton);
//        daysEditText = view.findViewById(R.id.daysEditText);
//        fromDate.setVisibility(View.INVISIBLE);
//        toDate.setVisibility(View.INVISIBLE);
//        resetButton.setVisibility(View.INVISIBLE);

//        fromDate.setOnClickListener(this);
        viewPeriod.setOnClickListener(this);
        toBasicFragmentButton.setOnClickListener(view1 -> {
            GraphTimeSelectionFragment gts = new GraphTimeSelectionFragment();
//            Bundle bundle = new Bundle();
//            bundle.putInt("hiveID", hiveid);
//            if (selectedFromDate != 0L && selectedFromDate +spinnerSelection != 0L) {
//                bundle.putLong("selected1", fromDateChosenInBasic);
//                bundle.putLong("selected2", System.currentTimeMillis());
//                bundle.putInt("spinnerItem", spinnerItem);
//            } else {
//                bundle.putLong("selected1", 0L);
//                bundle.putLong("selected2", 0L);
//                bundle.putInt("spinnerItem", 0);
//            }
//            gts.setArguments(bundle);
            gts.show(getFragmentManager(), "timeDialog");
            // Delayed in order to avoid the flashing screen when dismiss() is called
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 100);
        });

//        resetButton.setOnClickListener(this);

//        hiveid = getArguments().getInt("hiveID", 0);
        hiveid = ((Graph) getActivity()).getHiveId();

        // Set starting from and to date or just to date
        fromDate = ((Graph) getActivity()).getFromDate();
//        long givenFromDate = this.getArguments().getLong("selected1");
//        this.fromDateChosenInBasic = givenFromDate;
//        long givenToDate = this.getArguments().getLong("selected2");
        toDate = ((Graph) getActivity()).getToDate();
//        System.out.println("givenFromDate "+ givenFromDate + ", givenToData" + givenToDate);
//        spinnerItem = this.getArguments().getInt("spinnerItem");
//        DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
//        if (givenFromDate != 0L && givenToDate != 0L) {
//            skipTwice = 0;
////            fromDate.setText(dateFormat.format(givenFromDate));
////            toDate.setText(dateFormat.format(givenToDate));
//            selectedFromDate = givenFromDate;
//            spinnerSelection = givenToDate - givenFromDate;
//        }

//        periodPicker  = view.findViewById(R.id.periodPicker);
//        periodPicker.setMinValue(0);
//        periodPicker.setMaxValue(4);
//        periods = getResources().getStringArray(R.array.time_period);
//        periodPicker.setDisplayedValues(periods);
//        periodPicker.setValue(spinnerItem);
//        periodPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
//            spinnerItem = numberPicker.getValue();
//            switch (spinnerItem) {
//                case 0:
//                    spinnerSelection = DateUtils.WEEK_IN_MILLIS; // 1 week
//                    break;
//                case 1:
//                    spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12; // 1 month
//                    break;
//                case 2:
//                    spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12 * 3; // 3 months
//                    break;
//                case 3:
//                    spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12 * 6; // 6 months
//                    break;
//                case 4:
//                    spinnerSelection = DateUtils.YEAR_IN_MILLIS; // 1 year
//                    break;
//            }
//            setFromDate();
//            setSelectedDate(selectedDate);
//        });
        setupPickers(view);
        createCalendar();

//        int selectedNumberOfDays = (int)((givenToDate-givenFromDate) / (1000*60*60*24));
//        int selectedNumberOfDays = (int)((toDate-fromDate) / (1000*60*60*24));
//        daysEditText.setText(String.valueOf(selectedNumberOfDays));

        //   spinnerHandler();
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
//        Bundle bundle = new Bundle();
        calendarFragment = new GraphTimeCalendarFragment();
        // bundle.putLong("min", (calendarObj.getTimeInMillis() - DateUtils.YEAR_IN_MILLIS));
//        bundle.putLong("min", (0));
//        //    bundle.putLong("max", (calendarObj.getTimeInMillis() - spinnerSelection));
//        bundle.putLong("max", (calendarObj.getTimeInMillis() - DateUtils.DAY_IN_MILLIS));
//        if (fromDate != 0L) {
//            bundle.putLong("selected", fromDate);
//        }
//        calendarFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.newTime_calendar_frame, calendarFragment).commit();
    }

    /**
     * Updates the fromDate based on the time period selected in the spinner.
     */
//    public void setFromDate() {
//        if (calendarFragment == null) {
//            //} else {
//            DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
//            //        if (selectedDate != 0L && // Should be always used the selected period from the current date
//            //                (selectedDate + spinnerSelection) < calendarObj.getTimeInMillis()) {
//            //            fromDate.setText(dateFormat.format(selectedDate));
//            //         } else {
////            fromDate.setText(dateFormat
////                    .format(calendarObj.getTimeInMillis() - spinnerSelection));
//            fromDate = calendarObj.getTimeInMillis() - spinnerSelection;
//            //        }
//        }
//    }

    /**
     * Creates the CalendarFragment after moving the selected date to the last possible date. A
     * legal date is any date before now minus the time period selected in the spinner. On close,
     * it sends a bundle to the parent Activity with a start date and a period length.
     */
//    public void refreshCalendar() {
//        if (calendarFragment != null) {
//            // If the calendar was already open, then it needs to be closed so when it opens
//            // again, it is refreshed
//            getChildFragmentManager().beginTransaction().remove(calendarFragment).commit();
//        } else {
//            // The code reaches this point, if the spinner is changed with the calendar closed
//            return;
//        }
//        // Refreshes (re-opens) the calendar fragment
//        Bundle bundle = new Bundle();
//        calendarFragment = new GraphTimeCalendarFragment();
//        bundle.putLong("min", (calendarObj.getTimeInMillis() - DateUtils.YEAR_IN_MILLIS));
//        bundle.putLong("max", (calendarObj.getTimeInMillis() - spinnerSelection));
//
//        if (fromDate > (calendarObj.getTimeInMillis() - spinnerSelection)) {
//            // If the selected date is closer to 'today' when the spinner says it should not be
//            fromDate = calendarObj.getTimeInMillis() - spinnerSelection;
//            DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
////            fromDate.setText(dateFormat.format(selectedDate));
//        } else {
//            // If it is not closer to 'today' then send where it is at so the calendar fragment
//            // can show it.
//            bundle.putLong("selected", fromDate);
//        }
//        calendarFragment.setArguments(bundle);
//        getChildFragmentManager().beginTransaction().replace(R.id.newTime_calendar_frame,
//                calendarFragment).commit();
//    }

    /**
     * Handles input from the calendar TextView and the time selection Spinner.
     * @param v
     */
    @Override
    public void onClick(View v) {
//        if (v == fromDate) {
//            // User wants to see calendar to change date
//            if (calendarFragment != null) {
//                // If the user wants to close the shown calendar
//                getChildFragmentManager().beginTransaction().remove(calendarFragment).commit();
//                calendarFragment = null;
//            } else {
//                // If the user wants to open the calendar
//                Bundle bundle = new Bundle();
//                calendarFragment = new GraphTimeCalendarFragment();
//                bundle.putLong("min", (calendarObj.getTimeInMillis() - DateUtils.YEAR_IN_MILLIS));
//            //    bundle.putLong("max", (calendarObj.getTimeInMillis() - spinnerSelection));
//                bundle.putLong("max", (calendarObj.getTimeInMillis() - DateUtils.DAY_IN_MILLIS));
//                if (selectedDate != 0L) {
//                    bundle.putLong("selected", selectedDate);
//                }
//                calendarFragment.setArguments(bundle);
//                getChildFragmentManager().beginTransaction()
//                        .replace(R.id.newTime_calendar_frame, calendarFragment).commit();
//            }
//        } else
            if (v == viewPeriod) {
            // User wants to close fragment and see the updated time period
//            ((Graph) getActivity())
//                    .setPeriod(selectedDate, (selectedDate + spinnerSelection), spinnerItem);
//            ((Graph) getActivity()).showWithNewTimeDelta(new Timestamp(selectedDate),
//                    new Timestamp(selectedDate + spinnerSelection));
                 calculateToDate();
            ((Graph) getActivity())
                    .setPeriod(fromDate,toDate);
         //           .setPeriod(fromDate, (fromDate + daysEntered*DateUtils.DAY_IN_MILLIS));
            ((Graph) getActivity()).showWithNewTimeDelta(new Timestamp(fromDate),
                    new Timestamp(toDate));
            this.dismiss();
        } else if (v == toBasicFragmentButton) {
//            Bundle bundle = new Bundle();
//            bundle.putBoolean("isFromGraph", false);
//            bundle.putInt("hiveID", hiveid);
//            ConfigurationFragment fragment = new ConfigurationFragment();
//            fragment.setArguments(bundle);
//            fragment.show(getFragmentManager(), "configurationDialog");
        }
//        else if (v == resetButton) {
//            spinnerSelection = 0;
//            selectedFromDate = calendarObj.getTimeInMillis() - DateUtils.WEEK_IN_MILLIS;
//            spinnerItem = 0;
//            skipTwice = 3;
//            // spinnerHandler();
//        }
    }

    private void calculateToDate() {
//        daysEntered = Integer.parseInt(daysEditText.getText().toString());
        daysEntered = dayPicker1.getValue() + dayPicker10.getValue()*10 + dayPicker100.getValue()*100;
        long insertedToDate = fromDate + daysEntered*DateUtils.DAY_IN_MILLIS;
        toDate = Math.min(insertedToDate, System.currentTimeMillis());
    }

    /**
     * Creates an ArrayAdapter for the spinner, to register what is selected in the spinner.
     * @pre A new date is selected by the user.
     * @post fromDate is updated, and view is updated with the new date.
     */
//    public void spinnerHandler() {
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
//                R.array.time_period, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (skipTwice < 2) {
//                    if (spinnerItem == 0) {
//                        skipTwice = 3;
//                    }
//                    spinner.setSelection(spinnerItem);
//                    skipTwice++;
//                    return;
//                }
//                spinnerItem = position;
//                switch (spinnerItem) {
//                    case 0:
//                        spinnerSelection = DateUtils.WEEK_IN_MILLIS; // 1 week
//                        break;
//                    case 1:
//                        spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12; // 1 month
//                        break;
//                    case 2:
//                        spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12 * 3; // 3 months
//                        break;
//                    case 3:
//                        spinnerSelection = DateUtils.YEAR_IN_MILLIS / 12 * 6; // 6 months
//                        break;
//                    case 4:
//                        spinnerSelection = DateUtils.YEAR_IN_MILLIS; // 1 year
//                        break;
//                }
//                refreshCalendar();
//
//                setFromDate();
//                setSelectedDate(selectedDate);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });
//    }

    /**
     * Set by the calendar fragment, and when the spinner changes. Updates the fromDate and toDate
     * according to period and start date.
     * @param  fromDate
     */
    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
//        DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
//        fromDate.setText(dateFormat.format(selectedDate));
//        toDate.setText(dateFormat.format(selectedDate + spinnerSelection));
    }
}
