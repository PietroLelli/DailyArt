package com.example.dailyart;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dailyart.ArtWorkRecyclerView.ArtWork;
import com.example.dailyart.ArtistRecyclerView.Artist;
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
import java.util.Locale;

public class ArtworkDetailsFragment extends Fragment {

    private final ArtWork currentArtwork;

    private TextView artwork;
    private TextView museum;
    private TextView description;
    private TextView artist;
    private ExpandableTextView guide;
    private ImageView imageArtwork;
    private ImageView imageMuseum;
    private Button btnPublishReview;
    private Button btnAllReview;
    private RatingBar rating;
    private TextView descriptionReview;
    private TextView noReview;

    private TextToSpeech textToSpeech;
    private ImageView imgPlayAudio;
    private Button mButtonSpeak;


    private boolean isPlaying;

    private FloatingActionButton fabAdd;
    private FloatingActionButton fabFavourites;
    private FloatingActionButton fabVisited;

    ReviewCardAdapter adapterReviewArtwork;
    RecyclerView recyclerReviewArtwork;
    List<Review> reviewArtworkList = new ArrayList<>();

    DatabaseReference dbRef;

    boolean visited = false;
    boolean favourites = false;
    private boolean clicked = false;

    public ArtworkDetailsFragment(ArtWork artwork) {
        this.currentArtwork = artwork;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = textToSpeech.setLanguage(Locale.ITALIAN);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Lingua non supportata");
                    }
                    else {
                        mButtonSpeak.setEnabled(true);
                    }
                }
                else{
                    Log.e("TTS", "Inizializzazione fallita");

                }
            }
        });
        return inflater.inflate(R.layout.fragment_artwork_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setVisited();
        setFavourites();
        artwork = view.findViewById(R.id.tvArtistNameDetails);
        imageArtwork = view.findViewById(R.id.imageArtistDetails);
        description = view.findViewById(R.id.tvArtistDate);
        guide = view.findViewById(R.id.expand_text_view);
        artist = view.findViewById(R.id.tvArtworkArtist);
        museum = view.findViewById(R.id.tvArtworkMuseumDetails);
        imageMuseum = view.findViewById(R.id.imageArtworkMuseumDetails);
        noReview = view.findViewById(R.id.tvNoReviewArtwork);
        mButtonSpeak = view.findViewById(R.id.btnPlayAudio);

        rating = view.findViewById(R.id.ratingBarReviewArtwork);
        descriptionReview = view.findViewById(R.id.tvReviewDescriptionArtwork);
        btnPublishReview = view.findViewById(R.id.btnPublishReviewArtwork);
        btnPublishReview.setOnClickListener(view1 -> publishReview());
        btnAllReview= view.findViewById(R.id.btnShowReviewArtwork);
        btnAllReview.setOnClickListener(view1 -> openAllReview());

        fabAdd = view.findViewById(R.id.fab_add);
        fabFavourites = view.findViewById(R.id.fab_favourites);
        fabVisited = view.findViewById(R.id.fab_visited);

        dbRef.child("ArtWork").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentArtwork.getName())){
                    String imagePath = (snapshot.child(currentArtwork.getName()).child("image").getValue(String.class));
                    Drawable drawable = AppCompatResources.getDrawable(getContext(), getContext().getResources()
                            .getIdentifier(imagePath, "drawable", getActivity().getPackageName()));
                    imageArtwork.setImageDrawable(drawable);
                    artwork.setText(snapshot.child(currentArtwork.getName()).child("name").getValue(String.class));
                    museum.setText(snapshot.child(currentArtwork.getName()).child("museum").getValue(String.class));
                    description.setText(snapshot.child(currentArtwork.getName()).child("description").getValue(String.class));
                    artist.setText(snapshot.child(currentArtwork.getName()).child("artist").getValue(String.class));
                    guide.setText(snapshot.child(currentArtwork.getName()).child("guide").getValue(String.class));

                    String imagePathMuseum = (snapshot.child(currentArtwork.getName()).child("imageMuseum").getValue(String.class));
                    Drawable drawableMuseum = AppCompatResources.getDrawable(getContext(), getContext().getResources()
                            .getIdentifier(imagePathMuseum, "drawable", getActivity().getPackageName()));
                    imageMuseum.setImageDrawable(drawableMuseum);

                    imageMuseum.setOnClickListener(view -> openMuseum(museum.getText().toString()));
                    artist.setOnClickListener(view -> openArtist(artist.getText().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (reviewArtworkList != null) {
            reviewArtworkList.clear();
        }
        recyclerReviewArtwork = view.findViewById(R.id.recyclerReviewArtwork);
        recyclerReviewArtwork.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerReviewArtwork.setHasFixedSize(true);
        adapterReviewArtwork = new ReviewCardAdapter(reviewArtworkList, getActivity());
        recyclerReviewArtwork.setAdapter(adapterReviewArtwork);

        dbRef.child("ReviewArtwork").child(currentArtwork.getName()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Review review = dataSnapshot.getValue(Review.class);
                    reviewArtworkList.add(review);
                }
                adapterReviewArtwork.notifyDataSetChanged();
                if(reviewArtworkList.isEmpty()){
                    btnAllReview.setVisibility(View.INVISIBLE);
                    noReview.setVisibility(View.VISIBLE);
                }
                else {
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

                    if(favourites){
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

        fabFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

                dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!favourites){
                            dbRef.child("FavouriteArtwork").child(LoginActivity.loggedUser).child(currentArtwork.getName()).child("name").setValue(currentArtwork.getName());
                            Toast.makeText(getActivity(), "Aggiunto alle opere preferite", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            dbRef.child("FavouriteArtwork").child(LoginActivity.loggedUser).child(currentArtwork.getName()).removeValue();
                            Toast.makeText(getActivity(), "Rimosso dalle opere preferite", Toast.LENGTH_SHORT).show();
                        }

                        fabVisited.setVisibility(View.INVISIBLE);
                        fabFavourites.setVisibility(View.INVISIBLE);
                        clicked = false;
                        setFavourites();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        fabVisited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

                dbRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(!visited){
                            dbRef.child("VisitedArtwork").child(LoginActivity.loggedUser).child(currentArtwork.getName()).child("name").setValue(currentArtwork.getName());
                            Toast.makeText(getActivity(), "Aggiunto alle opere visitati", Toast.LENGTH_SHORT).show();

                        }
                        else{
                            dbRef.child("VisitedArtwork").child(LoginActivity.loggedUser).child(currentArtwork.getName()).removeValue();
                            Toast.makeText(getActivity(), "Rimosso dalle opere visitati", Toast.LENGTH_SHORT).show();

                        }
                        setVisited();
                        clicked = false;
                        fabVisited.setVisibility(View.INVISIBLE);
                        fabFavourites.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        mButtonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying){
                    stopSpeakText();
                    mButtonSpeak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play_arrow_48px, 0, 0, 0);
                    mButtonSpeak.setTextColor(Color.WHITE);
                }
                else{
                    speakText();
                    mButtonSpeak.setCompoundDrawablesWithIntrinsicBounds(R.drawable.stop_48px, 0, 0, 0);
                }
            }
        });

    }

    private void speakText() {
        textToSpeech.setPitch(1);
        textToSpeech.setSpeechRate(1);
        textToSpeech.speak(guide.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        isPlaying = true;
    }

    private void stopSpeakText() {
        isPlaying = false;
        textToSpeech.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(textToSpeech != null){
            textToSpeech.stop();
        }
    }

    private void setVisited(){
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        dbRef.child("VisitedArtwork").child(LoginActivity.loggedUser).child(currentArtwork.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").getValue(String.class) != null){
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

    private void setFavourites(){
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        dbRef.child("FavouriteArtwork").child(LoginActivity.loggedUser).child(currentArtwork.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").getValue(String.class) != null){
                    favourites = true;
                }
                else {
                    favourites = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openMuseum(String museumName) {
        DatabaseReference dbRefArtworkDetails = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        dbRefArtworkDetails.child("Museum").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(museumName)) {
                    Museum museum = snapshot.child(museumName).getValue(Museum.class);
                    Utilities.insertFragment((AppCompatActivity) getActivity(), new MuseumDetailsFragment(museum), MuseumDetailsFragment.class.getSimpleName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void openAllReview() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new AllReview(currentArtwork), AllReview.class.getSimpleName());
    }

    private void publishReview() {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        if(visited){
            dbRef.child("ReviewArtwork").child(currentArtwork.getName()).child(LoginActivity.loggedUser).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dbRef.child("ReviewArtwork").child(currentArtwork.getName()).child(LoginActivity.loggedUser).child("username").setValue(LoginActivity.loggedUser);
                    dbRef.child("ReviewArtwork").child(currentArtwork.getName()).child(LoginActivity.loggedUser).child("rating").setValue(rating.getRating());
                    dbRef.child("ReviewArtwork").child(currentArtwork.getName()).child(LoginActivity.loggedUser).child("description").setValue(descriptionReview.getText().toString());

                    Toast.makeText(getActivity(), "Recensione pubblicata", Toast.LENGTH_SHORT).show();
                    descriptionReview.setText("");
                    rating.setRating(0);

                    adapterReviewArtwork.notifyDataSetChanged();
                    Utilities.insertFragment((AppCompatActivity) getActivity(), new AllReview(currentArtwork), AllReview.class.getSimpleName());
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
    private void openArtist(String artistName) {
        DatabaseReference dbRefArtistDetails = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");
        dbRefArtistDetails.child("Artist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(artistName)) {
                    Artist artist = snapshot.child(artistName).getValue(Artist.class);
                    Utilities.insertFragment((AppCompatActivity) getActivity(), new ArtistDetailsFragment(artist), ArtistDetailsFragment.class.getSimpleName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}