package dk.dtu.group22.beeware.presentation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class ConfigurationFragment extends DialogFragment implements View.OnClickListener {
    private TextView hiveNameTV, weightIndicatorTV, tempIndicatorTV, saveButton, configTV, configInfo;
    private EditText weightIndicatorNum, tempIndicatorNum;
    private Logic logic = Logic.getSingleton();
    private Hive hive;
    private boolean cameFromGraphAct;

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
        super.onViewCreated(view, savedInstanceState);

        saveButton = view.findViewById(R.id.config_save);
        saveButton.setOnClickListener(this);

        hiveNameTV = view.findViewById(R.id.hiveNameTV);

        weightIndicatorTV = view.findViewById(R.id.weightIndicatorTV);
        tempIndicatorTV = view.findViewById(R.id.tempIndicatorTV);

        weightIndicatorNum = view.findViewById(R.id.weightIndicatorNum);
        tempIndicatorNum = view.findViewById(R.id.tempIndicatorNumber);

        configTV = view.findViewById(R.id.config_tv);
        configInfo = view.findViewById(R.id.config_info);

        cameFromGraphAct = getArguments().getBoolean("isFromGraph", false);
        if (cameFromGraphAct) {
            configTV.setText(getString(R.string.FromGraphConfigTitle));
            configInfo.setText(getString(R.string.FromGraphConfigInfo));
        }
        int hiveid = getArguments().getInt("hiveID", 0);
        if (hiveid != 0) {
            hive = logic.getHive(hiveid);
        }

        hiveNameTV.setText(hive.getName());

        weightIndicatorNum.setText(Integer.toString(hive.getWeightIndicator()));
        tempIndicatorNum.setText(Integer.toString(hive.getTempIndicator()));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (weightIndicatorNum.getText().toString().isEmpty()) {
            weightIndicatorNum.setText(Integer.toString(hive.getWeightIndicator()));
        }
        if (tempIndicatorNum.getText().toString().isEmpty()) {
            tempIndicatorNum.setText(Integer.toString(hive.getTempIndicator()));
        }

        hive.setWeightIndicator(Integer.parseInt(weightIndicatorNum.getText().toString()));
        hive.setTempIndicator(Integer.parseInt(tempIndicatorNum.getText().toString()));

        System.out.println("weight indicator : " + hive.getWeightIndicator());
        System.out.println("temp indicator : " + hive.getTempIndicator());

        if (hive.getHasBeenConfigured() == false) {
            logic.setIsConfigured(hive.getId(), true);
        }

    }

    @Override
    public void onClick(View view) {
        this.dismiss();
    }

}
