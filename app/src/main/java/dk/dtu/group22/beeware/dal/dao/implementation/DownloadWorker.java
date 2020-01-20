package dk.dtu.group22.beeware.dal.dao.implementation;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import dk.dtu.group22.beeware.business.implementation.Logic;
import dk.dtu.group22.beeware.dal.dto.Hive;

public class DownloadWorker extends Worker {

    public DownloadWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        // Do the work here--in this case, upload the images.

        Logic logic = Logic.getSingleton();

        String notificationDetails = "";

        System.out.println("Worker.doWork called. Notification sent!");

        List<Hive> hives = logic.getSubscribedHives(2);

        for (Hive hive : hives) {
            logic.calculateHiveStatus(hive);

            List<Hive.StatusIntrospection> statuses = hive.getStatusIntrospection();

            for (Hive.StatusIntrospection status : statuses) {

                if (status.getStatus() == Hive.Status.DANGER) {

                    notificationDetails += "Your hive " + hive.getName() + " is ";

                    if (status.getReasoning() == Hive.DataAnalysis.CASE_CRITICAL_THRESHOLD) {

                        notificationDetails += "having trouble with ";

                        if (status.getVariable() == Hive.Variables.WEIGHT) {
                            notificationDetails += "weight, reaching a critical threshold.\n\n";
                        } else {
                            notificationDetails += "temperature, reaching a critical threshold.\n\n";
                        }
                    } else if (status.getReasoning() == Hive.DataAnalysis.CASE_SUDDEN_CHANGE) {

                        notificationDetails += "experiencing a sudden change in weight.\n\n";

                    }

                }

            }

            if (!notificationDetails.equals("")) {
                logic.createNotification(notificationDetails);
            }

        }

        // Indicate whether the task finished successfully with the Result
        return Result.success();
    }

}
