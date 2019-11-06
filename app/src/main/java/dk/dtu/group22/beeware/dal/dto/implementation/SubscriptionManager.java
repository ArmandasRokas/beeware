package dk.dtu.group22.beeware.dal.dto.implementation;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import dk.dtu.group22.beeware.R;
import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscriptionManager;

public class SubscriptionManager implements ISubscriptionManager {

    private File file;
    private Context ctx;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SubscriptionManager(Context ctx) {
        this.ctx = ctx;
        //sharedPreferences = ctx.getSharedPreferences(, Context.MODE_PRIVATE);
        sharedPreferences = ctx.getSharedPreferences(String.valueOf(R.string.subscription_ids), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        /*
        try {
            file = new File(ctx.getFilesDir(), "subscriptions.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    public void saveSubscription(int id) throws IOException {
        editor.putInt("hive" + id, id);
        editor.commit();
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for(Map.Entry<String, ?> entry : allEntries.entrySet()){
            System.out.println("map values "+  entry.getKey() + ": " + entry.getValue().toString());
        }

        /*
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(id);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
        */
    }

    public Map<String, ?> getSubscriptions() throws IOException {

        //return Map<String, ?> subMap = sharedPreferences.getAll();

        /*
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        ArrayList<Integer> subscriptions = new ArrayList<>();
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();
            while(line != null){
                subscriptions.add(Integer.parseInt(line));
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return subscriptions;
        */
        return null;
    }

    public void deleteSubscription(int id) throws IOException {
    editor.remove("hive" + id);
    }

}
