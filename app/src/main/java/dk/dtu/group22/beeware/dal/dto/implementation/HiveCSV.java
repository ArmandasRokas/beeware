package dk.dtu.group22.beeware.dal.dto.implementation;

import java.io.File;
import java.io.FileNotFoundException;
//import java.sql.Date;
import java.util.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import dk.dtu.group22.beeware.dal.dao.Hive;
import dk.dtu.group22.beeware.dal.dao.Measurement;
import dk.dtu.group22.beeware.dal.dto.interfaces.IHive;

public class HiveCSV implements IHive {
    int id_counter;
    //Mapping from hive_id to its measurements
    Map<Integer, Hive> idToHive;

    HiveCSV() {
        id_counter = 0;
        idToHive = new HashMap<Integer, Hive>();
    }

    Hive getMeasurements(int id) {
        return idToHive.get(id);
    }

    /**
     * @param file
     * @return a Hive instance, which is the unpacked value of the given CSV-filename
     */
    Hive extractHiveFromHiveToolCSV(String file) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final int timestampIndex = 0;
        final int weightIndex = 1;
        final int tempIndex = 8;
        final int humidityIndex = 9;
        final int illuminanceIndex = 6;

        List<Measurement> data_measure = new ArrayList<>();

        //Remove the initial line of categories
        scanner.nextLine();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            line.replaceAll("\\s", "");
            String[] raw_data = line.split(",");

            //Example Hive tool entry: 2019-09-25 00:00:01
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            Timestamp timestamp = new Timestamp(0);
            try {
                Date date = dateFormat.parse(raw_data[timestampIndex]);
                timestamp = new Timestamp(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Double weightLbs = parseToDoubleOrNeg(raw_data[weightIndex]);
            Double weightKg = parseToDoubleOrNeg(raw_data[weightIndex]) * 0.45359237;

            Double tempF = parseToDoubleOrNeg(raw_data[tempIndex]);
            Double tempC = (tempF - 32.0) * 5 / 9;

            Double humidity = parseToDoubleOrNeg(raw_data[humidityIndex]);

            Double illuminance = parseToDoubleOrNeg(raw_data[illuminanceIndex]);

            data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));
        }
        Hive hive = new Hive();
        hive.setMeasurements(data_measure);
        return hive;
    }

    /**
     * @param string
     * @return either a double, or a symbolic value (-1),
     * which indicate that the value trying to be parsed was incorrectly formatted or non-existing.
     */
    Double parseToDoubleOrNeg(String string) {
        try {
            return Double.parseDouble(string);
        } catch (Exception e) {
            return -1.0;
        }
    }

    @Override
    public Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        return null;
    }
}

