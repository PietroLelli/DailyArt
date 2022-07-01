package com.example.dailyart.ArtWorkRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyart.R;

public class ArtWorkCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    ImageView imgArtwork;
    TextView tvArtwork;
    TextView tvMuseumCityArtwork;

    private OnItemListenerArtWork itemListener;

    public ArtWorkCardViewHolder(@NonNull View itemView, OnItemListenerArtWork listener) {
        super(itemView);
        imgArtwork = itemView.findViewById(R.id.imgArtist);
        tvArtwork = itemView.findViewById(R.id.tvArtistName);
        tvMuseumCityArtwork = itemView.findViewById(R.id.tvMuseumCityArtwork);
        itemListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemListener.onItemClickArtWork(getAdapterPosition());
    }
}
