/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;


public class SerialPortFinder {

  public class Driver {
    public Driver(String name, String root) {
      mDriverName = name;
      mDeviceRoot = root;
    }

    private String mDriverName;
    private String mDeviceRoot;
    List<File> mDevices = null;

    public List<File> getDevices() {
      if (mDevices == null) {
        List<File> devices = new LinkedList<>();
        File dev = new File("/dev");
        File[] files = dev.listFiles();
        if (files != null) {
          int i;
          for (i = 0; i < files.length; i++) {
            if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
              Log.d(TAG, "Found new device: " + files[i]);
              devices.add(files[i]);
            }
          }
        }
        mDevices = devices;
      }
      return mDevices;
    }

    public String getName() {
      return mDriverName;
    }
  }

  private static final String TAG = "SerialPort";

  private List<Driver> mDrivers = null;

  List<Driver> getDrivers() throws IOException {
    if (mDrivers == null) {
      List<Driver> drivers = new LinkedList<>();
      LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
      String l;
      while ((l = r.readLine()) != null) {
        // Issue 3:
        // Since driver name may contain spaces, we do not extract driver name with split()
        String drivername = l.substring(0, 0x15).trim();
        String[] w = l.split(" +");
        if ((w.length >= 5) && (w[w.length - 1].equals("serial"))) {
          Log.d(TAG, "Found new driver " + drivername + " on " + w[w.length - 4]);
          drivers.add(new Driver(drivername, w[w.length - 4]));
        }
      }
      r.close();
      mDrivers = drivers;
    }
    return mDrivers;
  }

  public String[] getAllDevices() {
    List<String> devices = new LinkedList<>();
    // Parse each driver
    try {
      for (Driver driver : getDrivers()) {
        for (File f : driver.getDevices()) {
          String device = f.getName();
          String value = String.format("%s (%s)", device, driver.getName());
          devices.add(value);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return devices.toArray(new String[devices.size()]);
  }

  public String[] getAllDevicesPath() {
    List<String> devices = new LinkedList<>();
    // Parse each driver
    try {
      for (Driver driver : getDrivers()) {
        for (File device : driver.getDevices()) {
          devices.add(device.getAbsolutePath());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return devices.toArray(new String[devices.size()]);
  }
}
