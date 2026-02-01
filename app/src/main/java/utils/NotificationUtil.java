package utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.pocketplan.R;

public class NotificationUtil {

    public static void showAlert(Context context, String message) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "budget_alert",
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "budget_alert")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Budget Alert")
                        .setContentText(message)
                        .setAutoCancel(true);

        nm.notify(1, builder.build());
    }
}
