package dk.dtu.group22.beeware.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import dk.dtu.group22.beeware.R;

public class GraphTimeCalendarFragment extends Fragment {
    private CalendarView calendar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View popup = inflater.inflate(R.layout.fragment_newtime_calendar, container, false);

        calendar = popup.findViewById(R.id.newTime_calendarView);
        calendar.setMinDate(this.getArguments().getLong("min"));
        calendar.setMaxDate(this.getArguments().getLong("max"));
        long selectedDate = this.getArguments().getLong("selected", 0L);
        if (selectedDate != 0L) {
            calendar.setDate(selectedDate);
        } else {
            calendar.setDate(calendar.getMaxDate());
        }

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-y");
                long selectedDate = calendar.getMaxDate();
                try {
                    if (++i1 > 12) {
                        i1 = 1;
                        i2++;
                    }
                    selectedDate = dateFormat.parse(i2 + "-" + i1 + "-" + i).getTime();
                } catch (Exception e) {
                    System.out.println("Parse error in GraphTimeCalendarFragment!");
                }
                ((GraphTimeAdvancedSelectionFrag) getParentFragment()).setSelectedDate(selectedDate);
            }
        });

        return popup;
    }

}
