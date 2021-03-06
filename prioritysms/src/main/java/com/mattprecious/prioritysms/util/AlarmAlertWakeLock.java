/*
 * Copyright 2013 Matthew Precious
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.prioritysms.util;

import android.content.Context;
import android.os.PowerManager;

/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and released in the AlarmAlert
 * activity
 */
public class AlarmAlertWakeLock {
  private static PowerManager.WakeLock cpuWakeLock;

  public static PowerManager.WakeLock createPartialWakeLock(Context context) {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TAG");
  }

  public static void acquireCpuWakeLock(Context context) {
    if (cpuWakeLock != null) {
      return;
    }

    cpuWakeLock = createPartialWakeLock(context);
    cpuWakeLock.acquire();
  }

  public static void acquireScreenCpuWakeLock(Context context) {
    if (cpuWakeLock != null) {
      return;
    }
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    cpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
        | PowerManager.ACQUIRE_CAUSES_WAKEUP
        | PowerManager.ON_AFTER_RELEASE, "TAG");
    cpuWakeLock.acquire();
  }

  public static void releaseCpuLock() {
    if (cpuWakeLock != null) {
      cpuWakeLock.release();
      cpuWakeLock = null;
    }
  }
}
