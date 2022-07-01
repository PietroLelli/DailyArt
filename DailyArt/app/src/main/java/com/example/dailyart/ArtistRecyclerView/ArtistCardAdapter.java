package com.example.dailyart.ArtistRecyclerView;

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

public class ArtistCardAdapter extends RecyclerView.Adapter<ArtistCardViewHolder> {

        List<Artist> artistList;
        Activity activity;

        private OnItemListenerArtist listener;

        public ArtistCardAdapter(OnItemListenerArtist listener, List<Artist> artistList, Activity activity) {
            this.listener = listener;
            this.artistList = artistList;
            this.activity = activity;
        }

        @NonNull
        @Override
        public ArtistCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layoutView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.card_layout_artist, parent,false);
            return new ArtistCardViewHolder(layoutView, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull ArtistCardViewHolder holder, int position) {
            Artist currentCardItem = artistList.get(position);
            String imagePath = currentCardItem.getImage();
            Drawable drawable = AppCompatResources.getDrawable(activity, activity.getResources()
                    .getIdentifier(imagePath, "drawable", activity.getPackageName()));
            holder.imgArtist.setImageDrawable(drawable);
            holder.tvArtistName.setText(currentCardItem.getName());
        }

        public Artist getArtistSelected(int position){
            return artistList.get(position);
        }

        @Override
        public int getItemCount() {
            return artistList.size();
        }
}
