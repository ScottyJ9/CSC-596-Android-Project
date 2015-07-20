package com.example.owner.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("65802");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String[] forecastArray = {
                "Today - Sunny - 88/83",
                "Tomorrow - Foggy - 78/58",
                "Weds - Cloudy - 72/62",
                "Thursday - Astroids - 73/48",
                "Friday - Heavy Rain - 63/58",
                "saturday - Rain - 60/50",
                "Sunday - Sunny - 88/68"
        };
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        mForecastAdapter = new ArrayAdapter<String>(
                //the curetn context
                getActivity(),
                //ID of the list item layout
                R.layout.list_item_forecast,
                //Id of the text view to populate
                R.id.list_item_forecast_textview,
                //Forcast data
                weekForecast);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String,Void,String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /*the date/time convers code is going to be moived outside the asynctask later,
            so for cnvenience we're breaking it out into its own method now
         */

        private String getReadableDateString(int dateNum, int weekDay, int month, int queryNum){
            //because the api return a nix timestamp(measure in seconds)
            //it must be converted to milisecionds in order to be convered to a vaild date
            String monthString;
            switch (month) {
                case 1:  monthString = "January";
                    break;
                case 2:  monthString = "February";
                    break;
                case 3:  monthString = "March";
                    break;
                case 4:  monthString = "April";
                    break;
                case 5:  monthString = "May";
                    break;
                case 6:  monthString = "June";
                    break;
                case 7:  monthString = "July";
                    break;
                case 8:  monthString = "August";
                    break;
                case 9:  monthString = "September";
                    break;
                case 10: monthString = "October";
                    break;
                case 11: monthString = "November";
                    break;
                case 12: monthString = "December";
                    break;
                default: monthString = "Invalid month";
                    break;
            }
            System.out.println(monthString);

            String weekDayString;
            switch (weekDay) {
                case 1:
                    weekDayString = "Sunday";
                    break;
                case 2:
                    weekDayString = "Monday";
                    break;
                case 3:
                    weekDayString = "Tuesday";
                    break;
                case 4:
                    weekDayString = "Wednesday";
                    break;
                case 5:
                    weekDayString = "Thursday";
                    break;
                case 6:
                    weekDayString = "Friday";
                    break;
                case 7:
                    weekDayString = "Saturday";
                    break;
                default: weekDayString = "Invalid Week Day";
                    break;
            }
            if(queryNum == 0){
                weekDayString = "Today";
            }
            else if(queryNum ==1){
                weekDayString = "Tomorrow";
            }
            System.out.println(weekDayString);
            String resultString = weekDayString + monthString + dateNum;
            return resultString;
        }

        /*
         * prepare for high/lows for presentation
         */
        private String formatHighLows(double high, double low){
            //for presentaion, assume the user doesn't care about tenths of a degree
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /*
         * take the string representing the complete forcast in json format
         * and pull out the data we need to construct the stings needed for the wireframes
         *
         * fortunately parsing is easy: construction takes the JSON string and convers it into an
         * object hierarchy for us.
         */

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)throws JSONException{
            final String OWM_LIST = "list";
            final String OWN_WEATHER = "weather";
            final String OWN_TEMPERATURE = "temp";
            final String OWN_MAX = "max";
            final String OWN_MIN = "min";
            final String OWN_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


            //owm return daily forecast based upon the local time of the city that is being asked for
            // which means that we need to know the GMT offset to translate this data properly

            // since this data is also sent in oder and the first is always the current day
            // we are going to take advantage of that to get a nice normatilve UTC date for all of our weather


            Calendar calender = Calendar.getInstance();
            int dayIs = calender.get(Calendar.DATE);
            String[] resultStrs = new String[numDays];
            for(int i=0; i < weatherArray.length(); i++) {
                //for now using the format day, description, hi/lo
                String day;
                String description;
                String highAndLow;
                //get the json object reprsenting the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                //the data/time is return as a long. we need to convert that into
                // something human-readable, since most people wont read 134304309 as this saturda
                int dateNum = calender.get(Calendar.DATE);
                int weekDay = calender.get(Calendar.DAY_OF_WEEK);
                int month = calender.get(Calendar.MONTH);


                day = getReadableDateString(dateNum, weekDay, month, i);

                //description is in a child "weather" whic his 1 element long
                JSONObject weatherObject = dayForecast.getJSONArray(OWN_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWN_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWN_TEMPERATURE);
                double high = temperatureObject.getDouble(OWN_MAX);
                double low = temperatureObject.getDouble(OWN_MIN);
                highAndLow = formatHighLows(high, low);

                resultStrs[i] = day + " - " + description + " - " + highAndLow;
                calender.add(Calendar.DATE, 1);
            }

            for( String s: resultStrs){

                Log.v(LOG_TAG, "forcast entry: " + s);
            }
            return resultStrs;
        }
        @Override
        protected String[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //http://api.openweathermap.org/data/2.5/forecast/daily?q=65802&mode=json&units=metric&cnt=7
                final String FORECAST_BASE_URL =  "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUEREY_PARAM= "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUEREY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "BuiltUri " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result){
            if(result != null) {
                mForecastAdapter.clear();
                for (String dayForecastStr : result) {
                    mForecastAdapter.add(dayForecastStr);
                }
            }
        }
    }
}

