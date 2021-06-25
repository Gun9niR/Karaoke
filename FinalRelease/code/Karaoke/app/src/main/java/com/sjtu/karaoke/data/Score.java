package com.sjtu.karaoke.data;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * @ClassName: Score
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: 演唱打分类，包括了打分算法产生的各种分数。同时记录了分数的更新次数，便于计算每句的平均得分
 */
public class Score implements Parcelable {
    // 总分（加权得到）
    Integer totalScore;
    // 音准得分
    Integer accuracyScore;
    // 情感得分
    Integer emotionScore;
    // 气息得分
    Integer breathScore;
    // 分数更新的次数
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
