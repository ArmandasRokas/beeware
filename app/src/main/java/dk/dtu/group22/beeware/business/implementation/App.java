package dk.dtu.group22.beeware.business.implementation;

import android.app.Application;
import android.content.SharedPreferences;

import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dk.dtu.group22.beeware.R;

public class App extends Application {

    SharedPreferences sharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();
        //Initializes language
        Lingver.init(this, "en");
    }
    public void onResumeHelper(){
    }
}

