package com.sjtu.karaoke;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.sjtu.karaoke.adapter.SettingAdapter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.sjtu.karaoke.util.Constants.ACCOMPANY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ALBUM_COVER_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ASSET_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.BASS_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.CHORD_TRANS_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.CHORD_WAV_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.DRUM_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.LYRIC_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.LYRIC_INSTRUMENT_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.MV_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ORCHESTRA_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.ORIGINAL_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.PCM_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.RATERDATA_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.RATE_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.USER_PLAY_DIRECTORY;
import static com.sjtu.karaoke.util.Constants.VOICE_DIRECTORY;

/*
 * @ClassName: SettingActivity
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.2
 * @Description: 设置界面。本类中保存了设置项名称，并调用SettingAdapter来设置设置列表中显示的内容及点击事件。
 */

public class SettingActivity extends AppCompatActivity {
    private String[] settingItems = {"清空本地缓存", "关于"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // add back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSetting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ListView listView = findViewById(R.id.listSetting);
        SettingAdapter listAdapter = new SettingAdapter(getApplicationContext(), settingItems);
        listView.setAdapter(listAdapter);
        listView.setFooterDividersEnabled(true);
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            switch(i) {
                case 0:
                    long bytes = 0;

                    List<File> dirs = new ArrayList<>();
                    // delete accompany
                    dirs.add(new File(ACCOMPANY_DIRECTORY));
                    // delete album cover
                    dirs.add(new File(ALBUM_COVER_DIRECTORY));
                    // delete chord
                    dirs.add(new File(CHORD_TRANS_DIRECTORY));
                    // delete i_accompany
                    dirs.add(new File(BASS_DIRECTORY));
                    dirs.add(new File(DRUM_DIRECTORY));
                    dirs.add(new File(ORCHESTRA_DIRECTORY));
                    // delete i_lyric
                    dirs.add(new File(LYRIC_INSTRUMENT_DIRECTORY));
                    // delete lyric
                    dirs.add(new File(LYRIC_DIRECTORY));
                    // delete mv
                    dirs.add(new File(MV_DIRECTORY));
                    // delete original
                    dirs.add(new File(ORIGINAL_DIRECTORY));
                    // delete pcm
                    dirs.add(new File(PCM_DIRECTORY));
                    // delete raterdata
                    dirs.add(new File(RATERDATA_DIRECTORY));
                    // delete rating
                    dirs.add(new File(RATE_DIRECTORY));
                    // delete temporary
                    dirs.add(new File(ASSET_DIRECTORY));
                    dirs.add(new File(CHORD_WAV_DIRECTORY));
                    dirs.add(new File(USER_PLAY_DIRECTORY));
                    // delete wav
                    dirs.add(new File(VOICE_DIRECTORY));

                    for (File dir: dirs) {
                        if (dir.exists()) {
                            for (File file: Objects.requireNonNull(dir.listFiles())) {
                                bytes += file.length();
                                file.delete();
                            }
                        }
                    }

                    System.out.println(FileUtils.byteCountToDisplaySize(bytes));

                    new AwesomeSuccessDialog(this)
                            .setTitle("删除成功")
                            .setMessage(FileUtils.byteCountToDisplaySize(bytes) + "手机内存已释放")
                            .setColoredCircle(R.color.purple_500)
                            .setDialogIconAndColor(R.drawable.ic_success, R.color.white)
                            .setCancelable(true)
                            .setPositiveButtonText("确认")
                            .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                            .setPositiveButtonTextColor(R.color.white)
                            .setPositiveButtonClick(() -> {
                                //click
                            })
                            .setNegativeButtonClick(() -> {
                                //click
                            })
                            .show();
                    break;
                case 1:
                    Dialog aboutDialog = new Dialog(this);
                    break;
            }
        });
    }
}