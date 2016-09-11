package com.olive.prayertimes;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by frkn on 11.09.2016.
 */
public class MyGeocoder {

    private static final String TAG = "GEOCODER";

    private Context context;
    // GPSTracker class
    private GPSTracker gps;

    public double latitude, longitude;

    public MyGeocoder(Context context){
        this.context = context;
    }

    public void GeoAddress(){
        getLatLong();
        new GeocodeAsyncTask().execute();
    }


    private void getLatLong() {
        Log.d(TAG, "getLatLong");
        gps = new GPSTracker(context);

        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            System.out.println("Your Location is - \nLat: " + latitude + "\nLong: " + longitude);
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }


    private class GeocodeAsyncTask extends AsyncTask<Void, Void, Address> {

        String errorMessage = "";

        @Override
        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Address doInBackground(Void ... none) {
            Log.d(TAG, "doInBack..");
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException ioException) {
                errorMessage = "Service Not Available";
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = "Invalid Latitude or Longitude Used";
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + latitude + ", Longitude = " +
                        longitude, illegalArgumentException);
            }

            if(addresses != null && addresses.size() > 0)
                return addresses.get(0);

            return null;
        }

        protected void onPostExecute(Address address) {
            if(address == null) {
                //progressBar.setVisibility(View.INVISIBLE);
                //infoText.setVisibility(View.VISIBLE);
                //infoText.setText(errorMessage);
                System.out.println(errorMessage);
            }
            else {
                String addressName = "";
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressName += " --- " + address.getAddressLine(i);
                }
                //progressBar.setVisibility(View.INVISIBLE);
                System.out.println("Latitude: " + address.getLatitude() + "\n" +
                        "Longitude: " + address.getLongitude() + "\n" +
                        "Address: " + addressName);
                int index = addressName.lastIndexOf(" ");
                String stateInfo = addressName.substring(index+1);
                System.out.println("District: " + stateInfo.split("/")[0]);
                System.out.println("State: " + stateInfo.split("/")[1]);
                System.out.println("Country: " + address.getCountryName());
                /*infoText.setText("Latitude: " + address.getLatitude() + "\n" +
                        "Longitude: " + address.getLongitude() + "\n" +
                        "Address: " + addressName);*/
            }
        }
    }

}
