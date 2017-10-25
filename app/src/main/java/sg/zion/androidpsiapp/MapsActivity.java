package sg.zion.androidpsiapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity
        implements OnMyLocationButtonClickListener,
        OnMapReadyCallback {

    private String apiUrl = "https://api.data.gov.sg/v1/environment/psi";

    private String apiKey = BuildConfig.API_KEY_DATA_GOV_SG;

    private String[] regionNames = {"east", "west", "north", "south", "central"};

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
        } else {
            // Show rationale and request permission.
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        callApi();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    protected void callApi() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Construct query url
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
        String currDateTime = sdf.format(new Date());
        try {
            currDateTime = URLEncoder.encode(currDateTime, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // do nothing
        }
        String url = apiUrl + "?date_time=" + currDateTime;

        // Request a JSON response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("callApi", "Response: " + response.toString());
                        Map<String, HashMap<String, Double>> result = parseResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("callApi", "Error: " + error.toString());
                        Toast.makeText(MapsActivity.this, "Error getting PSI updates", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("api-key", apiKey);

                    return headers;
                }
        };

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    protected Map<String, HashMap<String, Double>> parseResponse(JSONObject response) {
        Map<String, HashMap<String, Double>> result =
                new HashMap<String, HashMap<String, Double>>();

        JSONArray items = response.optJSONArray("items");
        JSONObject item = (null == items || 0 == items.length())
                ? null
                : items.optJSONObject(0);
        JSONObject readings = new JSONObject();
        Boolean hasReadings = false;
        if (item != null) {
            readings = item.optJSONObject("readings");
            hasReadings = (readings != null && readings.has("psi_twenty_four_hourly"));
        }

        // Read PSI index for each region
        Map<String, Double> psiReadings = new HashMap<String, Double>();
        if (! hasReadings) {
            // Defaults
            for (String regionName : regionNames) {
                psiReadings.put(regionName, 0.0);
            }
        } else {
            JSONObject data = readings.optJSONObject("psi_twenty_four_hourly"); // alr checked
            for (String regionName : regionNames) {
                psiReadings.put(regionName, data.optDouble(regionName, 0.0));
            }
        }

        JSONArray regions = response.optJSONArray("region_metadata");
        JSONObject region;
        String name;
        JSONObject location;
        double latitude;
        double longitude;
        double reading;
        HashMap<String, Double> info;
        for (int i = 0; i < regions.length(); i++) {
            region = regions.optJSONObject(i);
            name = region.optString("name", "");
            location = region.optJSONObject("label_location");
            latitude = (null == location) ? 0.0 : location.optDouble("latitude", 0.0);
            longitude = (null == location) ? 0.0 : location.optDouble("longitude", 0.0);
            reading = psiReadings.containsKey(name) ? psiReadings.get(name) : 0.0;
            if (0 == reading) {
                continue;
            }

            info = new HashMap<String, Double>();
            info.put("latitude", latitude);
            info.put("longitude", longitude);
            info.put("reading", reading);
            result.put(name, info);

            Log.d(
                    "parseResponse",
                    String.format("%s: %f, %f, %f", name, latitude, longitude, reading)
            );
        }

        return result;
    }

    protected void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle("Dialog")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }
}
