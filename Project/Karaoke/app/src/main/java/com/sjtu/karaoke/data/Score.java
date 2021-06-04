package com.sjtu.karaoke.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Score implements Parcelable {
    Integer totalScore;
    Integer accuracyScore;
    Integer emotionScore;
    Integer breathScore;
    Integer numOfUpdate;

    public Score() {
        totalScore = 0;
        accuracyScore = 0;
        emotionScore = 0;
        breathScore = 0;
        numOfUpdate = 0;
    }

    protected Score(Parcel in) {
        setTotalScore(in.readInt());
        setAccuracyScore(in.readInt());
        setEmotionScore(in.readInt());
        setBreathScore(in.readInt());
        setNumOfUpdate(in.readInt());
    }

    public static final Creator<Score> CREATOR = new Creator<Score>() {
        @Override
        public Score createFromParcel(Parcel in) {
            return new Score(in);
        }

        @Override
        public Score[] newArray(int size) {
            return new Score[size];
        }
    };

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
        ++numOfUpdate;
        totalScore += scores[0];
        accuracyScore += scores[1];
        emotionScore += scores[2];
        breathScore += scores[3];
    }

    public void computeFinalScore() {
        if (numOfUpdate != 0) {
            accuracyScore /= numOfUpdate;
            emotionScore /= numOfUpdate;
            breathScore /= numOfUpdate;
        }
    }

    public int getPercentageScore() {
        if (numOfUpdate != 0) {
            return totalScore / numOfUpdate;
        } else {
            return 0;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(totalScore);
        dest.writeInt(accuracyScore);
        dest.writeInt(emotionScore);
        dest.writeInt(breathScore);
        dest.writeInt(numOfUpdate);
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public void setAccuracyScore(Integer accuracyScore) {
        this.accuracyScore = accuracyScore;
    }

    public void setEmotionScore(Integer emotionScore) {
        this.emotionScore = emotionScore;
    }

    public void setBreathScore(Integer breathScore) {
        this.breathScore = breathScore;
    }

    public void setNumOfUpdate(Integer numOfUpdate) {
        this.numOfUpdate = numOfUpdate;
    }
}
