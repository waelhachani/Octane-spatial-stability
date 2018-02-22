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
      pc.setHeightCm((short) 250);

      // These settings aren't required in a single spatial reader
      // environment
      // They can be set to zero (which is the default)
      pc.setFacilityXLocationCm(0);
      pc.setFacilityYLocationCm(0);
      pc.setOrientationDegrees((short) 0);

      LocationConfig lc = settings.getSpatialConfig().getLocation();

      // set up filtering and aging on the tags
      lc.setComputeWindowSeconds((short) 10);
      lc.setTagAgeIntervalSeconds((short) 20);

      // set up how often we want to get update reports
      lc.setUpdateIntervalSeconds((short) 1);

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
    // useful for filling Points
    Coordinates tempcoordinates;
    // 5 present_points
    ArrayList<Coordinates> Points = new ArrayList<>();
    // when we go to the next coordinates
    // we compare it with the last if is near
    // we consider that the tag didn't move
    Coordinates last_coordinates = null;
    Coordinates new_coordinates;

    /*
     * public void verifier_Treshold(Coordinates newcoordinates, Coordinates
     * lastcoordinates) {
     *
     * double xn = newcoordinates.x; double yn = newcoordinates.y; double xl =
     * lastcoordinates.x; double yl = lastcoordinates.y; double variation =
     * Math.sqrt(Math.pow(yl - yn, 2) + Math.pow(xl - xn, 2));
     *
     * System.out.println(" variation " + variation); if (variation > 40) {
     * lastcoordinates = newcoordinates; } }
     */

    @Override
    public void onLocationReported(ImpinjReader reader, LocationReport report) {

      // we use updae_Coordinates to sum all coordiantes in one and then devide
      // by 5
      // in order to calculate the mean coordinates which represents the new
      // coordinates
      // while (true) {
      int i = 0;
      tempcoordinates = null;
      if (last_coordinates == null) {

      }
      while (i < 5) {
        try {
          Thread.sleep(1100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        i++;
        float xtemp = report.getLocationXCm();
        float ytemp = report.getLocationYCm();
        if (tempcoordinates == null) {
          tempcoordinates = new Coordinates(xtemp, ytemp);

        } else {
          tempcoordinates.updae_Coordinates(tempcoordinates.x + xtemp, tempcoordinates.y + ytemp);

        }
      }
      tempcoordinates.updae_Coordinates(tempcoordinates.x / 5.0f, tempcoordinates.y / 5.0f);
      float xnew = tempcoordinates.x;
      float ynew = tempcoordinates.y;

      System.out.println(" fraichement cuites " + xnew + "  " + ynew);

      new_coordinates = new Coordinates(xnew, ynew);
      // update last_coordiantes if
      // changes position

      if (last_coordinates == null) {
        last_coordinates = new_coordinates;
      } else {

        double xn = new_coordinates.x;
        double yn = new_coordinates.y;
        double xl = last_coordinates.x;
        double yl = last_coordinates.y;
        double variation = Math.sqrt(Math.pow(yl - yn, 2) + Math.pow(xl - xn, 2));

        if (variation > 40) {
          last_coordinates = new_coordinates;
        }
        // verifier_Treshold(new_coordinates, last_coordinates);
      }

      System.out.println("Location: " + " epc: " + " x: " + last_coordinates.x + " y: " + last_coordinates.y);
    }
    /*
     * System.out.println("Location: " + " epc: " +
     * report.getEpc().toHexString() + " x: " + report.getLocationXCm() + " y: "
     * + report.getLocationYCm() + " read_count: " +
     * report.getConfidenceFactors().getReadCount());
     */
  }
}
