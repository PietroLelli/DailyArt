package com.example.dailyart;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dailyart.ArtWorkRecyclerView.ArtWork;
import com.example.dailyart.MuseumRecyclerView.Museum;
import com.example.dailyart.ReviewRecyclerView.Review;
import com.example.dailyart.ReviewRecyclerView.ReviewCardAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllReview extends Fragment {

    ReviewCardAdapter adapterReview;
    RecyclerView recyclerReview;
    List<Review> reviewList = new ArrayList<>();

    DatabaseReference dbRef;

    Museum currentMuseum;
    ArtWork currentArtwork;

    public AllReview(Museum currentMuseum) {
        this.currentMuseum = currentMuseum;
    }

    public AllReview(ArtWork currentArtwork) {
        this.currentArtwork = currentArtwork;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        return inflater.inflate(R.layout.fragment_all_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (reviewList != null) {
            reviewList.clear();
        }
        recyclerReview = view.findViewById(R.id.recyclerReviewArtwork);
        recyclerReview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerReview.setHasFixedSize(true);
        adapterReview = new ReviewCardAdapter(reviewList, getActivity());
        recyclerReview.setAdapter(adapterReview);

        if(currentMuseum != null){
            dbRef.child("ReviewMuseum").child(currentMuseum.getName()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Review review = dataSnapshot.getValue(Review.class);
                        reviewList.add(review);
                    }
                    adapterReview.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else if(currentArtwork != null){
            dbRef.child("ReviewArtwork").child(currentArtwork.getName()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Review review = dataSnapshot.getValue(Review.class);
                        reviewList.add(review);
                    }
                    adapterReview.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }



    }
}