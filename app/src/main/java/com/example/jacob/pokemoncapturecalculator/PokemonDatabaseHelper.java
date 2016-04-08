package com.example.jacob.pokemoncapturecalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.widget.Toast;

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
import java.util.List;

/**
 *
 */
public class PokemonDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pokemondb";
    private static final int DB_VERSION = 1;
    private static final String BASE_URL = "http://pokeapi.co/api/v2/";
    private final Context context;

    PokemonDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE POKEMON ("
                + "_id TEXT PRIMARY KEY, " // Let the species name be the primary key, since it should be unique
                + "CAPTURE_RATE INTEGER, "
                + "BASE_HP INTEGER);");
        readAPI(1, 151);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static void insertPokemon(SQLiteDatabase db, String species, Pokemon pokemon) {
        ContentValues pokemonValues = new ContentValues();
        pokemonValues.put("_id", species);
        pokemonValues.put("CAPTURE_RATE", pokemon.captureRate);
        pokemonValues.put("BASE_HP", pokemon.baseHP);
        db.insert("POKEMON", null, pokemonValues);
    }

    public List<String> getAllLabels() {
        List<String> labels = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("POKEMON", new String[]{"_id"}, null, null, null, null, "_id ASC");
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return labels;
    }

    private void readAPI(int start, int end) {
        for (int i = start; i <= end; i++) {
            String url1 = BASE_URL + "pokemon/" + i + "/";
            String url2 = BASE_URL + "pokemon-species/" + i + "/";
            APIContact contact = new APIContact();
            contact.execute(url1, url2);
        }
    }

    private static String cleanName(String input) {
        // Make sure the input isn't null
        if (input == null) return null;
        String str = input.trim();
        // Make sure the input was more than whitespace characters
        if (str.length() == 0) return input;
        // Insert the apostrophe for Farfetch'd
        if (str.equals("farfetchd")) return "Farfetch'd";
        // Deal with Flabébé's accent marks
        if (str.equals("flabebe")) return "Flabébé";
        // If the name contains a hyphen in the JSON file, it needs to be handled differently
        if (str.contains("-")) {
            switch (str) {
                case "nidoran-f":
                    return "Nidoran (F)";
                case "nidoran-m":
                    return "Nidoran (M)";
                case "mr-mime":
                    return "Mr. Mime";
                case "ho-oh":
                    return "Ho-Oh";
                case "mime-jr":
                    return "Mime Jr.";
                case "porygon-z":
                    return "Porygon-Z";
            }
        }
        // If we didn't hit a special case, capitalize the first letter and return the string
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private class APIContact extends AsyncTask<String, String, String[]> {

        private Pokemon pokemon;
        private String name;

        @Override
        protected void onPreExecute() {
            pokemon = new Pokemon();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String[] results = new String[params.length];
            InputStream inputStream = null;
            for (int i = 0; i < results.length; i++) {
                try {
                    URL url = new URL(params[i]);
                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setReadTimeout(10000);
                        urlConnection.setConnectTimeout(15000);
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoInput(true);
                        urlConnection.connect();
                        inputStream = urlConnection.getInputStream();
                        BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = streamReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        results[i] = stringBuilder.toString();
                        publishProgress(params[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    results[0] = "Unable to access API";
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if ((results != null) && !results[0].equals("Unable to access API")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(results[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (jsonObject != null) {
                    try {
                        String jsonName = jsonObject.getString("name");
                        name = cleanName(jsonName);
                        JSONArray jsonArray = jsonObject.getJSONArray("stats");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject stat = jsonArray.getJSONObject(i);
                            JSONObject statName = stat.getJSONObject("stat");
                            if (statName.getString("name").equals("hp")) {
                                pokemon.baseHP = stat.getInt("base_stat");
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                jsonObject = null;
                try {
                    jsonObject = new JSONObject(results[1]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (jsonObject != null) {
                    try {
                        pokemon.captureRate = jsonObject.getInt("capture_rate");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                SQLiteDatabase db = getReadableDatabase();
                insertPokemon(db, name, pokemon);
                db.close();
            }
        }

        @Override
        protected void onProgressUpdate(String... params) {
            super.onProgressUpdate(params);
            Toast toast = Toast.makeText(context, params[0], Toast.LENGTH_SHORT);
            toast.show();
        }

    }

}
