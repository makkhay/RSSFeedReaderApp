package com.example.makkhay.rssfeedapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = " MAINActivity";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
    private int feedLimit =10;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.myListView);

//        Log.d(TAG, "onCreate: starting Asynctask");
//        DownloadData downloadData = new DownloadData();
//        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=100/xml");
//        Log.d(TAG, "onCreate:done");

        downloadUrl(String.format(feedUrl,feedLimit));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_menu,menu);

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id){
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.menuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.menu10:
            case R.id.menu25:
                if(!item.isChecked()){
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle()+ " setting feedlimit to " + feedLimit);
                }else{
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle()+ " feedlimit unchanged ");
                }
                break;

            default:
                return super.onOptionsItemSelected(item);


        }
        downloadUrl(String.format(feedUrl,feedLimit));
        return  true;

    }

    private void downloadUrl(String feedUrl){
        Log.d(TAG, "downloadUrl: starting Asynctask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedUrl);
        Log.d(TAG, "downloadUrl:done");
    }



    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute:: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            // using a default array adapter
//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<>(MainActivity.this,
//                    R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);

            // Using a custom adapter for better customization
            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record
                    , parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);


        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackGround:: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInbackground: Error downloading");
            }

            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();
            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The resonse code was" + response);
//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);

                // This line of code does the same thing as three lines of code above
                // Buffer reader reads text from a character-input stream.
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();

                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downoadXML: Invalid URL" + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "downoadXML: IO Exception reading data: " + e.getMessage());
                e.printStackTrace();
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception. Needs Permission?" + e.getMessage());
                // this line of code(printStackTrace) will show the line of code where the error is caused.
                e.printStackTrace();
            }


            return null;
        }


    }

}
