package com.deoguyadin.druginteractions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by: Deo Guyadin
 *
 * Description: Handles and retrieves from a URL entered as a string.
 */

public class HTTPDataHandler {

    private static String stream = null;

    // Constructor
    public HTTPDataHandler() {

    }

    public String GetHTTPData(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Check the Connection Status (if response code is 200 then ok)
            if (urlConnection.getResponseCode() == 200) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // Read the BufferedInputStream
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
                stream = sb.toString();

                // End of reading, now disconnect the HttpURLConnection
                urlConnection.disconnect();
            }
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Return the data from input URL
        return stream;
    }
}
