package dk.dtu.group22.beeware.business.implementation;

import android.app.Application;
import android.content.SharedPreferences;

import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.R;

public class App extends Application {

    SharedPreferences sharedPreferences;

    boolean langIsSupported(String lang){
        List<String> supportedLangs = new ArrayList<>();
        supportedLangs.add("en");
        supportedLangs.add("da");
        return supportedLangs.contains(lang);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("Language", MODE_PRIVATE);

        // Language setup
        String defaultLang = "default";
        // TODO: Provide feedback for the user when his locale is not supported, and mention that you switch to en_US
        String defaultLanguage = sharedPreferences.getString(defaultLang, getResources().getConfiguration().getLocales().get(0).toString());
        Lingver.init(this, "en");
        if(langIsSupported(defaultLang)) {
            Lingver.getInstance().setLocale(this, defaultLanguage);
        }
    }
}
