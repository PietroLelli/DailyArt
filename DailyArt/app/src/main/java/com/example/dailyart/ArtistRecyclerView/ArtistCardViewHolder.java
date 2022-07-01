package com.example.dailyart.ArtistRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyart.R;

public class ArtistCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    ImageView imgArtist;
    TextView tvArtistName;

    private OnItemListenerArtist itemListener;

    public ArtistCardViewHolder(@NonNull View itemView, OnItemListenerArtist listener) {
        super(itemView);
        imgArtist = itemView.findViewById(R.id.imgArtist);
        tvArtistName = itemView.findViewById(R.id.tvArtistName);
        itemListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemListener.onItemClickArtist(getAdapterPosition());
    }
}
