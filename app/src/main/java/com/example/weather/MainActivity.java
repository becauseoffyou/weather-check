package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    ImageView imageGif;
    TextView kota, dateNow, timeNow, suhu, keterangan, ws, humadity, visib, airpres;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    String latitude, longitude;
    private ProgressBar loadingPB;
    boolean isProgressVisible = false;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateNow = findViewById(R.id.tanggalsekarang);
        timeNow = findViewById(R.id.waktusekarang);
        kota = findViewById(R.id.nama_kota);
        imageGif = findViewById(R.id.image_5);
        suhu = findViewById(R.id.derajat);
        keterangan = findViewById(R.id.keterangancuaca);
        ws = findViewById(R.id.wsvalue);
        humadity = findViewById(R.id.hmdtyvalue);
        visib = findViewById(R.id.visvalue);
        airpres = findViewById(R.id.presureval);
        loadingPB = findViewById(R.id.pgb);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);

        getLocation();
        GetWeather();

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                            getLocation();
                            GetWeather();
                            swipeRefreshLayout.setRefreshing(false);
//                        }
                    }
                });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10){
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Izin lokasi tidak di aktifkan!", Toast.LENGTH_SHORT).show();
            }else{
                getLocation();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // get Permission
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 10);
        } else {
            // get Location
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    getCurrentLocation();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        isProgressVisible = true;
        loadingPB.setVisibility(View.VISIBLE);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult.getLocations().size() > 0){
                            int latestlocationIndex = locationResult.getLocations().size() - 1;
                            double latitudes =
                                    locationResult.getLocations().get(latestlocationIndex).getLatitude();
                            latitude = String.valueOf(latitudes);
                            double longitudes =
                                    locationResult.getLocations().get(latestlocationIndex).getLongitude();
                            longitude = String.valueOf(longitudes);
                            GetWeather();
                        }
                    }
                }, Looper.getMainLooper());



    }

    void GetWeather() {
        String URL = "https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"&lon="+longitude+"&appid=246870ca0491e4f355fa3c139dd60029&lang=ID&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);


        JSONObject jsonBody = new JSONObject();
        final String requestBody = jsonBody.toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Calendar c1 = Calendar.getInstance();
                            JSONObject jsonPost = new JSONObject(response.toString());

                            SimpleDateFormat tanggal = new SimpleDateFormat("d MMMM yyyy");
                            SimpleDateFormat waktu = new SimpleDateFormat("H:m");
                            //SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
                            String strdate1 = tanggal.format(c1.getTime());
                            String strtime1 = waktu.format(c1.getTime());
                            dateNow.setText(strdate1);
                            timeNow.setText(strtime1);
                            kota.setText(jsonPost.getString("name"));

                            JSONObject suhuvalue = new JSONObject(jsonPost.getString("main"));
                            suhu.setText(suhuvalue.getString("temp"));

                            JSONArray ketsuhu = new JSONArray(jsonPost.getString("weather"));
                            int len = ketsuhu.length();
                            for (int j = 0; j < len; j++) {
                                JSONObject json = ketsuhu.getJSONObject(j);
                                keterangan.setText(json.getString("description"));
                                Log.d("ccc",json.getString("main"));
                                if(json.getString("main").equals("Clouds")){
                                    imageGif.setImageResource(R.drawable.berawan);
                                }else if(json.getString("main").equals("Rain")){
                                    imageGif.setImageResource(R.drawable.hujan);
                                }else if(json.getString("main").equals("Clear")){
                                    imageGif.setImageResource(R.drawable.panas);
                                }else{
                                    imageGif.setImageResource(R.drawable.malamcerah);
                                }
                                JSONObject speedangin = new JSONObject(jsonPost.getString("wind"));
                                ws.setText(speedangin.getString("speed"));
                                JSONObject hmdt = new JSONObject(jsonPost.getString("main"));
                                humadity.setText(hmdt.getString("humidity"));
                                visib.setText(jsonPost.getString("visibility"));
                                JSONObject airpresure = new JSONObject(jsonPost.getString("main"));
                                airpres.setText(airpresure.getString("pressure"));
                            }

                            loadingPB.setVisibility(View.GONE);
                            isProgressVisible = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error Response", error.toString());
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }



}