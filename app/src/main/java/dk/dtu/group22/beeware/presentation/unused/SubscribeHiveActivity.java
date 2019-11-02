package dk.dtu.group22.beeware.presentation.unused;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.stream.Collectors;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.business.interfaces.ILogic;
import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.User;

public class SubscribeHiveActivity extends AppCompatActivity {
    final ILogic hiveBusiness = new Logic();


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.unused_subscribe_hive);
        final EditText idET = findViewById(R.id.hiveIdET);

        updateSubscribedHives();
        Button confirmBtn = findViewById(R.id.confirmHiveIdSubscribeBtn);
        confirmBtn.setOnClickListener(
                new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        // TODO: Fix Hardcoded user. Armandas
                        User user = new User();
                        user.setId(1);
                        Hive hive = new Hive();
                        String hiveId = idET.getText().toString();
                        if (!(hiveId.equals(""))) {
                            hive.setId(Integer.valueOf(hiveId));
                            hiveBusiness.subscribeHive(user, hive);
                            updateSubscribedHives();
                        }
                    }
                }
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateSubscribedHives(){
        TextView subscribedHivesTV = findViewById(R.id.subscribedHivesArrayTV);
        User user = new User();
        user.setId(1);
        List<Hive> hives = hiveBusiness.getHives(user, 2);
        System.out.println(hives.toString());
        subscribedHivesTV.setText(hives.stream().map(x -> x.getId()).collect(Collectors.toList()).toString());
    }
}