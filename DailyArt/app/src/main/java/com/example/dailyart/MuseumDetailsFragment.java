package com.example.dailyart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyart.ArtWorkRecyclerView.ArtWork;
import com.example.dailyart.ArtWorkRecyclerView.ArtworkCardAdapter;
import com.example.dailyart.ArtWorkRecyclerView.OnItemListenerArtWork;
import com.example.dailyart.MuseumRecyclerView.Museum;

import com.example.dailyart.ReviewRecyclerView.Review;
import com.example.dailyart.ReviewRecyclerView.ReviewCardAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

public class MuseumDetailsFragment extends Fragment implements OnItemListenerArtWork{

    private final Museum currentMuseum;

    private ImageView imageMuseum;
    private TextView museum;
    private TextView city;
    private ExpandableTextView description;
    private TextView address;
    private TextView telephone;
    private TextView website;
    private Button btnMuseumCollection;
    private Button btnPublishReview;
    private Button btnAllReview;
    private RatingBar rating;
    private TextView descriptionReview;
    private TextView noReview;

    private FloatingActionButton fabAdd;
    private FloatingActionButton fabFavourites;
    private FloatingActionButton fabVisited;

    static final int PERMISSION_CODE = 0;

    ArtworkCardAdapter adapterArtwork;
    RecyclerView recyclerArtwokMuseum;
    List<ArtWork> artworkList = new ArrayList<>();

    ReviewCardAdapter adapterReviewMuseum;
    RecyclerView recyclerReviewMuseum;
    List<Review> reviewMuseumList = new ArrayList<>();

    DatabaseReference dbRef;

    boolean visited = false;
    boolean favourite = false;
    private boolean clicked = false;

