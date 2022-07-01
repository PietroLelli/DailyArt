package com.example.dailyart.ArtWorkRecyclerView;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;


import com.example.dailyart.MuseumRecyclerView.OnItemListenerMuseum;
import com.example.dailyart.R;

import java.util.ArrayList;
import java.util.List;

public class ArtworkCardAdapter extends RecyclerView.Adapter<ArtWorkCardViewHolder> {

    List<ArtWork> artWorkList;
    Activity activity;
    private OnItemListenerArtWork listener;

    public ArtworkCardAdapter(OnItemListenerArtWork listener, List<ArtWork> artWorkList, Activity activity) {
        this.listener = listener;
        this.artWorkList = artWorkList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ArtWorkCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_layout_artwork, parent,false);
        return new ArtWorkCardViewHolder(layoutView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtWorkCardViewHolder holder, int position) {
        ArtWork currentCardItem = artWorkList.get(position);
        String imagePath = currentCardItem.getImage();
        Drawable drawable = AppCompatResources.getDrawable(activity, activity.getResources()
                .getIdentifier(imagePath, "drawable", activity.getPackageName()));
        holder.imgArtwork.setImageDrawable(drawable);
        holder.tvMuseumCityArtwork.setText(currentCardItem.getMuseum());
        holder.tvArtwork.setText(currentCardItem.getName());
    }

    public ArtWork getArtWorkSelected(int position){
        return artWorkList.get(position);
    }

    @Override
    public int getItemCount() {
        return artWorkList.size();
    }

}
