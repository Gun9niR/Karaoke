package com.sjtu.karaoke.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.sjtu.karaoke.R;
import com.sjtu.karaoke.SearchActivity;
import com.sjtu.karaoke.adapter.CarouselAdapter;
import com.sjtu.karaoke.adapter.SongListAdapter;
import com.sjtu.karaoke.data.SongInfo;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.sjtu.karaoke.util.MiscUtil.getSongInfo;
import static com.sjtu.karaoke.util.MiscUtil.showWarningToast;

/*
 * @ClassName: ViewSongsFragment
 * @Author: guozh
 * @Date: 2021/3/28
 * @Version: v1.3
 * @Description: 主界面(MainActivity)中“点歌”界面。本类用于初始化界面中的各个组件，包括：
 *                  1. 初始化各个组件
 *                  2. 调用CarouselAdapter设置走马灯中的显示内容、点击事件
 *                  3. 调用SongListAdapter设置“猜你喜欢”歌曲列表中的显示内容、点击事件
 */

public class ViewSongsFragment extends Fragment {
    View view;
    Activity activity;

    // 走马灯
    private ViewPager2 carousel;
    // 调用走马灯切换图片的handler
    private final Handler carouselHandler = new Handler();
    // 使走马灯切换图片的runnable
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            carousel.setCurrentItem(carousel.getCurrentItem() + 1);
        }
    };

    // 使歌曲列面可以滚动
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;

    // 歌曲列表
    private RecyclerView songRecyclerView;
    // 歌曲列表适配器
    private SongListAdapter adapter;
    // 传给歌曲列表的数据
    private List<SongInfo> songList;
    // 图片切换间隔
    private static final int CAROUSEL_INTERVAL = 3000;

    public ViewSongsFragment() { }

    public static ViewSongsFragment newInstance(String param1, String param2) {
        return new ViewSongsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.activity = getActivity();
    }

    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_view_songs, container, false);

        // find the carousel
        carousel = (ViewPager2) view.findViewById(R.id.carousel);

        initSwipeRefreshLayout();

        initSongRecyclerView();

        initCarousel();

        // set up toolbar
        initToolbar();

        return view;
    }

    private void initSongRecyclerView() {
        songRecyclerView = view.findViewById(R.id.songList);
        songList = new ArrayList<>();
        adapter = new SongListAdapter(songList);
        songRecyclerView.setAdapter(adapter);
        refreshListener.onRefresh();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        songRecyclerView.setLayoutManager(layoutManager);
        songRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setSongs(List<SongInfo> songs) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                adapter.setSongs(songs);
                // 不调用这个方法，列表不会更新
                adapter.notifyDataSetChanged();
            });
        }
    }

    private void initToolbar() {
        Toolbar toolbar = view.findViewById(R.id.toolbarViewSongs);
        // 只可能跳转到搜索界面
        toolbar.setOnMenuItemClickListener(item -> {
            // 直接讲歌曲列表通过intent传给搜索界面，这样搜索界面就不需要再从服务器请求一次数据
            Intent intent = new Intent(activity, SearchActivity.class);
            ArrayList<SongInfo> songs = new ArrayList<>(songList);
            intent.putParcelableArrayListExtra("songList", songs);
            startActivity(intent);
            return true;
        });
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout = view.findViewById(R.id.homepageSwipe);
        refreshListener = () -> getSongInfo(new Callback() {

            // 只要status code不以2开头就失败
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if (activity != null) {
                    showWarningToast(activity, getString(R.string.download_fail_hint));
                }
                // 取消刷新状态，否则列表会一直处在刷新状态，无法点击
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonString = response.body().string();
                    JSONArray jsonArray = new JSONArray(jsonString);
                    int length = jsonArray.length();

                    songList = new ArrayList<>();
                    for (int i = 0; i < length; ++i) {
                        JSONObject songInfo = jsonArray.getJSONObject(i);
                        songList.add(new SongInfo(
                                (Integer) songInfo.get("id"),
                                (String) songInfo.get("song_name"),
                                (String) songInfo.get("singer")));
                    }
                    setSongs(songList);

                } catch (JSONException e) {
                    showWarningToast(activity, getString(R.string.download_fail_hint));
                    // 取消刷新状态，否则列表会一直处在刷新状态，无法点击
                    swipeRefreshLayout.setRefreshing(false);
                    e.printStackTrace();
                }
                // 取消刷新状态，否则列表会一直处在刷新状态，无法点击
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
    }

    /**
     * 初始化跑马灯
     */
    private void initCarousel() {
        // 添加图片
        List<Integer> carouselImages = new ArrayList<>();
        carouselImages.add(R.drawable.carousel_sjzy);
        carouselImages.add(R.drawable.carousel_sn);
        carouselImages.add(R.drawable.carousel_xxy);

        // 设置适配器
        carousel.setAdapter(new CarouselAdapter(carouselImages, carousel));

        // 显示前一张和后一张图片
        carousel.setClipToPadding(false);
        carousel.setClipChildren(false);
        carousel.setOffscreenPageLimit(3);
        carousel.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        // 设置图片从两边到中间的大小变化
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        carousel.setPageTransformer(compositePageTransformer);

        // 设置滚动
        carousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                carouselHandler.removeCallbacks(sliderRunnable);
                carouselHandler.postDelayed(sliderRunnable, CAROUSEL_INTERVAL);
            }
        });
    }
}