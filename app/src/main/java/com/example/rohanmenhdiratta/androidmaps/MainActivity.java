package com.example.rohanmenhdiratta.androidmaps;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    final String API_KEY = "AIzaSyDUYg2nD_YP-OCym6Hy_gomwE8D_kHw6ZE";
    final String httpRequest = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    public String responseJSON;
    public HttpResponse _response;

    public Button submitButton;
    public Button mapButton;
    public Button satelliteButton;
    public Button hybridButton;

    public EditText addressText;

    public String placeID;

    GoogleMap _mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final String address = "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=AIzaSyDUYg2nD_YP-OCym6Hy_gomwE8D_kHw6ZE";

        submitButton = (Button)findViewById(R.id.submitbutton);
        mapButton = (Button)findViewById(R.id.mapbutton);
        satelliteButton = (Button)findViewById(R.id.satellitebutton);
        hybridButton = (Button)findViewById(R.id.hybridbutton);

        addressText = (EditText)findViewById(R.id.addresstext);

        _mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        satelliteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });

        hybridButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitButton.setEnabled(false);

                try  {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                String address = addressText.getText().toString();
                address = address.replace(" ", "+");
                final String url = httpRequest + address + "&key=" + API_KEY;
                Log.d("url", url);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        responseJSON = getJSON(url);
                        Log.d("task", responseJSON);


                    }
                });

                t.start();


                try {
                    t.join();

                     if(_response.getStatusLine().getReasonPhrase().equals("OK")) {
                        LatLng latLng = parseJSONForLatLng(responseJSON);

                         if(!(address.equals(null) && placeID.equals(null)))
                         {
                             addMarker(_mMap, latLng, address, placeID);
                         }
                         else
                         {
                             addMarker(_mMap, latLng, "", "");
                         }
                        Toast.makeText(getBaseContext(), "Marker Added", Toast.LENGTH_LONG);
                     }
                    else{
                         Toast.makeText(getBaseContext(), "Address Not Found", Toast.LENGTH_LONG);
                     }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }



                submitButton.setEnabled(true);
            }
        });





        //addMarker(_mMap, new LatLng(37.4224879,-122.08422), "Swag", "this is swag city");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("Marker"));
    }

    public String getJSON(String address) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();

            //Toast.makeText(getBaseContext(), response.getStatusLine().getReasonPhrase(),Toast.LENGTH_LONG);
            Log.d("task", response.getStatusLine().getReasonPhrase());
            _response = response;


            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(MainActivity.class.toString(), "Failedet JSON object");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public void addMarker(GoogleMap map, LatLng latLng, String title, String snippet)
    {
        map.addMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public LatLng parseJSONForLatLng(String json)
    {
        LatLng latLng = new LatLng(0,0);

        try {
            JSONObject jsonObject = new JSONObject(json);

            //String namesResults[] = {"results"};
            JSONArray results = jsonObject.getJSONArray("results");
            JSONObject resultsObj = results.getJSONObject(0);
            //Log.d("results", resultsObj.toString());

            //String namesGeometry[] = {"geometry"};
            JSONObject geometry = resultsObj.getJSONObject("geometry");
            //geometry = new JSONObject(geometry, namesGeometry);
            //Log.d("geometry", geometry.toString());

            //String namesLocation[] = {"location"};
            JSONObject location = geometry.getJSONObject("location");
            //Log.d("location", location.toString());

            placeID = resultsObj.getString("place_id");

            String lat = location.getString("lat");
            String lng = location.getString("lng");

            //Log.d("latlng", lat + " " + lng);

            LatLng myLatLng =  new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            latLng = myLatLng;
        }
        catch(Throwable t)
        {
            Log.d("json", "could not convert to json");
        }

        return latLng;
    }

}

