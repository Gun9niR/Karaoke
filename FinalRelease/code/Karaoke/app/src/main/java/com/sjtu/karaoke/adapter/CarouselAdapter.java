package com.sjtu.karaoke.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sjtu.karaoke.R;

import java.util.List;

/*
 * @ClassName: CarouselAdapter
 * @Author: 郭志东
 * @Date: 2021/3/28
 * @Description: 主界面的走马灯适配器。根据构造时传入的图片参数设置走马灯中显示的内容。
 */
public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.SliderViewHolder>{
    private List<Integer> carouselImages;
    private ViewPager2 viewPager2;

    public CarouselAdapter(List<Integer> carouselImages, ViewPager2 viewPager2) {
        this.carouselImages = carouselImages;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.carousel_item_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.setImage(carouselImages.get(position));
        if (position == carouselImages.size() - 2) {
            viewPager2.post(runnable);
        }
    }

    @Override
    public int getItemCount() {
        return carouselImages.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imageView;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carouselImage);
        }

        void setImage(Integer carouselImage) {
            imageView.setImageResource(carouselImage);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            carouselImages.addAll(carouselImages);
            notifyDataSetChanged();
        }
    };
}
