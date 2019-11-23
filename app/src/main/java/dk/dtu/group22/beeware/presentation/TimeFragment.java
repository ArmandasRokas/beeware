package dk.dtu.group22.beeware.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import dk.dtu.group22.beeware.R;

public class TimeFragment extends Fragment {
    private Spinner spinner;
    private DatePicker datePicker;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        spinner = getActivity().findViewById(R.id.spinner1);
        datePicker = getActivity().findViewById(R.id.datePicker);

        // Setup listeners

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time, container, false);
    }
}
