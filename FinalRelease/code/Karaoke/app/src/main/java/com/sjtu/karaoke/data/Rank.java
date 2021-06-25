package com.sjtu.karaoke.data;

import androidx.core.content.res.ResourcesCompat;

import com.sjtu.karaoke.Karaoke;
import com.sjtu.karaoke.R;

import java.util.HashMap;

/*
 * @ClassName: Rank
 * @Author: 郭志东
 * @Date: 2021/6/5
 * @Description: 演唱等级类，封装演唱结果的显示（文字和颜色），由于只有固定的几个等级，构造函数为私有
 */
public class Rank {
    final String rankingText;
    final int RankingColor;

    private Rank(String rankingText, int rankingColor) {
        this.rankingText = rankingText;
        RankingColor = rankingColor;
    }

    public String getRankingText() {
        return rankingText;
    }

    public int getRankingColor() {
        return RankingColor;
    }

    public final static Rank SSS = new Rank("SSS",
            ResourcesCompat.getColor(Karaoke.getRes(), R.color.golden, null));
    public final static Rank SS = new Rank("SS",
            ResourcesCompat.getColor(Karaoke.getRes(), R.color.golden, null));
    public final static Rank S = new Rank("S",
            ResourcesCompat.getColor(Karaoke.getRes(), R.color.golden, null));
    public final static Rank A = new Rank("A",
            ResourcesCompat.getColor(Karaoke.getRes(), R.color.coral, null));
    public final static Rank B = new Rank("B",
            ResourcesCompat.getColor(Karaoke.getRes(), R.color.blue, null));
    public final static Rank C = new Rank("C",
            ResourcesCompat.getColor(Karaoke.getRes(), R.color.green, null));
    public final static Rank D = new Rank("D",
            ResourcesCompat.getColor(Karaoke.getRes(), R.color.gray, null));

    public final static HashMap<String, Rank> strToRank = new HashMap<String, Rank>() {
        {
            put("SSS", SSS);
            put("SS", SS);
            put("S", S);
            put("A", A);
            put("B", B);
            put("C", C);
            put("D", D);
        }
    };
}
