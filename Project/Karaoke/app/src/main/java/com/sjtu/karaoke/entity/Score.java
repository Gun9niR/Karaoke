package com.sjtu.karaoke.entity;

public class Score {
    Integer totalScore;
    Integer accuracyScore;
    Integer emotionScore;
    Integer breathScore;

    public Score() {
        totalScore = 0;
        accuracyScore = 0;
        emotionScore = 0;
        breathScore = 0;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public Integer getAccuracyScore() {
        return accuracyScore;
    }

    public Integer getEmotionScore() {
        return emotionScore;
    }

    public Integer getBreathScore() {
        return breathScore;
    }

    public void update(Integer[] scores) {
        totalScore += scores[0];
        accuracyScore += scores[1];
        emotionScore += scores[2];
        breathScore += scores[3];
    }
}
