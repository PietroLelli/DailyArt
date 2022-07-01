package com.example.dailyart.MuseumRecyclerView;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyart.R;

import java.util.List;

public class AllMuseumCardAdapter extends RecyclerView.Adapter<MuseumCardViewHolder> {

    List<Museum> museumList;
    Activity activity;
    private OnItemListenerMuseum listener;

    public AllMuseumCardAdapter(OnItemListenerMuseum listener, List<Museum> museumList, Activity activity) {
        this.listener = listener;
        this.activity = activity;
        this.museumList = museumList;
    }

    @NonNull
    @Override
    public MuseumCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_layout_all_museum, parent,false);
        return new MuseumCardViewHolder(layoutView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MuseumCardViewHolder holder, int position) {
        Museum currentCardItem = museumList.get(position);
        String imagePath = currentCardItem.getImage();
        Drawable drawable = AppCompatResources.getDrawable(activity, activity.getResources()
                .getIdentifier(imagePath, "drawable", activity.getPackageName()));
        holder.imgMuseum.setImageDrawable(drawable);
        holder.tvMuseum.setText(currentCardItem.getName());
        holder.tvCity.setText(currentCardItem.getCity());
    }

    public Museum getMuseumSelected(int position){
        return museumList.get(position);
    }

    @Override
    public int getItemCount() {
        return museumList.size();
    }
}
