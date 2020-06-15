package com.example.eventifind;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotificationService {

    private final String CHANNEL_ID = "EventAlert";
    private NotificationManagerCompat notificationManager;
    private Context context;
    private boolean notificationShowed;

    public NotificationService(Context context) {
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
        notificationShowed = false;
        createNotificationChannel();
    }

    public void createNotificationToday(String eventName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pin)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.Event_Today,eventName))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(100,builder.build());
    }

    public void createNotificationTomorrow(String eventName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pin)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.Event_Tomorrow,eventName))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(101,builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event alert";
            String description = "Get notified when an event is close";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotifications(HashMap<String, Event> map, ArrayList<String> joinedEvents) {
        if (notificationShowed) return;
        if (map == null || joinedEvents == null || map.isEmpty() || joinedEvents.isEmpty()) return;
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        for(Map.Entry<String,Event> e : map.entrySet()){
            if(joinedEvents.contains(e.getKey())) {
                Calendar date = Calendar.getInstance();
                date.setTime(e.getValue().getDate());
                if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    createNotificationToday(e.getValue().getName());
                }
                if (date.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
                    createNotificationTomorrow(e.getValue().getName());
                }
            }
        }
        notificationShowed = true;
    }

}
