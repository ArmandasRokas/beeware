package dk.dtu.group22.beeware.business.implementation;

import android.app.Application;

import com.yariksoffice.lingver.Lingver;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Initializes language
        Lingver.init(this, "en");
    }

}

