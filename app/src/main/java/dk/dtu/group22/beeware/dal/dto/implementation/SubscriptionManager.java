package dk.dtu.group22.beeware.dal.dto.implementation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SubscriptionManager {

    File file = new File("subscriptions.txt");

    public void saveSubscription(int id) throws IOException {
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
    }

    public ArrayList<Integer> getSubscriptions() throws IOException {

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
    }

    public void deleteSubscription(int id) {

    }

}
