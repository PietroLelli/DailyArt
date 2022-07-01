package com.example.dailyart.MuseumRecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyart.R;

public class MuseumCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    ImageView imgMuseum;
    TextView tvMuseum;
    TextView tvCity;

    private OnItemListenerMuseum itemListener;

    public MuseumCardViewHolder(@NonNull View itemView, OnItemListenerMuseum listener) {
        super(itemView);
        imgMuseum = itemView.findViewById(R.id.imgMuseum);
        tvMuseum = itemView.findViewById(R.id.tvUserReview);
        tvCity = itemView. findViewById(R.id.tvReviewText);
        itemListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemListener.onItemClickMuseum(getAdapterPosition());
    }
}
