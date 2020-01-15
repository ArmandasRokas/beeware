package dk.dtu.group22.beeware.presentation;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;

public class ConfigurationFragment extends DialogFragment implements View.OnClickListener {

    private TextView hiveNameTV, weightIndicatorTV, tempIndicatorTV, saveButton;
    private EditText weightIndicatorNum, tempIndicatorNum;
    private GraphViewModel listener;
    private Logic logic = Logic.getSingleton();

    public ConfigurationFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_configuration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        saveButton = view.findViewById(R.id.config_save);
        saveButton.setOnClickListener(this);

        hiveNameTV = view.findViewById(R.id.hiveNameTV);

        weightIndicatorTV = view.findViewById(R.id.weightIndicatorTV);
        tempIndicatorTV = view.findViewById(R.id.tempIndicatorTV);

        weightIndicatorNum = view.findViewById(R.id.weightIndicatorNum);
        tempIndicatorNum = view.findViewById(R.id.tempIndicatorNumber);

        //weightIndicatorNum.setInputType(InputType.TYPE_NULL);
        //tempIndicatorNum.setInputType(InputType.TYPE);

        listener = ((GraphActivity) getActivity()).getGraphViewModel();

        hiveNameTV.setText(listener.getHive().getName());

        weightIndicatorTV.setText("Weight (kg)");
        tempIndicatorTV.setText("Temperature (*C)");



        weightIndicatorNum.setText(Integer.toString(listener.getHive().getWeightIndicator()));
        tempIndicatorNum.setText(Integer.toString(listener.getHive().getTempIndicator()));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        listener.getHive().setWeightIndicator(Integer.parseInt(weightIndicatorNum.getText().toString()));
        listener.getHive().setTempIndicator(Integer.parseInt(tempIndicatorNum.getText().toString()));

        System.out.println( "weight indicator : " + listener.getHive().getWeightIndicator() );
        System.out.println( "temp indicator : " + listener.getHive().getTempIndicator() );

        if(listener.getHive().getHasBeenConfigured() == false){
            listener.getHive().setHasBeenConfigured(true);
        }
    }

    @Override
    public void onClick(View view) {
        this.dismiss();
    }
}
