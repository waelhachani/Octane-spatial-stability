package com.example.sdksamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.LocationConfig;
import com.impinj.octane.LocationReport;
import com.impinj.octane.LocationReportListener;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.PlacementConfig;
import com.impinj.octane.ReaderMode;
import com.impinj.octane.Settings;
import com.impinj.octane.SpatialMode;

public class SpatialReaderLocation {

  public static void main(String[] args) {
    try {
      String hostname = "192.168.2.229";
      ImpinjReader reader = new ImpinjReader();

      reader.connect(hostname);

      // set up the listener for special location reports
      reader.setLocationReportListener(new LocationReportListenerImplementation());

      Settings settings = reader.queryDefaultSettings();

      // set spatial reader into location mode
      settings.getSpatialConfig().setMode(SpatialMode.Location);

      // Set spatial reader placement parameters
      PlacementConfig pc = settings.getSpatialConfig().getPlacement();

      // The mounting height of the spatial reader, in centimeters
      pc.setHeightCm((short) 457);

      // These settings aren't required in a single spatial reader
      // environment
      // They can be set to zero (which is the default)
      pc.setFacilityXLocationCm(0);
      pc.setFacilityYLocationCm(0);
      pc.setOrientationDegrees((short) 0);

      LocationConfig lc = settings.getSpatialConfig().getLocation();

      // set up filtering and aging on the tgs
      lc.setComputeWindowSeconds((short) 10);
      lc.setTagAgeIntervalSeconds((short) 20);

      // set up how often we want to get update reports
      lc.setUpdateIntervalSeconds((short) 5);

      // disable antennas targeting areas from which we may not want
      // location reports,
      // in this case we're disabling antennas 10 and 15
      List<Short> disabledAntennas = new ArrayList<>();
      disabledAntennas.add((short) 10);
      disabledAntennas.add((short) 15);
      lc.setDisabledAntennaList(disabledAntennas);

      // enable all three reports
      lc.setEntryReportEnabled(true);
      lc.setExitReportEnabled(true);
      lc.setUpdateReportEnabled(true);

      // set power used for each antenna by first indicating that the max
      // power
      // should not be used, then by setting an explicit power in dbm
      lc.setIsMaxTxPower(false);
      lc.setTxPowerinDbm(20.0);

      // set up some general reader settings
      settings.setSession(2);
      settings.setReaderMode(ReaderMode.AutoSetDenseReader);

      reader.applySettings(settings);

      reader.start();

      System.out.println("Press enter to continue.");
      Scanner s = new Scanner(System.in);
      s.nextLine();
      s.close();

      reader.stop();

      reader.disconnect();
    } catch (OctaneSdkException ex) {
      System.out.println(ex.getMessage());
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace(System.out);
    }
  }

  public static class LocationReportListenerImplementation implements LocationReportListener {

    @Override
    public void onLocationReported(ImpinjReader reader, LocationReport report) {
      System.out.println("Location: " + " epc: " + report.getEpc().toHexString() + " x: " + report.getLocationXCm()
          + " y: " + report.getLocationYCm() + " read_count: " + report.getConfidenceFactors().getReadCount());
    }
  }
}
