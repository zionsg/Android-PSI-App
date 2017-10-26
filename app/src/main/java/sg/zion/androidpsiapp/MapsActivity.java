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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

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

    /**
     * Base url for data.gov.sg API
     */
    private String apiUrl = "https://api.data.gov.sg/v1/environment/psi";

    /**
     * API key for data.gov.sg
     */
    private String apiKey = BuildConfig.API_KEY_DATA_GOV_SG;

    /**
     * List of regions, excluding "national"
     */
    private String[] regionNames = {"east", "west", "north", "south", "central"};

    /**
     * Map instance
     */
    private GoogleMap mMap;

    /**
     * Initial creation of the fragment
     */
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
     * Callback triggered when map is ready to be used.
     *
     * Manipulates the map once available.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Call API to update map - location permissions not needed for this
        callApi();

        // Note the vendor's layer on top of the OS may precede this and show its notice instead
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
        } else {
            // Show rationale and request permission.
            showDialog(
                    "Location Permissions Error",
                    "Please enable location permissions for this app in Settings."
            );
        }
    }

    /**
     * Callback triggered when My Location button in map is clicked
     */
    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     * Call data.gov.sg API for PSI readings
     *
     * Success response is passed to updateMap().
     */
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
                        updateMap(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("callApi", "Error: " + error.toString());
                        Toast.makeText(
                                MapsActivity.this,
                                "Error getting PSI updates. Please check connectivity.",
                                Toast.LENGTH_LONG
                        ).show();
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

    /**
     * Update map with PSI readings
     */
    protected void updateMap(JSONObject response) {
        Map<String, HashMap<String, Double>> result = parseResponse(response);

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_BLUE);

        String name;
        HashMap<String, Double> info;
        LatLng position;
        String reading;
        String text;
        for (Map.Entry<String, HashMap<String, Double>> entry : result.entrySet()) {
            name = entry.getKey();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            info = (HashMap<String, Double>) entry.getValue();
            position = new LatLng(info.get("latitude"), info.get("longitude"));
            reading = String.format("%.0f", info.get("reading")); // show reading as int, not double
            text = name + "\n" + "PSI: " + reading;

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV())
                    .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text)))
            );
        }
    }

    /**
     * Parse API response
     */
    protected Map<String, HashMap<String, Double>> parseResponse(JSONObject response) {
        Map<String, HashMap<String, Double>> result =
                new HashMap<String, HashMap<String, Double>>();

        // Get PSI readings for all regions
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

        // Collate latlong and PSI reading for each region
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

    /**
     * Helper method to show an alert dialog
     */
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
