package com.example.jacob.pokemoncapturecalculator;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


/**
 *
 */
public class Gen1Fragment extends Fragment implements View.OnClickListener {

    private NumberPicker levelPicker;
    private SeekBar hpPicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gen1, container, false);
        Button button = (Button) view.findViewById(R.id.gen1submit);
        button.setOnClickListener(this);
        Spinner speciesPicker = (Spinner) view.findViewById(R.id.gen1species);
        try {
            PokemonDatabaseHelper dbHelper = new PokemonDatabaseHelper(view.getContext());
            List<String> species = dbHelper.getAllLabels();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, species);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            speciesPicker.setAdapter(adapter);
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(view.getContext(), "Could not access database", Toast.LENGTH_SHORT);
            toast.show();
        }
        levelPicker = (NumberPicker) view.findViewById(R.id.gen1level);
        levelPicker.setMaxValue(100);
        levelPicker.setMinValue(1);
        levelPicker.setWrapSelectorWheel(false);
        hpPicker = (SeekBar) view.findViewById(R.id.gen1hp);
        hpPicker.setProgress(100);
        return view;
    }

    @Override
    public void onClick(View v) {
        View view = (View) v.getParent();
        if (view != null) {
            Spinner spinner = (Spinner) view.findViewById(R.id.gen1species);
            String species = String.valueOf(spinner.getSelectedItem());
            Pokemon pokemon = new Pokemon();
            try {
                SQLiteOpenHelper dbHelper = new PokemonDatabaseHelper(view.getContext());
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query("POKEMON", new String[]{"CAPTURE_RATE", "BASE_HP"},
                        "_id = ?", new String[]{species}, null, null, null);
                if (cursor.moveToFirst()) {
                    pokemon.captureRate = cursor.getInt(0);
                    pokemon.baseHP = cursor.getInt(1);
                }
                cursor.close();
                db.close();
            } catch (SQLiteException e) {
                Toast toast = Toast.makeText(view.getContext(), "Could not access database", Toast.LENGTH_SHORT);
                toast.show();
            }
            spinner = (Spinner) view.findViewById(R.id.gen1status);
            Status status;
            String statusName = String.valueOf(spinner.getSelectedItem());
            switch (statusName) {
                case "None":
                    status = Status.NONE;
                    break;
                case "Asleep":
                    status = Status.SLEEP;
                    break;
                case "Burned":
                    status = Status.BURN;
                    break;
                case "Frozen":
                    status = Status.FROZEN;
                    break;
                case "Paralyzed":
                    status = Status.PARALYZED;
                    break;
                case "Poisoned":
                    status = Status.POISON;
                    break;
                default:
                    Toast toast = Toast.makeText(view.getContext(), "Bad value selected for status", Toast.LENGTH_SHORT);
                    toast.show();
                    status = Status.NONE;
                    break;
            }
            pokemon.level = levelPicker.getValue();
            spinner = (Spinner) view.findViewById(R.id.gen1ball);
            PokeBall ball;
            String ballName = String.valueOf(spinner.getSelectedItem());
            switch (ballName) {
                case "Poké Ball":
                    ball = PokeBall.POKE;
                    break;
                case "Great Ball":
                    ball = PokeBall.GREAT;
                    break;
                case "Ultra Ball":
                    ball = PokeBall.ULTRA;
                    break;
                case "Safari Ball":
                    ball = PokeBall.SAFARI;
                    break;
                case "Master Ball":
                    ball = PokeBall.MASTER;
                    break;
                default:
                    Toast toast = Toast.makeText(view.getContext(), "Bad value selected for ball type", Toast.LENGTH_SHORT);
                    toast.show();
                    ball = PokeBall.POKE;
                    break;
            }
            double hp = hpPicker.getProgress() / 100.0;
            double captureChance = chance(pokemon, ball, status, hp) * 100;
            String text = String.format("You have a %.2f%% chance of success", captureChance);
            TextView textView = (TextView) view.findViewById(R.id.gen1text);
            textView.setText(text);
        }
    }

    /**
     * Calculates the chance of capturing a Pokemon in the first-generation games (Red, Blue, Yellow)
     *
     * @param pokemon    The target Pokemon, containing relevant information about the species
     * @param ball       The type of Poke Ball used
     * @param status     The target Pokemon's current status condition, if any
     * @param fractionHP The fraction of health the target Pokemon has left, ranging from 0 to 1
     * @return The final capture chance, ranging from 0 to 1
     */
    private double chance(Pokemon pokemon, PokeBall ball, Status status, double fractionHP) {
        if (ball == PokeBall.MASTER) return 1.0;

        double chance;
        int ballMod;
        int maxHP = 0;
        for (int i = 0; i < 16; i++) {
            maxHP += (((pokemon.baseHP + i) * 2 * pokemon.level) / 100) + pokemon.level + 10;
        }
        maxHP /= 16;
        int currentHP = (int) (fractionHP * maxHP);
        int hpFactor = maxHP * 255;
        switch (ball) {
            case ULTRA:
            case SAFARI:
                ballMod = 151;
                hpFactor /= 12;
                break;
            case GREAT:
                ballMod = 201;
                hpFactor /= 8;
                break;
            case POKE:
            default:
                ballMod = 256;
                hpFactor /= 12;
                break;
        }
        if ((currentHP / 4) > 1)
            hpFactor /= (currentHP / 4);
        if (hpFactor > 255)
            hpFactor = 255; // Cap hpFactor at 255
        int statusMod;
        switch (status) {
            case SLEEP:
            case FROZEN:
                statusMod = 25;
                break;
            case PARALYZED:
            case BURN:
            case POISON:
                statusMod = 12;
                break;
            case NONE:
            default:
                statusMod = 0;
                break;
        }
        chance = (hpFactor + 1) / 256.0;
        if (pokemon.captureRate + 1 < ballMod - statusMod) {
            chance *= pokemon.captureRate + 1;
        } else {
            chance *= ballMod - statusMod;
        }
        chance /= ballMod;
        chance += (double) (statusMod) / ballMod;

        return chance;
    }
}
