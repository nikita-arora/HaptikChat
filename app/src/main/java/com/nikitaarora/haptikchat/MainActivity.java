package com.nikitaarora.haptikchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ProgressBar progressBar;
    private Button retry;
    private ImageView noInternet;
    static boolean firstLoad=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminate(true);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        retry = (Button) findViewById(R.id.btn_retry);
        noInternet = (ImageView) findViewById(R.id.img_no_internet);

        // data loaded from shared preference to be displayed before fresh data is loaded
        if (!Utils.getStringPreferences(this, "completeChat").equals(""))    {
            try {
                JSONObject jsonObj = new JSONObject(Utils.getStringPreferences(this, "completeChat"));
                parseData(jsonObj);
                firstLoad = false;
            } catch (Exception e)   {
                e.printStackTrace();
            }
        }

        if (checkIfOnline())    {
            getData();
        }

        if (mRecyclerView.getVisibility() == View.VISIBLE)  {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    // check for active wifi or mobile data
    private boolean checkIfOnline() {
        boolean status = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()== NetworkInfo.State.CONNECTED)    {
                status = true;
            }    else {
                netInfo = cm.getNetworkInfo(1);
                if (netInfo!= null && netInfo.getState()== NetworkInfo.State.CONNECTED) {
                    status = true;
                }
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }

        if (!status)    {
            progressBar.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
            showInternetSettings();
        }
        return status;
    }

    //show dialog to enable wifi
    private void showInternetSettings() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Enable Internet?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            noInternet.setVisibility(View.GONE);
                            retry.setVisibility(View.GONE);
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            getData();
                            
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener()  {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            noInternet.setVisibility(View.VISIBLE);
                            retry.setVisibility(View.VISIBLE);
                            retry.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getData();
                                }
                            });
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }   catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getData() {
        progressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Config.DATA_URL, null,
                new Response.Listener<JSONObject>()  {
                    @Override
                    public void onResponse(JSONObject response) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        setProgressBarIndeterminateVisibility(false);
                        progressBar.setVisibility(View.GONE);
                        retry.setVisibility(View.GONE);
                        noInternet.setVisibility(View.GONE);
                        parseData(response);
                        Utils.saveSharedPref(MainActivity.this, "completeChat", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NetworkError) {
                    progressBar.setVisibility(View.GONE);
                    noInternet.setVisibility(View.VISIBLE);
                    retry.setVisibility(View.VISIBLE);
                    retry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            noInternet.setVisibility(View.GONE);
                            retry.setVisibility(View.GONE);
                            getData();
                        }
                    });
                }
                Log.e("Fetch Data", "error in fetching data");
                error.printStackTrace();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void parseData(JSONObject objects) {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = objects.getJSONArray("messages");  } catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter = new ChatAdapter(jsonArray, this);
        mRecyclerView.setAdapter(mAdapter);

        // flag set up to save calling notifyDataSetChanged on every instance
        if (!firstLoad) {
            mAdapter.notifyDataSetChanged();
        }
    }
}