package dk.dtu.group22.beeware.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import dk.dtu.group22.beeware.R;

public class TimeFragment extends DialogFragment {
    private Spinner spinner;
    private DatePicker datePicker;
    private Button timeButton;
    private int option = 0;
    private Timestamp fromDate;
    private Timestamp toDate;
    private GraphActivity listener;

    public TimeFragment() {
        // Required empty public constructor
    }

    public static TimeFragment newInstance() {
        TimeFragment fragment = new TimeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spinner = view.findViewById(R.id.spinnerTimeDelta);
        datePicker = view.findViewById(R.id.datePicker);
        timeButton = view.findViewById(R.id.timeSelectedButton);
        Calendar cal = Calendar.getInstance();
        listener = (GraphActivity) getActivity();

        // Setup listeners
        timeButton.setOnClickListener(v -> {
            cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            fromDate = new Timestamp(cal.getTimeInMillis());

            switch (option) {
                case 1:
                    cal.add(Calendar.DAY_OF_MONTH, 3);
                    break;
                case 2:
                    cal.add(Calendar.WEEK_OF_MONTH, 2);
                    break;
                case 3:
                    cal.add(Calendar.WEEK_OF_MONTH, 4);
                    break;
                case 4:
                    cal.add(Calendar.MONTH, 3);
                    break;
                case 5:
                    cal.add(Calendar.MONTH, 6);
                    break;
                case 6:
                    cal.add(Calendar.YEAR, 1);
                    break;
                default:
                    cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            toDate = new Timestamp(cal.getTimeInMillis());
            if (option > -1) {
                listener.updateTimeDelta(fromDate, toDate);
            }
            this.dismiss();
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                option = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                option = -1; // 2 Week default
            }
        });
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date lastYear = cal.getTime();
        datePicker.setMaxDate(today.getTime());
        datePicker.setMinDate(lastYear.getTime());
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
}
