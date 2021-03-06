package com.bracketcove.postrainer.data.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;

import com.bracketcove.postrainer.alarmreceiver.AlarmReceiverActivity;
import com.bracketcove.postrainer.data.reminder.RealmReminder;

import java.util.Calendar;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.realm.internal.IOException;

import static android.content.Context.POWER_SERVICE;
import static com.bracketcove.postrainer.R.string.on;

/**
 * This class is intended to decouple System Services from having to be run in
 * {@link AlarmReceiverActivity}. In theory, this will allow me to treat my alarmreceiver
 * component in a similar manner to which I treat the other ones (i.e. same class
 * structure/testing approach)
 * Created by Ryan on 17/03/2017.
 */

public class AlarmService implements AlarmSource {
    private static final String ALARM_ID = "ALARM_ID";

    private final PowerManager.WakeLock wakeLock;
    private MediaPlayer mediaPlayer;
    private final AudioManager audioManager;
    private final Vibrator vibe;
    private final AlarmManager alarmManager;
    private Context context;

    public AlarmService(Context applicationContext) {
        //TODO: handle this s**t in Dagger 2 somehow
        PowerManager powerManager = (PowerManager) applicationContext.getSystemService(POWER_SERVICE);
        this.wakeLock = ((PowerManager) applicationContext
                .getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Alarm");

        audioManager =  ((AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE));

        mediaPlayer = new MediaPlayer();

        vibe = ((Vibrator) applicationContext.getSystemService(Context.VIBRATOR_SERVICE));

        alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
    }


    @Override
    public Completable setAlarm(RealmReminder reminder) {
        Calendar alarm = Calendar.getInstance();
        alarm.setTimeInMillis(System.currentTimeMillis());
        alarm.set(Calendar.HOUR_OF_DAY, reminder.getHourOfDay());
        alarm.set(Calendar.MINUTE, reminder.getMinute());

        checkAlarm(alarm);

        Intent intent = new Intent(context, AlarmReceiverActivity.class);
        intent.putExtra(ALARM_ID, reminder.getReminderId());
        PendingIntent alarmIntent = PendingIntent.getActivity(context,
                Integer.parseInt(reminder.getReminderId()),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (reminder.isRenewAutomatically()){
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(),
                    alarmIntent);
        }

        //TODO: make sure that we set Reminder state to Active at some point
        return Completable.complete();
    }

    private void checkAlarm(Calendar alarm){
        Calendar now = Calendar.getInstance();
        if (alarm.before(now)){
            long alarmForFollowingDay = alarm.getTimeInMillis() + 86400000L;
            alarm.setTimeInMillis(alarmForFollowingDay);
        }
    }

    @Override
    public Completable cancelAlarm(RealmReminder reminder) {

        Intent intent = new Intent(context, AlarmReceiverActivity.class);

        PendingIntent alarmIntent = PendingIntent.getActivity(context,
                Integer.parseInt(reminder.getReminderId()),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(alarmIntent);

        return Completable.complete();
    }

    @Override
    public void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibe != null) {
            vibe.cancel();
        }
    }

    /**
     * Starts an Alarm with the Requisite Parameters:
     */
    @Override
    public void startAlarm(RealmReminder reminder) {
        wakeLock.acquire();

        if (reminder.isVibrateOnly()){
            vibratePhone();
        } else {
            vibratePhone();

            try {
                playAlarmSound();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void releaseWakeLock() {
        if (wakeLock != null){
            wakeLock.release();
        }
    }

    private void playAlarmSound() throws java.io.IOException {
        mediaPlayer.setDataSource(getAlarmUri());

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                }
            };

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            mediaPlayer.prepare();
        }
    }

    private void vibratePhone() {
        long[] vPatternOne = {0, 1000, 2000, 1000, 2000, 1000, 2000, 1000, 2000};
        vibe.vibrate(vPatternOne, -1);
    }

    private String getAlarmUri() {
        String alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString();
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString();
            }
        }
        return alert;
    }
}
