package com.example.vaultbirthday;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "RingtonePicker")
public class RingtonePickerPlugin extends Plugin {

    @PluginMethod
    public void pickRingtone(PluginCall call) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose alarm sound");
        String current = call.getString("currentUri");
        if (current != null && !current.isEmpty()) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(current));
        }
        startActivityForResult(call, intent, "handleRingtoneResult");
    }

    @ActivityCallback
    private void handleRingtoneResult(PluginCall call, androidx.activity.result.ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            call.reject("cancelled");
            return;
        }
        Uri uri = (Uri) result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String title = "Selected sound";
        try {
            Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
            if (ringtone != null) title = ringtone.getTitle(getContext());
        } catch (Exception ignored) { }
        JSObject response = new JSObject();
        response.put("uri", uri.toString());
        response.put("title", title);
        call.resolve(response);
    }

    @PluginMethod
    public void configureChannel(PluginCall call) {
        JSObject response = new JSObject();
        String prefix = call.getString("id", "birthday-alarms");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            response.put("id", prefix);
            call.resolve(response);
            return;
        }

        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String soundUri = call.getString("soundUri", defaultUri.toString());
        String id = prefix + "-" + Integer.toHexString(soundUri.hashCode());
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        for (NotificationChannel existing : manager.getNotificationChannels()) {
            if (existing.getId().startsWith(prefix) && !existing.getId().equals(id)) {
                manager.deleteNotificationChannel(existing.getId());
            }
        }

        NotificationChannel channel = new NotificationChannel(
            id, call.getString("name", "Birthday Alarms"), NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(call.getString("description", "Alerts for birthday reminders"));
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[] {0, 400, 200, 400, 200, 800});
        channel.enableLights(true);
        channel.setLightColor(android.graphics.Color.rgb(255, 77, 109));
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(true);
        AudioAttributes audio = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        channel.setSound(Uri.parse(soundUri), audio);
        manager.createNotificationChannel(channel);
        response.put("id", id);
        call.resolve(response);
    }
}
