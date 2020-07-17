package dk.dtu.group22.beeware.presentation;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView hiveNameTV, weightIndicatorTV, tempIndicatorTV, saveButton,cancelButton, topicThresh; // configTV
    private ImageView explainTresh;
   // private EditText tempIndicatorNum;
    private NumberPicker weightNumberPicker, tempNumberPicker ;
    private Logic logic = Logic.getSingleton();
    private Hive hive;
    private boolean cameFromGraphAct;
    private ProgressBar progressBar;
    private AsyncTask asyncTask;
    private Toast explainThreshToast, failedToGetHive;

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
        explainThreshToast = Toast.makeText(getContext(), R.string.configInfo, Toast.LENGTH_LONG);
        View.OnClickListener toastListener = vToast -> {
            explainThreshToast.setGravity(Gravity.CENTER, 0, 0);
            explainThreshToast.show();
        };
     //   super.onViewCreated(view, savedInstanceState);
        saveButton = view.findViewById(R.id.config_save);
        saveButton.setOnClickListener(this);
        cancelButton = view.findViewById(R.id.config_cancel);
        cancelButton.setOnClickListener(view1 ->{
            onDismiss(getDialog());
        } );

        hiveNameTV = view.findViewById(R.id.hiveNameTV);

        weightIndicatorTV = view.findViewById(R.id.weightIndicatorTV);
        tempIndicatorTV = view.findViewById(R.id.tempIndicatorTV);

        weightNumberPicker = view.findViewById(R.id.weightNumberPicker);
        weightNumberPicker.setMinValue(0);
        weightNumberPicker.setMaxValue(199);
      //  tempIndicatorNum = view.findViewById(R.id.tempIndicatorNumber);
        tempNumberPicker = view.findViewById(R.id.tempNumberPicker);
        tempNumberPicker.setMinValue(0);
        tempNumberPicker.setMaxValue(50);

        //configTV = view.findViewById(R.id.config_tv);
        topicThresh = view.findViewById(R.id.configTopicThresh);
        topicThresh.setOnClickListener(toastListener);
        explainTresh = view.findViewById(R.id.explainThresh);
        explainTresh.setOnClickListener(toastListener);
        progressBar = view.findViewById(R.id.progressBarConfigurationFrag);
        failedToGetHive = Toast.makeText(getContext(), R.string.FailedToGetHive, Toast.LENGTH_SHORT);
        failedToGetHive.setGravity(Gravity.CENTER, 0, 00);
        failedToGetHive.getView().setBackgroundResource(R.drawable.toast_error);
        TextView v = (TextView) failedToGetHive.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.WHITE);
        int hiveid = getArguments().getInt("hiveID", 0);
        asyncTask = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
             //   getDialog().setCanceledOnTouchOutside(false);
                topicThresh.setVisibility(View.INVISIBLE);
                explainTresh.setVisibility(View.INVISIBLE);
                weightNumberPicker.setVisibility(View.INVISIBLE);
                tempNumberPicker.setVisibility(View.INVISIBLE);
                weightIndicatorTV.setVisibility(View.INVISIBLE);
                tempIndicatorTV.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                saveButton.setEnabled(false);
                saveButton.setTextColor(getResources().getColor(R.color.BEE_graphWeight));
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                long now = System.currentTimeMillis();
                long since = now - (86400000 * 2);
                hive = logic.getCachedHive(hiveid);
                if(hive==null) {
                    try {
                        hive = logic.getHiveNetwork(hiveid, new Timestamp(since), new Timestamp(now));
                    } catch (Exception e) {
                        failedToGetHive.show();
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                progressBar.setVisibility(View.INVISIBLE);
                if(hive != null){
                    topicThresh.setVisibility(View.VISIBLE);
                    explainTresh.setVisibility(View.VISIBLE);
                    weightNumberPicker.setVisibility(View.VISIBLE);
                    tempNumberPicker.setVisibility(View.VISIBLE);
                    weightIndicatorTV.setVisibility(View.VISIBLE);
                    tempIndicatorTV.setVisibility(View.VISIBLE);
                    saveButton.setEnabled(true);
                    saveButton.setTextColor(getResources().getColor(R.color.app_theme));
                    hiveNameTV.setText(hive.getName());
                    weightNumberPicker.setValue(hive.getWeightIndicator());
                    tempNumberPicker.setValue(hive.getTempIndicator());
                } else {
                    hiveNameTV.setText("");
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                onDismiss(getDialog());
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.start();
                }
                // some junk below.
             /*   cameFromGraphAct = getArguments().getBoolean("isFromGraph", false);
                if (cameFromGraphAct) {
                    configTV.setText(getString(R.string.FromGraphConfigTitle));
                    configInfo.setText(getString(R.string.FromGraphConfigInfo));
                }*/
                /*if (hiveid != 0) {
                    hive = logic.getCachedHive(hiveid);
                }*/
//                hiveNameTV.setText(hive.getName());
//                weightIndicatorNum.setText(Integer.toString(hive.getWeightIndicator()));
//                weightNumberPicker.setValue(hive.getWeightIndicator());
//                tempNumberPicker.setValue(hive.getTempIndicator());
                //tempIndicatorNum.setText(Integer.toString(hive.getTempIndicator()));
            }
        };
        asyncTask.execute();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        // Do not save any changes
        asyncTask.cancel(true);
        explainThreshToast.cancel();
        failedToGetHive.cancel();
        super.onDismiss(dialog);

    }

    @Override
    public void onClick(View view) {
        // Some junk
//        if (weightIndicatorNum.getText().toString().isEmpty()) {
//            weightIndicatorNum.setText(Integer.toString(hive.getWeightIndicator()));
//        }
//        if (tempIndicatorNum.getText().toString().isEmpty()) {
//            tempIndicatorNum.setText(Integer.toString(hive.getTempIndicator()));
//        }
       // hive.setWeightIndicator(Integer.parseInt(weightIndicatorNum.getText().toString()));
//        hive.setTempIndicator(Integer.parseInt(tempIndicatorNum.getText().toString()));

        // Only when saveButton is clicked, save new values
        hive.setWeightIndicator(weightNumberPicker.getValue());
        hive.setTempIndicator(tempNumberPicker.getValue());

        if (hive.getHasBeenConfigured() == false) {
            logic.setIsConfigured(hive.getId(), true);
        }
        // Close fragment
        onDismiss(getDialog());
    }
}
