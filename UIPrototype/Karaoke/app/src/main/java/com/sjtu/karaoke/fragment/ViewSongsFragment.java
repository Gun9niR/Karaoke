package com.sjtu.karaoke.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sjtu.karaoke.R;
import com.sjtu.karaoke.adapter.CarouselAdapter;
import com.sjtu.karaoke.adapter.SongListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewSongsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewSongsFragment extends Fragment {

    static final int CAROUSEL_INTERVAL = 3000;
    // carousel
    private ViewPager2 carousel;
    private final Handler carouselHandler = new Handler();
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            carousel.setCurrentItem(carousel.getCurrentItem() + 1);
        }
    };

    String[] songNames, singers;
    int[] images = { R.drawable.voice_notes, R.drawable.nine_track_mind, R.drawable.speak_now, R.drawable.speak_now };

    public ViewSongsFragment() { }

    public static ViewSongsFragment newInstance(String param1, String param2) {
        ViewSongsFragment fragment = new ViewSongsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songNames = getResources().getStringArray(R.array.song_names);
        singers = getResources().getStringArray(R.array.singers);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_songs, container, false);

        // find the carousel
        carousel = (ViewPager2) view.findViewById(R.id.carousel);

        // prepare list of images from drawable
        List<Integer> carouselImages = new ArrayList<>();
        carouselImages.add(R.drawable.carousel_attention);
        carouselImages.add(R.drawable.carousel_dangerously);
        carouselImages.add(R.drawable.carousel_back_to_december);
        // set adapter
        carousel.setAdapter(new CarouselAdapter(carouselImages, carousel));

        // set clipping effect
        carousel.setClipToPadding(false);
        carousel.setClipChildren(false);
        carousel.setOffscreenPageLimit(3);
        carousel.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        carousel.setPageTransformer(compositePageTransformer);

        carousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                carouselHandler.removeCallbacks(sliderRunnable);
                carouselHandler.postDelayed(sliderRunnable, CAROUSEL_INTERVAL);
            }
        });

        RecyclerView songList = (RecyclerView) view.findViewById(R.id.songList);

        SongListAdapter adapter = new SongListAdapter(songNames, singers, images);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        songList.setLayoutManager(layoutManager);
        songList.setAdapter(adapter);
        songList.setNestedScrollingEnabled(false);

        // set up toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbarViewSongs);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                
                return true;
            }
        });
        return view;
    }
}