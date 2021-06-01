package com.sjtu.karaoke.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Score implements Parcelable {
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

    protected Score(Parcel in) {
        if (in.readByte() == 0) {
            totalScore = null;
        } else {
            totalScore = in.readInt();
        }
        if (in.readByte() == 0) {
            accuracyScore = null;
        } else {
            accuracyScore = in.readInt();
        }
        if (in.readByte() == 0) {
            emotionScore = null;
        } else {
            emotionScore = in.readInt();
        }
        if (in.readByte() == 0) {
            breathScore = null;
        } else {
            breathScore = in.readInt();
        }
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
        totalScore += scores[0];
        accuracyScore += scores[1];
        emotionScore += scores[2];
        breathScore += scores[3];
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
    }
}
