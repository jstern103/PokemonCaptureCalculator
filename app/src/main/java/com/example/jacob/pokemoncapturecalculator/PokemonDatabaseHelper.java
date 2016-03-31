package com.example.jacob.pokemoncapturecalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
}
