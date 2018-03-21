package world.strangers.nomorejam;

import android.graphics.Color;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mhealth.core.mvp.BaseTiPresenter;
import com.schibstedspain.leku.geocoder.GeocoderInteractor;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import rx.schedulers.Schedulers;
import world.strangers.nomorejam.data.DirectionsJSONParser;
import world.strangers.nomorejam.data.MapsActivityView;
import world.strangers.nomorejam.util.ColorHelper;

/**
 * Created by luhonghai on 3/19/18.
 */

public class MapsActivityPresenter extends BaseTiPresenter<MapsActivityView> {
    private static final String TAG = "MapsActivityPresenter";

    private static final int MAX_CHANGE = 10;

    Geocoder geocoder;

    GeocoderInteractor geocoderInteractor;

    public enum LocationType {
        FROM,
        TO
    }

    public MapsActivityPresenter(Geocoder geocoder) {
        this.geocoder = geocoder;
        this.geocoderInteractor = new GeocoderInteractor(geocoder);
    }

    public void pickAddress(LatLng location, LocationType type) {
        this.geocoderInteractor.getFromLocation(location.latitude, location.longitude)
            .subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.io())
            .subscribe(addresses -> {
                if (addresses.size() > 0) {
                    sendToView(v -> v.displayAddress(addresses.get(0), type, location));
                }
            }, throwable -> {
                sendToView(v -> v.showAlert("Could not load address"));
            });
        ;
    }

    public void calculateDirection(LatLng origin, LatLng dest) {
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            final List<PolylineOptions> polylineOptionsList = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                List<HashMap<String, String>> path = result.get(i);
                if (path.size() > 1) {
                    int percent = 0;
                    for (int j = 1; j < path.size(); j++) {
                        LatLng pos1 = getPos(path.get(j - 1));
                        LatLng pos2 = getPos(path.get(j));
                        PolylineOptions lineOptions = new PolylineOptions();
                        lineOptions.add(pos1);
                        lineOptions.add(pos2);
                        lineOptions.width(12);
                        percent = calculateRandomPercent(percent);
                        lineOptions.color(ColorHelper.getColorOfDegradate(Color.GREEN, Color.RED, percent));
                        lineOptions.geodesic(true);
                        polylineOptionsList.add(lineOptions);
                    }
                }
            }
            sendToView(v -> v.displayDirection(polylineOptionsList));
        }
    }

    private int calculateRandomPercent(int previousPercent) {
        Random random = new Random();
        int next = MAX_CHANGE - random.nextInt(MAX_CHANGE * 2);

        int nextPercent =  previousPercent + next;
        if (nextPercent < 0) nextPercent = 0;
        if (nextPercent > 100) nextPercent = 100;
        Log.e(TAG, "calculateRandomPercent: Next " + next + ". NextPercent " + nextPercent + ". previousPercent" + previousPercent);
        return nextPercent;
    }

    private LatLng getPos(HashMap<String, String> point) {
        double lat = Double.parseDouble(point.get("lat"));
        double lng = Double.parseDouble(point.get("lng"));
        return new LatLng(lat, lng);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&alternatives=true";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
