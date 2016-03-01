package com.example.jacob.pokemoncapturecalculator;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


/**
 *
 */
public class Gen1Fragment extends Fragment implements View.OnClickListener, NumberPicker.OnValueChangeListener {

    private int level;

    public Gen1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gen1, container, false);
        Button button = (Button) view.findViewById(R.id.gen1submit);
        button.setOnClickListener(this);
        NumberPicker levelPicker = (NumberPicker) view.findViewById(R.id.gen1level);
        levelPicker.setMaxValue(100);
        levelPicker.setMinValue(1);
        levelPicker.setWrapSelectorWheel(false);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            Spinner spinner = (Spinner) v.findViewById(R.id.gen1spinner);
            String species = String.valueOf(spinner.getSelectedItem());
            Pokemon pokemon = new Pokemon();
            switch (species) {
                case "Bulbasaur":
                    pokemon.baseHP = 45;
                    pokemon.captureRate = 45;
                    //break;
                case "Caterpie":
                    pokemon.baseHP = 45;
                    pokemon.captureRate = 255;
                    //break;
                case "Pikachu":
                    pokemon.baseHP = 35;
                    pokemon.captureRate = 190;
                    //break;
                case "Mewtwo":
                    pokemon.baseHP = 106;
                    pokemon.captureRate = 3;
                    //break;
                default:
                    Toast toast = Toast.makeText(v.getContext(), "Bad value returned by spinner", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
            }
            pokemon.level = level;
            double captureChance = chance(pokemon, PokeBall.POKE, Status.NONE, 1.0) * 100;
            String text = String.format("You have a %.2f%% chance of success", captureChance);
            TextView textView = (TextView) v.findViewById(R.id.gen1text);
            textView.setText(text);
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        level = newVal;
    }

    /**
     * Calculates the chance of capturing a com.example.jacob.pokemoncapturecalculator.Pokemon in the first-generation games (Red, Blue, Yellow)
     *
     * @param pokemon    The target com.example.jacob.pokemoncapturecalculator.Pokemon, containing relevant information about the species
     * @param ball       The type of Poke Ball used
     * @param status     The target com.example.jacob.pokemoncapturecalculator.Pokemon's current status condition, if any
     * @param fractionHP The fraction of health the target com.example.jacob.pokemoncapturecalculator.Pokemon has left, ranging from 0 to 1
     * @return The final capture chance, ranging from 0 to 1
     */
    private double chance(Pokemon pokemon, PokeBall ball, Status status, double fractionHP) {
        int chance;
        int ballMod;
        int maxHP = (((pokemon.baseHP + 8) * 2 * pokemon.level) / 100) + pokemon.level + 10;
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
        chance = (hpFactor + 1) / 256;
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
