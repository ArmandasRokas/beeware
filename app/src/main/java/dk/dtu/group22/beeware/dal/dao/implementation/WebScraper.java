package dk.dtu.group22.beeware.dal.dao.implementation;

import androidx.core.util.Pair;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.dtu.group22.beeware.dal.dto.Measurement;

public class WebScraper {
    final int everyNth = 3; // Keep every everyNth measurement (extractDataFromCsv).

    public Pair<List<Measurement>, String> getHiveMeasurements(int id, Timestamp sinceTime, Timestamp untilTime) throws IOException, NoDataAvailableOnHivetoolException {
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
                    //pe.printStackTrace();
                    System.out.println("Parse Exception.");
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
    private Pair<String[], String> getDataLines(Timestamp sinceTime, Timestamp untilTime, int hiveID) throws IOException, NoDataAvailableOnHivetoolException {
     //   if (!isDataAvailableOnHiveTool(sinceTime, untilTime, hiveID)){
     //       throw new NoDataAvailableOnHivetoolException("Data is not availabe on HiveTool in a selected time interval");
     //   }
        // If there is more than 30 days between requested timeinterval, it is assumed that there is no the data
        // until this time point and stop downloading.
        if(untilTime.getTime() - sinceTime.getTime() > 1000*30*24*60*60L ){
            throw new NoDataAvailableOnHivetoolException("Data is not availabe on HiveTool in a selected time interval");
        }
        //Calculates number of days
        double milliseconds = (untilTime.getTime() - sinceTime.getTime()) / 1000;
        //double numOfDays = (int) (milliseconds / (1000*60*60*24)) + 1;
        double numOfDays = (milliseconds * 1000 / (1000 * 60 * 60 * 24));
        String sinceStr = sinceTime.toString().split(" ")[0];
        String untilStr = untilTime.toString().split(" ")[0];
        String untilHours = untilTime.toString().split(" ")[1].split(":")[0];
        String untilMins = untilTime.toString().split(" ")[1].split(":")[1];
        System.out.println("WebScrapper since: "+ sinceStr + ", until: " + untilStr);
        Document doc = null;
            String url = "http://hivetool.net/db/hive_graph706.pl?chart=Temperature&new_hive_id=" +
                    hiveID + "&start_time=" +
                    sinceStr + "+23%3A59%3A59&end_time=" +
                    untilStr + "+" + untilHours + "%3A" + untilMins + "%3A59&hive_id=" + hiveID + "&number_of_days=" +
                    numOfDays + "&last_max_dwdt_lbs_per_hour=30&weight_filter=Raw&max_dwdt_lbs_per_hour=&days=&begin=&end=&units=Metric&undefined=Skip&download_data=Download&download_file_format=csv";
        System.out.println("Henter URL: "+url);
            doc = Jsoup.connect(url)
                    .timeout(100 * 1000).maxBodySize(4000000).get();
            /* Catch-blokke fjernet af Jacob:
            Send de exceptions der opstår videre til rette modtager i stedet for at æde som og få følgefejl
        } catch (UnknownHostException u) {
            //u.printStackTrace();
            System.out.println("Unknown host.");
        } catch (HttpStatusException e) {
            //e.printStackTrace();
            System.out.println("HTTP status exception.");
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("IO Exception");
        }
             */
            //Check if document contains h1 element with "Sorry, no data for hive...."
//        if(!doc.body().getElementsByTag("h1").isEmpty()){
//            throw new NoDataAvailableOnHivetoolException("Data is not availabe on HiveTool in a selected time interval");
//        }
        Elements nameElement = doc.getElementsByTag("title"); // her kom en nullpointerexception som følgefejl af problemer med at hente data
        String name = nameElement.get(0).wholeText().split(": ")[1];

        Elements elements = doc.getElementsByTag("div");
        Element e = elements.get(2);
        String[] lines = e.wholeText().split("\n");

        return new Pair<String[], String>(lines, name);
    }

    /**
     * Checks if data is available for a given time interval.
     * @param sinceTime
     * @param untilTime no used yet, but should be considered to be checked to in order to push sinceTime more in future
     *                  in case of untilTime is on HiveTool.
     * @param hiveID
     * @return
     * @throws IOException
     */
    /*public boolean isDataAvailableOnHiveTool(Timestamp sinceTime, Timestamp untilTime, int hiveID) throws IOException {

        String sinceStr = sinceTime.toString().split(" ")[0];

        Document doc = null;
        String url = "http://hivetool.net/db/hive_graph706.pl?chart=Temperature&new_hive_id=" +
                hiveID + "&start_time=&end_time=&hive_id=" +
                hiveID + "&number_of_days=&last_max_dwdt_lbs_per_hour=30&weight_filter=Raw&max_dwdt_lbs_per_hour=&days=3&begin=" +
                sinceStr+ "&end=&units=Metric&undefined=Skip&download_data=Download&download_file_format=csv";

        doc = Jsoup.connect(url)
                .timeout(30 * 1000).maxBodySize(4000000).get();

        if(doc.body().getElementsByTag("h1").isEmpty()){
            return true;
        } else{
            //System.out.println(doc.body().getElementsByTag("h1").get(0).text().contains("Sorry, no data"));
            return false;
        }
    }*/

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
