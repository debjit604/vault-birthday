package com.example.vaultbirthday;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import com.getcapacitor.ActivityResult;
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
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
            "alarm".equals(call.getString("type", "alarm")) ? RingtoneManager.TYPE_ALARM : RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, call.getBoolean("showDefaultItem", true));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, call.getBoolean("showSilentItem", false));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        startActivityForResult(call, intent, "handleRingtoneResult");
    }

    @ActivityCallback
    private void handleRingtoneResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            call.reject("Ringtone selection was cancelled");
            return;
        }
        Uri uri = (Uri) result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        JSObject response = new JSObject();
        response.put("uri", uri.toString());
        call.resolve(response);
    }

    @PluginMethod
    public void configureChannel(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            call.resolve();
            return;
        }
        String id = call.getString("id", "birthday-alarms");
        String name = call.getString("name", "Birthday Alarms");
        String description = call.getString("description", "Alerts for birthday reminders");
        String soundUri = call.getString("soundUri", "content://settings/system/notification_sound");
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(description);
        channel.enableVibration(true);
        channel.enableLights(true);
        channel.setLightColor(android.graphics.Color.rgb(255, 77, 109));
        AudioAttributes audio = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
        channel.setSound(Uri.parse(soundUri), audio);
        manager.createNotificationChannel(channel);
        call.resolve();
    }
}
