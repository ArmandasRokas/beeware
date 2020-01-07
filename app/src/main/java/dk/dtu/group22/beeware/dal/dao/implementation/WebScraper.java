package dk.dtu.group22.beeware.dal.dao.implementation;

import androidx.core.util.Pair;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.dtu.group22.beeware.dal.dto.Measurement;

public class WebScraper {
    final int everyNth = 3; // Keep every everyNth measurement (extractDataFromCsv).

    public Pair<List<Measurement>, String> getHiveMeasurements(int id, Timestamp sinceTime, Timestamp untilTime) {
        /**
         * It gives RequestTimeout exception, if there is requesting more
         * than two months data. The solution could be to allow see a graph for instance for every month,
         * not for a whole year. Buttons. This month, last month or something similar.
         * It solves the problem when the graph is zoomed out.
         */
        Pair<String[], String> tmp = getDataLines(sinceTime, untilTime, id);
        String[] lines = tmp.first;
        String name = tmp.second;

        return new Pair<List<Measurement>, String>(extractDataFromCSVLines(lines), name);
    }


    /**
     * @param lines an array of lines, where each indice is a CSV line.
     * @return a list of measurements based on lines
     */
    private List<Measurement> extractDataFromCSVLines(String[] lines) {
        final int timestampIndex = 0;
        final int weightIndex = 1;
        final int tempIndex = 2;
        final int humidityIndex = 3; // inside
        final int illuminanceIndex = 6;

        List<Measurement> data_measure = new ArrayList<>();


        for (int i = 2; i < lines.length; i++) {
            if (i % everyNth == 0) {
                String[] raw_data = lines[i].split(",");

                // Handles incomplete data lines
                if (raw_data.length < 16) {
                    continue;
                }


                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Timestamp timestamp = new Timestamp(0);
                try {
                    Date date = dateFormat.parse(raw_data[timestampIndex]);
                    timestamp = new Timestamp(date.getTime());
                } catch (ParseException pe) {
                    pe.printStackTrace();
                    // If timestamp is nonsense, go to next data
                    continue;
                }

                Double weightKg = parseToDoubleOrNeg(raw_data[weightIndex]);
                Double tempC = parseToDoubleOrNeg(raw_data[tempIndex]);
                Double humidity = parseToDoubleOrNeg(raw_data[humidityIndex]);
                Double illuminance = parseToDoubleOrNeg(raw_data[illuminanceIndex]);

                data_measure.add(new Measurement(timestamp, weightKg, tempC, humidity, illuminance));
            }
        }
        return data_measure;
    }

    /**
     * @param sinceTime
     * @param untilTime
     * @param hiveID
     * @return A string array, where each indice is a CSV line, and a name of the hive as displayed by HiveTool
     */
    private Pair<String[], String> getDataLines(Timestamp sinceTime, Timestamp untilTime, int hiveID) {
        // Calculates number of days
        double milliseconds = (untilTime.getTime() - sinceTime.getTime()) / 1000;
        //double numOfDays = (int) (milliseconds / (1000*60*60*24)) + 1;
        double numOfDays = (milliseconds * 1000 / (1000 * 60 * 60 * 24));
        String sinceStr = sinceTime.toString().split(" ")[0];
        String untilStr = untilTime.toString().split(" ")[0];
        String untilHours = untilTime.toString().split(" ")[1].split(":")[0];
        String untilMins = untilTime.toString().split(" ")[1].split(":")[1];
        Document doc = null;
        try {
            doc = Jsoup.connect("http://hivetool.net/db/hive_graph706.pl?chart=Temperature&new_hive_id=" +
                    hiveID + "&start_time=" +
                    sinceStr + "+23%3A59%3A59&end_time=" +
                    untilStr + "+" + untilHours + "%3A" + untilMins + "%3A59&hive_id=" + hiveID + "&number_of_days=" +
                    numOfDays + "&last_max_dwdt_lbs_per_hour=30&weight_filter=Raw&max_dwdt_lbs_per_hour=&days=&begin=&end=&units=Metric&undefined=Skip&download_data=Download&download_file_format=csv")
                    .timeout(100 * 1000).maxBodySize(4000000).get();
        } catch (UnknownHostException u) {
            u.printStackTrace();
        } catch (HttpStatusException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements nameElement = doc.getElementsByTag("title");
        String name = nameElement.get(0).wholeText().split(": ")[1];

        Elements elements = doc.getElementsByTag("div");
        Element e = elements.get(2);
        String[] lines = e.wholeText().split("\n");


        return new Pair<String[], String>(lines, name);
    }


    /**
     * @param string
     * @return Either a parsed double value, or -1.0, as a sentinel for data in the CSV that is ill-formatted
     */
    Double parseToDoubleOrNeg(String string) {
        try {
            return Double.parseDouble(string);
        } catch (Exception e) {
            return -1.0;
        }
    }
}
