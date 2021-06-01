package com.sjtu.karaoke.component;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sjtu.karaoke.R;
import com.sjtu.karaoke.entity.Score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rorbin.q.radarview.RadarData;
import rorbin.q.radarview.RadarView;

public class RateResultDialog {

    Activity activity;
    Dialog dialog;

    TextView rankingTextView;
    TextView finalScoreTextView;
    RadarView radarView;

    public RateResultDialog(Activity activity, Score score, String instrumentScoreStr) {

        this.activity = activity;
        this.dialog = new Dialog(activity);

        dialog.setContentView(R.layout.dialog_rate_result);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        rankingTextView = dialog.findViewById(R.id.rateRanking);
        finalScoreTextView = dialog.findViewById(R.id.rateFinalScore);
        radarView = dialog.findViewById(R.id.rateResultRadar);

        setRankingText(activity, score);
        setFinalScoreText(score);
        setRadarChart(score, instrumentScoreStr);

    }

    public void show() { dialog.show(); }

    public void dismiss() { dialog.dismiss(); }

    private void setRankingText(Activity activity, Score score) {

        Integer finalScore = score.getTotalScore();
        String text;
        int color;

        int SColor = ContextCompat.getColor(activity.getApplicationContext(), R.color.golden);
        int AColor = ContextCompat.getColor(activity.getApplicationContext(), R.color.coral);
        int BColor = ContextCompat.getColor(activity.getApplicationContext(), R.color.blue);
        int CColor = ContextCompat.getColor(activity.getApplicationContext(), R.color.green);
        int DColor = ContextCompat.getColor(activity.getApplicationContext(), R.color.gray);

        if (finalScore >= 95) {
            text = "SSS";
            color = SColor;
        } else if (finalScore >= 90) {
            text = "SS";
            color = SColor;
        } else if (finalScore >= 85) {
            text = "S";
            color = SColor;
        } else if (finalScore >= 80) {
            text = "A";
            color = AColor;
        } else if (finalScore >= 70) {
            text = "B";
            color = BColor;
        } else if (finalScore >= 60) {
            text = "C";
            color = CColor;
        } else {
            text = "D";
            color = DColor;
        }

        rankingTextView.setText(text);
        rankingTextView.setTextColor(color);
    }

    private void setFinalScoreText(Score score) {
        Integer finalScore = score.getTotalScore();
        String text = "演唱得分: " + finalScore;
        finalScoreTextView.setText(text);
    }

    private void setRadarChart(Score score, String instrumentScoreStr) {

        List<Integer> layerColor = new ArrayList<>();
        Collections.addAll(layerColor, 0x2200bcd4, 0x2203a9f4, 0x225677fc, 0x223f51b5, 0x22673ab7);

        List<Float> scores = new ArrayList<>();
        Float accuracyScore = Float.valueOf(score.getAccuracyScore());
        Float emotionScore = Float.valueOf(score.getEmotionScore());
        Float breathScore = Float.valueOf(score.getBreathScore());
        Collections.addAll(scores, accuracyScore, emotionScore, breathScore);

        List<String> vertexText = new ArrayList<>();
        Collections.addAll(vertexText,
                "情感\n" + Math.round(emotionScore),
                "气息\n" + Math.round(breathScore),
                "音准\n" + Math.round(accuracyScore));

        if (!instrumentScoreStr.isEmpty()) {
            Float instrumentScore = Float.parseFloat(instrumentScoreStr) * 10f;
            vertexText.add(0, "弹奏\n" + Math.round(instrumentScore));
            scores.add(0, instrumentScore);
        }

        radarView.setMaxValue(100f);
        radarView.setLayerColor(layerColor);
        radarView.setVertexText(vertexText);

        RadarData scoreData = new RadarData(scores, 0xDD7E57C2);
        scoreData.setVauleTextColor(Color.WHITE);
        scoreData.setValueTextSize(20);
        scoreData.setLineWidth(1);
        radarView.addData(scoreData);

    }

}
