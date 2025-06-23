package com.zendalona.zmantra.utility;

import java.util.Random;
import com.zendalona.zmantra.Enum.Difficulty;

public class RandomValueGenerator {
    private Random random;
    private final int NO_OF_TOPICS = 4;

    public RandomValueGenerator() {
        this.random = new Random();
    }

    public boolean generateNumberLineQuestion(){
        return random.nextBoolean();
    }

    public int generateQuestionTopic(){
        return random.nextInt(NO_OF_TOPICS);
    }

    public int generateNumberForCountGame() {
        return random.nextInt(16) + 2;
    }

    public int[] generateNumberRangeForCount(int upperBound){
        int start = random.nextInt(upperBound - 10);
        int end = random.nextInt(11) + start + 6;
        return new int[]{start, end};
    }

    public int[] generateAdditionValues(String difficulty) {
        int[] values = new int[3];
        switch (difficulty) {
            case "easy":
                values[0] = random.nextInt(10) + 1;
                values[1] = random.nextInt(10) + 1;
                break;
            case "medium":
                values[0] = random.nextInt(50) + 10;
                values[1] = random.nextInt(50) + 10;
                break;
            case "hard":
                values[0] = random.nextInt(500) + 17;
                values[1] = random.nextInt(500) + 17;
                break;
        }
        values[2] = values[0] + values[1];
        return values;
    }

    public int[] generateSubtractionValues() {
        int[] values = new int[3];
        values[0] = random.nextInt(5) + 1;
        values[1] = random.nextInt(10) + 1;
        values[2] = values[0] - values[1];
        return values;
    }

    public int[] generateMultiplicationValues(Difficulty difficulty) {
        int[] values = new int[3];
        values[0] = random.nextInt(10) + 1;
        values[1] = random.nextInt(10) + 1;
        values[2] = values[0] * values[1];
        return values;
    }

    public int[] generateDivisionValues(Difficulty difficulty) {
        int[] values = new int[3];
        values[1] = random.nextInt(9) + 1; // Avoid division by zero
        values[0] = values[1] * (random.nextInt(10) + 1);
        values[2] = values[0] / values[1];
        return values;
    }

    public float generateRandomDegree() {
        return random.nextInt(360); // Random number between 0 - 360
    }
    public int generateNumberBetween(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

}