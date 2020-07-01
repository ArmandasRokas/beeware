package dk.dtu.group22.beeware.business.implementation;

import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        String pickedLang = sharedPreferences.getString("default", getCurrentLocale().toString().split("_")[0]);
        System.out.println("###### " + pickedLang);
        // Language changes depending on user settings
        if (langIsSupported(pickedLang)) {
            Lingver.getInstance().setLocale(this, pickedLang);
        } else {
            Lingver.getInstance().setLocale(this, "en");
        }

        WorkManager.getInstance(getApplicationContext()).cancelAllWork();

    }

    private Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
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
