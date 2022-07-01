package com.example.dailyart;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyart.ArtWorkRecyclerView.ArtWork;
import com.example.dailyart.ArtWorkRecyclerView.ArtworkCardAdapter;
import com.example.dailyart.ArtWorkRecyclerView.OnItemListenerArtWork;
import com.example.dailyart.ArtistRecyclerView.Artist;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

public class ArtistDetailsFragment extends Fragment implements OnItemListenerArtWork{

    private final Artist currentArtist;

    private TextView artist;
    private ImageView imageArtist;
    private ExpandableTextView description;
    private TextView date;

    Button btnArtworkCollection;

    ArtworkCardAdapter adapterArtwork;
    RecyclerView recyclerArtworkArtist;
    List<ArtWork> artworkList = new ArrayList<>();

    DatabaseReference dbRef;

    public ArtistDetailsFragment(Artist artist) {
        this.currentArtist = artist;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

        return inflater.inflate(R.layout.fragment_artist_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        artist = view.findViewById(R.id.tvArtistNameDetails);
        imageArtist = view.findViewById(R.id.imageArtistDetails);
        date = view.findViewById(R.id.tvArtistDate);
        description = view.findViewById(R.id.expand_text_view);
        btnArtworkCollection = view.findViewById(R.id.btnArtworkCollection);
        btnArtworkCollection.setOnClickListener(v -> showCollection());

        dbRef.child("Artist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentArtist.getName())){
                    String imagePath = (snapshot.child(currentArtist.getName()).child("image").getValue(String.class));
                    Drawable drawable = AppCompatResources.getDrawable(getContext(), getContext().getResources()
                            .getIdentifier(imagePath, "drawable", getActivity().getPackageName()));
                    imageArtist.setImageDrawable(drawable);
                    artist.setText(snapshot.child(currentArtist.getName()).child("name").getValue(String.class));
                    description.setText(snapshot.child(currentArtist.getName()).child("description").getValue(String.class));
                    date.setText(snapshot.child(currentArtist.getName()).child("date").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (artworkList != null) {
            artworkList.clear();
        }
        recyclerArtworkArtist = view.findViewById(R.id.recyclerArtworkArtist);
        recyclerArtworkArtist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerArtworkArtist.setHasFixedSize(true);
        final OnItemListenerArtWork listenerArtWork = this;
        adapterArtwork = new ArtworkCardAdapter(listenerArtWork, artworkList, getActivity());
        recyclerArtworkArtist.setAdapter(adapterArtwork);

        dbRef.child("ArtWork").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String getArtwork = dataSnapshot.child("artist").getValue(String.class);
                    if (getArtwork.equals(currentArtist.getName())) {
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
    }

    private void showCollection() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new AllArtworkFragment(currentArtist), AllArtworkFragment.class.getSimpleName());
    }

    @Override
    public void onItemClickArtWork(int position) {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new ArtworkDetailsFragment(adapterArtwork.getArtWorkSelected(position)), ArtworkDetailsFragment.class.getSimpleName());
    }
}
