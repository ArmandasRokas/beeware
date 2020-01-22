package dk.dtu.group22.beeware.dal.dao.implementation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.dal.dao.interfaces.ISubscription;

public class SubscriptionHivetool implements ISubscription {

    @Override
    public List<NameIdPair> getHivesToSubscribe() {
        Document doc = null;
        //creates a document from the website from which we scrape data.
        try {
            doc = Jsoup.connect("http://hivetool.net/hive_data.shtml").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (doc == null) {
            throw new UnableToFetchData("SubscriptionHivetool class error.\nCheck device internet connection.");
        }

        Element currentTableRow = doc.getElementById("green"); // Selects the first TABLE ROW <tr> to read from.
        ArrayList<NameIdPair> hivesToSub = new ArrayList<>();

        while (currentTableRow != null) {

            // Get name
            String hiveName = currentTableRow.select("td").get(0).text();

            // The id of the hive is parsed from a Link
            Elements links = currentTableRow.getElementsByTag("a");
            String[] arrOfStr = links.get(0).attr("href").split("=", 2);
            int id = Integer.valueOf(arrOfStr[1]);

            // Get location
            String loc = currentTableRow.select("td").get(2).text();

            // Get active / inactive
            boolean active = false;
            if (currentTableRow.id().equals("green")) {
                active = true;
            }

            // Create a named pair
            NameIdPair tmp = new NameIdPair(hiveName, id, active, loc);
            currentTableRow = currentTableRow.nextElementSibling();
            hivesToSub.add(tmp);
        }
        return hivesToSub;
    }

}
