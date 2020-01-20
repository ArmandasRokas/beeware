package dk.dtu.group22.beeware.presentation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class ConfigPromptFragment extends DialogFragment implements View.OnClickListener {
    private TextView promptTV, waitBtn, nowBtn;
    private List<Hive> hives;
    private Logic logic = Logic.getSingleton();
    private boolean configureNow = false;

    public ConfigPromptFragment() {
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
        return inflater.inflate(R.layout.fragment_config_prompt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        promptTV = view.findViewById(R.id.promptMessageTV);

        waitBtn = view.findViewById(R.id.defaultsButton);
        nowBtn = view.findViewById(R.id.configureButton);

        waitBtn.setOnClickListener(this);
        nowBtn.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == waitBtn) {

            this.dismiss();
        } else if (v == nowBtn) {
            ((Overview) getActivity()).setConfigureNow(true);
            configureNow = true;
            this.dismiss();

        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (configureNow) {
            for (Hive hive : hives) {
                if (!hive.getHasBeenConfigured()) {
                    ConfigOnsubFragment oscf = new ConfigOnsubFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("ID", hive.getId());
                    oscf.setArguments(bundle);
                    oscf.show(getFragmentManager(), "configurationDialog");
                }
            }
        }
        for (Hive hive : hives) {
            logic.setIsConfigured(hive.getId(), true);
        }
        ((Overview) getActivity()).setConfigureNow(false);
        configureNow = false;

    }

}
