package com.example.jacob.pokemoncapturecalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PokemonDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pokemondb";
    private static final int DB_VERSION = 1;

    PokemonDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE POKEMON ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "SPECIES TEXT, "
                + "CAPTURE_RATE INTEGER, "
                + "BASE_HP INTEGER);");

        insertPokemon(db, "Bulbasaur", 45, 45);
        insertPokemon(db, "Caterpie", 255, 45);
        insertPokemon(db, "Pikachu", 190, 35);
        insertPokemon(db, "Mewtwo", 3, 106);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static void insertPokemon(SQLiteDatabase db, String species, int captureRate, int baseHP) {
        ContentValues pokemonValues = new ContentValues();
        pokemonValues.put("SPECIES", species);
        pokemonValues.put("CAPTURE_RATE", captureRate);
        pokemonValues.put("BASE_HP", baseHP);
        db.insert("POKEMON", null, pokemonValues);
    }

    public List<String> getAllLabels() {
        List<String> labels = new ArrayList<>();
        //String query = "SELECT SPECIES FROM POKEMON ORDER BY SPECIES ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("POKEMON", new String[]{"SPECIES"}, null, null, null, null, "SPECIES ASC");
        //Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return labels;
    }
}
