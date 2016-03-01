package com.example.jacob.pokemoncapturecalculator;

/**
 * Created by Jacob on 1/20/2016.
 */
public class Pokemon {
    /**
     * The base capture rate of the com.example.jacob.pokemoncapturecalculator.Pokemon's species, expressed as an integer ranging from 3 to 255
     */
    public int captureRate;
    /**
     * The current level of the com.example.jacob.pokemoncapturecalculator.Pokemon, ranging from 1 to 100
     */
    public int level;
    /**
     * The base HP value for the com.example.jacob.pokemoncapturecalculator.Pokemon's species, used to calculate the com.example.jacob.pokemoncapturecalculator.Pokemon's max health
     */
    public int baseHP;
    /**
     * The gender ratio for the com.example.jacob.pokemoncapturecalculator.Pokemon's species, expressed as an integer ranging from -1 to 8
     * A value of 0 indicates a 100% female species, while a value of 8 indicates a 100% male species
     * A value of -1 indicates a genderless species
     */
    public int genderRatio;
}