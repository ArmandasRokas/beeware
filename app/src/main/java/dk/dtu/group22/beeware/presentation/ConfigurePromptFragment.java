package dk.dtu.group22.beeware.presentation;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;


public class ConfigurePromptFragment extends DialogFragment implements View.OnClickListener {

    private TextView promptTV;
    private Button waitBtn, nowBtn;
    private List<Hive> hives;
    private Logic logic = Logic.getSingleton();
    private boolean configureNow = false;

    public ConfigurePromptFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hives = logic.getSubscribedHives(2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_configure_promt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        promptTV = view.findViewById(R.id.promptTV);

        waitBtn = view.findViewById(R.id.waitButton);
        nowBtn = view.findViewById(R.id.configureButton);

        waitBtn.setOnClickListener(this);
        nowBtn.setOnClickListener(this);

    }


    public void onClick(View v) {
        if(v == waitBtn){
            for(Hive hive: hives){
                hive.setHasBeenConfigured(true);
            }
            this.dismiss();
        } else if (v == nowBtn){
            ((Overview) getActivity()).setConfigureNow(true);
            configureNow = true;
            this.dismiss();

        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(configureNow) {
            for (Hive hive : hives) {
                if (!hive.getHasBeenConfigured()) {
                    OnSubConfigurationFragment oscf = new OnSubConfigurationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("ID", hive.getId());
                    oscf.setArguments(bundle);
                    oscf.show(getFragmentManager(), "configurationDialog");
                }
            }
        }
        ((Overview) getActivity()).setConfigureNow(false);
        configureNow = false;

    }
}

