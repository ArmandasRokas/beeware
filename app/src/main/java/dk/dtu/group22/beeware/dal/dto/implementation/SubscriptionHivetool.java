package dk.dtu.group22.beeware.dal.dto.implementation;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.dal.dto.interfaces.ISubscription;
import dk.dtu.group22.beeware.dal.dto.interfaces.NameIdPair;

public class SubscriptionHivetool implements ISubscription {

    @Override
    public List<NameIdPair> getHivesToSubscribe() {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://hivetool.net/hive_data.shtml").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(doc == null){
            throw new UnableToFetchData("Doc error. Unable to fetch data");
        }

        Element content = doc.getElementById("green");
        //   System.out.println(content.toString());
        ArrayList<NameIdPair> hivesToSub = new ArrayList<NameIdPair>();

        int skip = 0;
        while(content != null){
            Elements links = content.getElementsByTag("a");
            String[] arrOfStr = links.get(0).attr("href").split("=", 2);
            NameIdPair tmp = new NameIdPair(links.get(0).text(), Integer.valueOf(arrOfStr[1]));
            content = content.nextElementSibling();
            hivesToSub.add(tmp);
        }

        return hivesToSub;
    }
}

