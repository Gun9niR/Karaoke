package com.sjtu.karaoke;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
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
 * @Version: v1.3
 * @Description: 设置界面。本类中保存了设置项名称，并调用SettingAdapter来设置设置列表中显示的内容及点击事件。
 */

public class SettingActivity extends AppCompatActivity {
    private String[] settingItems = {"使用帮助", "清空本地缓存", "关于天天爱K歌"};

    /**
     * 捕获用户点击返回主页按钮的事件，调用onBackPressed()，否则返回时会返回到歌曲浏览界面
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

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
                // 显示使用帮助
                case 0:
                    MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR());
                    dialog.title(null, "使用帮助");
                    dialog.message(null, getString(R.string.help), null);
                    dialog.positiveButton(null, "我知道了", null);
                    dialog.show();
                    break;
                // 清空所有临时目录，并计算清除的文件大小
                case 1:
                    long bytes = 0;

                    List<File> dirs = new ArrayList<>();
                    // 删除伴奏演唱模式的伴奏
                    dirs.add(new File(ACCOMPANY_DIRECTORY));
                    // 删除专辑封面
                    dirs.add(new File(ALBUM_COVER_DIRECTORY));
                    // 删除和弦文件
                    dirs.add(new File(CHORD_TRANS_DIRECTORY));
                    // 删除自弹自唱模式的伴奏
                    dirs.add(new File(BASS_DIRECTORY));
                    dirs.add(new File(DRUM_DIRECTORY));
                    dirs.add(new File(ORCHESTRA_DIRECTORY));
                    // 删除自弹自唱模式的歌词
                    dirs.add(new File(LYRIC_INSTRUMENT_DIRECTORY));
                    // 删除伴奏演唱模式的歌词
                    dirs.add(new File(LYRIC_DIRECTORY));
                    // 删除MV
                    dirs.add(new File(MV_DIRECTORY));
                    // 删除原唱
                    dirs.add(new File(ORIGINAL_DIRECTORY));
                    // 删除录音pcm文件
                    dirs.add(new File(PCM_DIRECTORY));
                    // 删除打分系统临时文件
                    dirs.add(new File(RATERDATA_DIRECTORY));
                    // 删除打分文件
                    dirs.add(new File(RATE_DIRECTORY));
                    // 删除自弹自唱模式的临时文件
                    dirs.add(new File(ASSET_DIRECTORY));
                    dirs.add(new File(CHORD_WAV_DIRECTORY));
                    dirs.add(new File(USER_PLAY_DIRECTORY));
                    // 删除用户录音临时文件
                    dirs.add(new File(VOICE_DIRECTORY));

                    for (File dir: dirs) {
                        if (dir.exists()) {
                            for (File file: Objects.requireNonNull(dir.listFiles())) {
                                bytes += file.length();
                                file.delete();
                            }
                        }
                    }

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
                            })
                            .setNegativeButtonClick(() -> {
                            })
                            .show();
                    break;
                case 2:
                    // 显示关于信息
                    new AwesomeSuccessDialog(this)
                            .setTitle("关于")
                            .setMessage(getString(R.string.about))
                            .setColoredCircle(R.color.purple_500)
                            .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                            .setCancelable(true)
                            .setPositiveButtonText("确认")
                            .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                            .setPositiveButtonTextColor(R.color.white)
                            .setPositiveButtonClick(() -> {
                            })
                            .setNegativeButtonClick(() -> {
                            })
                            .show();
                    break;
            }
        });
    }
}