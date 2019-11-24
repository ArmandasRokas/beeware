package dk.dtu.group22.beeware.presentation;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import dk.dtu.group22.beeware.R;

public class TimeFragment extends Fragment {
    private Spinner spinner;
    private DatePicker datePicker;
    private Button timeButton;
    private int option;
    private Timestamp fromDate;
    private Timestamp toDate;

    public TimeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TimeFragment newInstance() {
        TimeFragment fragment = new TimeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        spinner = getActivity().findViewById(R.id.spinnerTimeDelta);
        datePicker = getActivity().findViewById(R.id.datePicker);
        timeButton = getActivity().findViewById(R.id.timeSelectedButton);
        Calendar cal = Calendar.getInstance();

        // Setup listeners
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth();
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

                ((GraphActivity) getActivity()).updateTimeDelta();
            }
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
                option = 3; // 2 Week default
            }
        });
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date lastYear = cal.getTime();
        datePicker.setMaxDate(today.getTime());
        datePicker.setMinDate(lastYear.getTime());


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time, container, false);
    }
}
