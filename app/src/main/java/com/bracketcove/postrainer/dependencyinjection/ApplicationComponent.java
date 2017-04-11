/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bracketcove.postrainer.dependencyinjection;

import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.Vibrator;

import com.bracketcove.postrainer.data.alarm.AlarmSource;
import com.bracketcove.postrainer.data.reminder.ReminderSource;
import com.bracketcove.postrainer.util.BaseSchedulerProvider;

import javax.inject.Singleton;

import dagger.Component;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

  Context context();
  ReminderSource reminderSource();
  AlarmSource alarmSource();
  PowerManager.WakeLock wakeLock();
  AudioManager audioManager();
  Vibrator vibrator();
  BaseSchedulerProvider scheduler();
}
