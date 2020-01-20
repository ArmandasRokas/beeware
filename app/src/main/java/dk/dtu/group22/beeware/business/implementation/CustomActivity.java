package dk.dtu.group22.beeware.business.implementation;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dk.dtu.group22.beeware.dal.dao.implementation.DownloadWorker;

public abstract class CustomActivity extends AppCompatActivity {

    boolean langIsSupported(String lang) {
        List<String> supportedLangs = new ArrayList<>();
        supportedLangs.add("en");
        supportedLangs.add("da");
        return supportedLangs.contains(lang);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("Language", MODE_PRIVATE);

        // Language setup
        String pickedLang = sharedPreferences.getString("default", getResources().getSystem().getConfiguration().getLocales().get(0).toString().split("_")[0]);
        System.out.println("###### " + pickedLang);
        // Language changes depending on user settings
        if (langIsSupported(pickedLang)) {
            Lingver.getInstance().setLocale(this, pickedLang);
        } else {
            Lingver.getInstance().setLocale(this, "en");
        }

        WorkManager.getInstance(getApplicationContext()).cancelAllWork();

    }

    @Override
    protected void onPause() {
        super.onPause();

        PeriodicWorkRequest saveRequest =
                new PeriodicWorkRequest.Builder(DownloadWorker.class, 60, TimeUnit.MINUTES)
                        .setInitialDelay(60, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueue(saveRequest);

    }
}