    public MuseumDetailsFragment(Museum museum){
        this.currentMuseum = museum;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        
        return inflater.inflate(R.layout.fragment_museum_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setVisited();
        setFavourite();

        imageMuseum = view.findViewById(R.id.imgMuseumDetails);
        museum = view.findViewById(R.id.tvMuseumName);
        city = view.findViewById(R.id.tvMuseumCity);
        description = view.findViewById(R.id.expand_text_view);
        address = view.findViewById(R.id.tvMuseumAddress);

        telephone = view.findViewById(R.id.tvMuseumTelephone);
        website = view.findViewById(R.id.tvMuseumWebsite);
        rating = view.findViewById(R.id.ratingBarReviewMuseum);
        descriptionReview = view.findViewById(R.id.tvReviewDescriptionMuseum);
        noReview = view.findViewById(R.id.tvNoReview);

        btnMuseumCollection = view.findViewById(R.id.btnMuseumCollection);
        btnMuseumCollection.setOnClickListener(v -> showCollection());
        btnPublishReview = view.findViewById(R.id.btnPublishReviewMuseum);
        btnPublishReview.setOnClickListener(view1 -> publishReview());
        btnAllReview= view.findViewById(R.id.btnShowAllReviewMuseum);
        btnAllReview.setOnClickListener(view2 -> openAllReview());

        fabAdd = view.findViewById(R.id.fab_add_museum);
        fabFavourites = view.findViewById(R.id.fab_favourites_museum);
        fabVisited = view.findViewById(R.id.fab_visited_museum);

        address.setOnClickListener(view13 -> {
            showOnMap();
        });

        telephone.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionsCall();
            } else {
                makeCall();
            }
        });

        website.setOnClickListener(view1 -> {
            if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionsInternet();
            } else {
                Utilities.insertFragment((AppCompatActivity) getActivity(), new WebViewFragment(website.getText().toString()), WebViewFragment.class.getSimpleName());
            }
        });

        dbRef.child("Museum").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentMuseum.getName())){
                    String imagePath = (snapshot.child(currentMuseum.getName()).child("image").getValue(String.class));
                    Drawable drawable = AppCompatResources.getDrawable(getContext(), getContext().getResources()
                            .getIdentifier(imagePath, "drawable", getActivity().getPackageName()));
                    imageMuseum.setImageDrawable(drawable);
                    museum.setText(snapshot.child(currentMuseum.getName()).child("name").getValue(String.class));
                    city.setText(snapshot.child(currentMuseum.getName()).child("city").getValue(String.class));
                    address.setText("   " + snapshot.child(currentMuseum.getName()).child("address").getValue(String.class));
                    telephone.setText("   " + snapshot.child(currentMuseum.getName()).child("telephone").getValue(String.class));
                    website.setText("   " + snapshot.child(currentMuseum.getName()).child("website").getValue(String.class));
                    description.setText(snapshot.child(currentMuseum.getName()).child("description").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (artworkList != null) {
            artworkList.clear();
        }
        recyclerArtwokMuseum = view.findViewById(R.id.recyclerReviewArtwork);
        recyclerArtwokMuseum.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerArtwokMuseum.setHasFixedSize(true);
        final OnItemListenerArtWork listenerArtWork = this;
        adapterArtwork = new ArtworkCardAdapter(listenerArtWork, artworkList, getActivity());
        recyclerArtwokMuseum.setAdapter(adapterArtwork);

        dbRef.child("ArtWork").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String getArtwork = dataSnapshot.child("museum").getValue(String.class);
                    if (getArtwork.equals(currentMuseum.getName())) {
                        if (count >= 5) {
                            continue;
                        }
                        ArtWork artWork = dataSnapshot.getValue(ArtWork.class);
                        artworkList.add(artWork);
                        count++;
                    }
                }
                adapterArtwork.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (reviewMuseumList != null) {
            reviewMuseumList.clear();
        }
        recyclerReviewMuseum = view.findViewById(R.id.recyclerReviewMuseum);
        recyclerReviewMuseum.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerReviewMuseum.setHasFixedSize(true);
        adapterReviewMuseum = new ReviewCardAdapter(reviewMuseumList, getActivity());
        recyclerReviewMuseum.setAdapter(adapterReviewMuseum);

        dbRef.child("ReviewMuseum").child(currentMuseum.getName()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Review review = dataSnapshot.getValue(Review.class);
                    reviewMuseumList.add(review);
                }
                adapterReviewMuseum.notifyDataSetChanged();
                if(reviewMuseumList.isEmpty()){
                    btnAllReview.setVisibility(View.INVISIBLE);
                    noReview.setVisibility(View.VISIBLE);
                }
                else{
                    btnAllReview.setVisibility(View.VISIBLE);
                    noReview.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!clicked){
                    fabVisited.setVisibility(View.VISIBLE);
                    fabFavourites.setVisibility(View.VISIBLE);
                    fabVisited.setClickable(true);
                    fabFavourites.setClickable(true);
                    clicked = true;

                    if(visited){
                        fabVisited.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#52D452")));
                    }
                    else{
                        fabVisited.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ef233c")));
                    }

                    if(favourite){
                        fabFavourites.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.baseline_favorite_white_48));
                        fabFavourites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#52D452")));

                    }
                    else{
                        fabFavourites.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.baseline_favorite_border_white_48));
                        fabFavourites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ef233c")));

                    }

                }else{
                    fabVisited.setVisibility(View.INVISIBLE);
                    fabFavourites.setVisibility(View.INVISIBLE);
                    fabVisited.setClickable(false);
                    fabFavourites.setClickable(false);
                    clicked = false;
                }
            }
        });

        fabFavourites.setOnClickListener(view12 -> {
            dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

            dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(!favourite){
                        dbRef.child("FavouriteMuseum").child(LoginActivity.loggedUser).child(currentMuseum.getName()).child("name").setValue(currentMuseum.getName());
                        Toast.makeText(getActivity(), "Aggiunto ai musei preferiti", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        dbRef.child("FavouriteMuseum").child(LoginActivity.loggedUser).child(currentMuseum.getName()).removeValue();
                        Toast.makeText(getActivity(), "Rimosso dai musei preferiti", Toast.LENGTH_SHORT).show();
                    }

                    fabVisited.setVisibility(View.INVISIBLE);
                    fabFavourites.setVisibility(View.INVISIBLE);
                    clicked = false;
                    setFavourite();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });

        fabVisited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

                dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!visited){
                            dbRef.child("VisitedMuseum").child(LoginActivity.loggedUser).child(currentMuseum.getName()).child("name").setValue(currentMuseum.getName());
                            Toast.makeText(getActivity(), "Aggiunto ai musei visitati", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            dbRef.child("VisitedMuseum").child(LoginActivity.loggedUser).child(currentMuseum.getName()).removeValue();
                            Toast.makeText(getActivity(), "Rimosso dai musei visitati", Toast.LENGTH_SHORT).show();
                        }

                        fabVisited.setVisibility(View.INVISIBLE);
                        fabFavourites.setVisibility(View.INVISIBLE);
                        clicked = false;
                        setVisited();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void showOnMap() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new MapFragment(currentMuseum), MapFragment.class.getSimpleName());

    }

    private void openAllReview() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new AllReview(currentMuseum), AllReview.class.getSimpleName());
    }

    private void publishReview() {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

        if (visited) {

            dbRef.child("ReviewMuseum").child(currentMuseum.getName()).child(LoginActivity.loggedUser).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dbRef.child("ReviewMuseum").child(currentMuseum.getName()).child(LoginActivity.loggedUser).child("username").setValue(LoginActivity.loggedUser);
                    dbRef.child("ReviewMuseum").child(currentMuseum.getName()).child(LoginActivity.loggedUser).child("rating").setValue(rating.getRating());
                    dbRef.child("ReviewMuseum").child(currentMuseum.getName()).child(LoginActivity.loggedUser).child("description").setValue(descriptionReview.getText().toString());

                    Toast.makeText(getActivity(), "Recensione pubblicata", Toast.LENGTH_SHORT).show();
                    descriptionReview.setText("");
                    rating.setRating(0);

                    adapterReviewMuseum.notifyDataSetChanged();

                    Utilities.insertFragment((AppCompatActivity) getActivity(), new AllReview(currentMuseum), AllReview.class.getSimpleName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        else{
            Toast.makeText(getContext(), "Aggiungi prima il museo nei 'Visitati'", Toast.LENGTH_SHORT).show();
        }
    }

    private void setVisited(){
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        dbRef.child("VisitedMuseum").child(LoginActivity.loggedUser).child(currentMuseum.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()){
                    visited = true;
                }
                else {
                    visited = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setFavourite() {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        dbRef.child("FavouriteMuseum").child(LoginActivity.loggedUser).child(currentMuseum.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()){
                    favourite = true;
                }
                else {
                    favourite = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void requestPermissionsInternet() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.INTERNET}, PERMISSION_CODE);
    }

    void requestPermissionsCall(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
    }

    void makeCall() {
        Intent i = new Intent(Intent.ACTION_DIAL);
        String uri = "tel:" + telephone.getText();
        i.setData(Uri.parse(uri));
        startActivity(i);
    }

    private void showCollection() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new AllArtworkFragment(currentMuseum), AllArtworkFragment.class.getSimpleName());
    }

    @Override
    public void onItemClickArtWork(int position) {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new ArtworkDetailsFragment(adapterArtwork.getArtWorkSelected(position)), ArtworkDetailsFragment.class.getSimpleName());
    }
}
