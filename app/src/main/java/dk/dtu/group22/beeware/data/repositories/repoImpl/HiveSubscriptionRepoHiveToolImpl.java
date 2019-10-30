package dk.dtu.group22.beeware.data.repositories.repoImpl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dk.dtu.group22.beeware.data.entities.Hive;
import dk.dtu.group22.beeware.data.repositories.interfaceRepo.HiveSubscriptionRepository;

public class HiveSubscriptionRepoHiveToolImpl implements HiveSubscriptionRepository {
    @Override
    public List<Hive> getHivesToSubscribe() {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://hivetool.net/hive_data.shtml").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(doc == null){
            throw  new UnableToFetchData("Error. Unable to fetch data");
        }

        Element content = doc.getElementById("green");
        //   System.out.println(content.toString());
        ArrayList<Hive> hives = new ArrayList<Hive>();

        while(content != null){
            Elements links = content.getElementsByTag("a");
            String[] arrOfStr = links.get(0).attr("href").split("=", 2);
            Hive hive = new Hive();
            hive.setId(Integer.valueOf(arrOfStr[1]));
            hive.setName(links.get(0).text());
            content = content.nextElementSibling();
            hives.add(hive);
        }

        return hives;
    }
}
