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

        if(doc == null){
            throw new UnableToFetchData("SubscriptionHivetool class error.\nCheck device internet connection.");
        }

        Element content = doc.getElementById("green"); //Selects the first table to read from.
        //   System.out.println(table.toString());
        ArrayList<NameIdPair> hivesToSub = new ArrayList<>();


        ArrayList<String> locations = new ArrayList<>();

        Elements table = doc.select("table");
        Elements rows = table.select("tr");
        //Gets locations from HTML doc
        for (int i = 4; i < rows.size(); i++) { //first row is the column names, so it is skipped
            Element row = rows.get(i);
            Elements cols = row.select("td");

                locations.add(cols.get(2).text());
        }
        int counter = 0;
        while(content != null){
            Elements links = content.getElementsByTag("a");
            boolean active;
            if(content.id().equals("green")){
                active=true;
            } else {
                active=false;
            }

            String[] arrOfStr = links.get(0).attr("href").split("=", 2);
            NameIdPair tmp = new NameIdPair(links.get(0).text(), Integer.valueOf(arrOfStr[1]),active, locations.get(counter));
            content = content.nextElementSibling();
            hivesToSub.add(tmp);
            counter++;
            tmp.toString();
        }

        return hivesToSub;
    }
}

