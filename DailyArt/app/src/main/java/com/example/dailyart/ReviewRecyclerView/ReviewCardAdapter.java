package com.example.dailyart.ReviewRecyclerView;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyart.ArtistRecyclerView.Artist;
import com.example.dailyart.R;

import java.util.List;

public class ReviewCardAdapter extends RecyclerView.Adapter<ReviewCardViewHolder>{

    List<Review> reviewList;
    Activity activity;

    public ReviewCardAdapter(List<Review> reviewList, Activity activity) {
        this.reviewList = reviewList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ReviewCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_layout_review, parent,false);
        return new ReviewCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewCardViewHolder holder, int position) {
        Review currentCardItem = reviewList.get(position);
        holder.ratingBar.setRating(currentCardItem.getRating());
        holder.tvUsername.setText(currentCardItem.getUsername());
        holder.tvDescription.setText(currentCardItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

}
