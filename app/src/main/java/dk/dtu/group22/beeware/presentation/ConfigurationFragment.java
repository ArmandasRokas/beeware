package dk.dtu.group22.beeware.presentation;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.sql.Timestamp;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dao.implementation.NoDataAvailableOnHivetoolException;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class ConfigurationFragment extends DialogFragment implements View.OnClickListener {
    private TextView hiveNameTV, weightIndicatorTV, tempIndicatorTV, saveButton, configInfo; // configTV
    private EditText weightIndicatorNum, tempIndicatorNum;
    private Logic logic = Logic.getSingleton();
    private Hive hive;
    private boolean cameFromGraphAct;
    private ProgressBar progressBar;
    private AsyncTask asyncTask;

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
     //   super.onViewCreated(view, savedInstanceState);
        saveButton = view.findViewById(R.id.config_save);
        saveButton.setOnClickListener(this);

        hiveNameTV = view.findViewById(R.id.hiveNameTV);

        weightIndicatorTV = view.findViewById(R.id.weightIndicatorTV);
        tempIndicatorTV = view.findViewById(R.id.tempIndicatorTV);

        weightIndicatorNum = view.findViewById(R.id.weightIndicatorNum);
        tempIndicatorNum = view.findViewById(R.id.tempIndicatorNumber);

        //configTV = view.findViewById(R.id.config_tv);
        configInfo = view.findViewById(R.id.configTopicThresh);
        progressBar = view.findViewById(R.id.progressBarConfigurationFrag);
        int hiveid = getArguments().getInt("hiveID", 0);
        asyncTask = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
             //   getDialog().setCanceledOnTouchOutside(false);
                progressBar.setVisibility(View.VISIBLE);
                saveButton.setEnabled(false);
                saveButton.setTextColor(getResources().getColor(R.color.BEE_graphWeight));
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                long now = System.currentTimeMillis();
                long since = now - (86400000 * 2);
                try {
                    hive = logic.getHiveNetwork(hiveid, new Timestamp(since), new Timestamp(now));
                } catch (NoDataAvailableOnHivetoolException e) {
                    // TODO implement Toast
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO implement Toast
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
             //   getDialog().setCanceledOnTouchOutside(true);
                progressBar.setVisibility(View.INVISIBLE);
                saveButton.setEnabled(true);
                saveButton.setTextColor(getResources().getColor(R.color.app_theme));

             /*   cameFromGraphAct = getArguments().getBoolean("isFromGraph", false);
                if (cameFromGraphAct) {
                    configTV.setText(getString(R.string.FromGraphConfigTitle));
                    configInfo.setText(getString(R.string.FromGraphConfigInfo));
                }*/
                if (hiveid != 0) {
                    hive = logic.getCachedHive(hiveid);
                }
                hiveNameTV.setText(hive.getName());
                weightIndicatorNum.setText(Integer.toString(hive.getWeightIndicator()));
                tempIndicatorNum.setText(Integer.toString(hive.getTempIndicator()));
            }
        };
        asyncTask.execute();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        // Do not save any changes
        asyncTask.cancel(true);
        super.onDismiss(dialog);

    }

    @Override
    public void onClick(View view) {
        // Only when saveButton is clicked, save new values
        if (weightIndicatorNum.getText().toString().isEmpty()) {
            weightIndicatorNum.setText(Integer.toString(hive.getWeightIndicator()));
        }
        if (tempIndicatorNum.getText().toString().isEmpty()) {
            tempIndicatorNum.setText(Integer.toString(hive.getTempIndicator()));
        }

        hive.setWeightIndicator(Integer.parseInt(weightIndicatorNum.getText().toString()));
        hive.setTempIndicator(Integer.parseInt(tempIndicatorNum.getText().toString()));

        if (hive.getHasBeenConfigured() == false) {
            logic.setIsConfigured(hive.getId(), true);
        }
        // Close fragment
        getDialog().dismiss();
    }
}
