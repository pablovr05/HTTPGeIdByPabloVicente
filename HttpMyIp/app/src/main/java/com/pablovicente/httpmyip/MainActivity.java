package com.pablovicente.httpmyip;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final Button ipButton = findViewById(R.id.ipButton);

        final TextView ipText = findViewById(R.id.ipText);

        ipButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Se pulsó el botón obtener IP", Toast.LENGTH_SHORT).show();

            ExecutorService executor = Executors.newSingleThreadExecutor();


            executor.execute(() -> {
                Log.d("NetworkRequest", "Starting network request");
                String data = getDataFromUrl("https://api.myip.com");
                Log.d("NetworkRequest", "Data received: " + data);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    if (data != null) {
                        String ip = "";
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            ip = jsonObject.getString("ip");
                        } catch (Exception e) { e.printStackTrace();}

                        Log.d("NetworkRequest", "Setting text on TextView");
                        ipText.setText("Tu ip es: " + ip);

                    } else {
                        Log.e("NetworkRequest", "Failed to retrieve data");
                        ipText.setText("Error al obtener la IP");
                    }
                });
            });
        });
    }

    String error = ""; // string field
    private String getDataFromUrl(String demoIdUrl) {

        String result = null;
        int resCode;
        InputStream in;
        try {
            URL url = new URL(demoIdUrl);
            URLConnection urlConn = url.openConnection();

            HttpsURLConnection httpsConn = (HttpsURLConnection) urlConn;
            httpsConn.setAllowUserInteraction(false);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.setRequestMethod("GET");
            httpsConn.connect();
            resCode = httpsConn.getResponseCode();

            Log.d("NetworkRequest","ResCode: " + resCode + " HTTP_OK: " + HttpURLConnection.HTTP_OK);

            if (resCode == HttpURLConnection.HTTP_OK) {

                in = httpsConn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                in.close();
                result = sb.toString();
            } else {
                error += "HTTP error code: " + resCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            error += "Error: " + e.getMessage();
        }
        return result;
    }
}