package dk.dtu.group22.beeware.presentation;

import android.content.DialogInterface;
import android.content.Intent;
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

public class OnSubscriptionConfigurationFragment extends DialogFragment implements View.OnClickListener {

    private TextView hiveNameTV, weightIndicatorTV, tempIndicatorTV;
    private EditText weightIndicatorNum, tempIndicatorNum;
    private Button doneButton;
    private Logic logic = Logic.getSingleton();
    private OverviewAdapter listener;

    public OnSubscriptionConfigurationFragment() {
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
        return inflater.inflate(R.layout.fragment_on_subscription_configuration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        hiveNameTV = view.findViewById(R.id.hiveNameTV);

        weightIndicatorTV = view.findViewById(R.id.weightIndicatorTV);
        tempIndicatorTV = view.findViewById(R.id.tempIndicatorTV);

        weightIndicatorNum = view.findViewById(R.id.weightIndicatorNum);
        tempIndicatorNum = view.findViewById(R.id.tempIndicatorNumber);

        weightIndicatorNum.setText(Integer.toString(logic.getHive(getArguments().getInt("ID")).getWeightIndicator()));
        tempIndicatorNum.setText(Integer.toString(logic.getHive(getArguments().getInt("ID")).getTempIndicator()));

        doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(this);

        hiveNameTV.setText(logic.getHive(getArguments().getInt("ID")).getName());

        weightIndicatorTV.setText("Weight (kg)");
        tempIndicatorTV.setText("Temperature (*C)");


    }



    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        logic.getHive(getArguments().getInt("ID")).setWeightIndicator(Integer.parseInt(weightIndicatorNum.getText().toString()));
        logic.getHive(getArguments().getInt("ID")).setTempIndicator(Integer.parseInt(tempIndicatorNum.getText().toString()));
        logic.getHive(getArguments().getInt("ID")).setHasBeenConfigured(true);

        logic.calculateHiveStatus(logic.getHive(getArguments().getInt("ID")));

    }

    @Override
    public void onClick(View v) {
        if(v == doneButton){
            this.dismiss();
        }
    }
}
