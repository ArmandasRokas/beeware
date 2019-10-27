package dk.dtu.group22.beeware.data.repositories.repoImpl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.sql.Timestamp;
import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveRepository;

public class HiveRepoHiveToolImpl implements HiveRepository {
    @Override
    public Hive getHive(Hive hive, Timestamp sinceTime, Timestamp untilTime) {
        /**
         * It gives RequestTimeout exception, if there is requesting more
         * than two months data. The solution could be to allow see a graph for instance for every month,
         * not for a whole year. Buttons. This month, last month or something similar.
         * It solves the problem when the graph is zoomed out.
         */

        String sinceDate = sinceTime.toString().split(" ")[0];
        String untilDate = untilTime.toString().split(" ")[0];

        // Calculates number of days
        long milliseconds = untilTime.getTime() - sinceTime.getTime();
        int numOfDays = (int) (milliseconds / (1000*60*60*24));

        System.out.println("since: " + sinceDate + " until: " + untilDate);

        Document doc = null;
        try {
            doc = Jsoup.connect("http://hivetool.net/db/hive_graph706.pl?chart=Temperature&new_hive_id="+
                    hive.getId()+"&start_time=" +
                    sinceDate+ "+23%3A59%3A59&end_time=" +
                    untilDate+ "+23%3A59%3A59&hive_id="+hive.getId()+"&number_of_days=" +
                    numOfDays+ "&last_max_dwdt_lbs_per_hour=30&weight_filter=Raw&max_dwdt_lbs_per_hour=&days=&begin=&end=&units=Metric&undefined=Skip&download_data=Download&download_file_format=csv").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements elements = doc.getElementsByTag("div");
        Element e = elements.get(2);
        String[] lines = e.wholeText().split("\n");

        for(int i = 2; i<lines.length; i++){
            String[] raw_data = lines[i].split("(,|\\s)");
            if(raw_data.length < 17){
                break;
            }
            // TODO implement creation of a hive object using raw_data
            System.out.println(raw_data[0] + " " + raw_data[1] + " " + raw_data[2] + " " + raw_data[3]);

        }
        return null;
    }
}
