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

public class GraphPeriodFragment extends DialogFragment {
    private Spinner spinner;
    private DatePicker datePicker;
    private Button viewPeriod;
    private int option = 0;
    private Timestamp fromDate;
    private Timestamp toDate;
    private GraphActivity listener;

    public GraphPeriodFragment() {
        // Required empty public constructor
    }

    public static GraphPeriodFragment newInstance() {
        GraphPeriodFragment fragment = new GraphPeriodFragment();
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
        viewPeriod = view.findViewById(R.id.btn_view_period);
        Calendar cal = Calendar.getInstance();
        listener = (GraphActivity) getActivity();

        // Setup listeners
        viewPeriod.setOnClickListener(v -> {
            cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            fromDate = new Timestamp(cal.getTimeInMillis());

            // Switches on the selected period from the spinner
            switch (option) {
                case 1:
                    cal.add(Calendar.MONTH, 1); // 1 month
                    break;
                case 2:
                    cal.add(Calendar.MONTH, 3); // 3 months
                    break;
                case 3:
                    cal.setTimeInMillis(System.currentTimeMillis());
                    cal.add(Calendar.YEAR, 1); // A year
                    break;
                default:
                    cal.add(Calendar.WEEK_OF_MONTH, 1); // 1 week
            }

            toDate = new Timestamp(cal.getTimeInMillis());
            Timestamp now = new Timestamp(System.currentTimeMillis());

            if (toDate.after(now)) {
                toDate = now;
            }

            // Year ignores date
            if (option == 3) {
                cal.setTimeInMillis(now.getTime());
                cal.add(Calendar.YEAR, -1);
                listener.showWithNewTimeDelta(new Timestamp(cal.getTimeInMillis()), now);
            } else if (option > -1) {
                listener.showWithNewTimeDelta(fromDate, toDate);
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

        // Set the time interval on the datepicker
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date lastYear = cal.getTime();
        datePicker.setMaxDate(today.getTime());
        datePicker.setMinDate(lastYear.getTime());
    }
}
