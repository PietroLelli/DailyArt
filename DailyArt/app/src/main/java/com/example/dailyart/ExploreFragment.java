package com.example.dailyart;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dailyart.ArtWorkRecyclerView.ArtWork;
import com.example.dailyart.ArtWorkRecyclerView.ArtworkCardAdapter;
import com.example.dailyart.ArtWorkRecyclerView.OnItemListenerArtWork;
import com.example.dailyart.ArtistRecyclerView.Artist;
import com.example.dailyart.ArtistRecyclerView.ArtistCardAdapter;
import com.example.dailyart.ArtistRecyclerView.OnItemListenerArtist;
import com.example.dailyart.MuseumRecyclerView.MuseumCardAdapter;
import com.example.dailyart.MuseumRecyclerView.Museum;
import com.example.dailyart.MuseumRecyclerView.OnItemListenerMuseum;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment implements OnItemListenerMuseum, OnItemListenerArtWork, OnItemListenerArtist {

    DatabaseReference dbRef;

    MuseumCardAdapter adapterMuseum;
    RecyclerView recyclerViewMuseum;
    List<Museum> museumList = new ArrayList<>();

    ArtworkCardAdapter adapterArtwork;
    RecyclerView recyclerArtwork;
    List<ArtWork> artworkList = new ArrayList<>();

    ArtistCardAdapter adapterArtist;
    RecyclerView recyclerArtist;
    List<Artist> artistList = new ArrayList<>();

    TextView showArtworks;
    TextView showMuseums;
    TextView showArtists;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (museumList != null) {
            museumList.clear();
        }
        recyclerViewMuseum = view.findViewById(R.id.recyclerViewMuseumsVisited);
        recyclerViewMuseum.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewMuseum.setHasFixedSize(true);
        final OnItemListenerMuseum listenerMuseum = this;
        adapterMuseum = new MuseumCardAdapter(listenerMuseum, museumList, getActivity());
        recyclerViewMuseum.setAdapter(adapterMuseum);

        showMuseums = view.findViewById(R.id.tvShowMuseum);
        showMuseums.setOnClickListener(v -> showMuseums());

        dbRef.child("Museum").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (count >= 5) {
                        continue;
                    }
                    Museum museum = dataSnapshot.getValue(Museum.class);
                    museumList.add(museum);
                    count++;
                }
                adapterMuseum.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (artworkList != null) {
            artworkList.clear();
        }
        recyclerArtwork = view.findViewById(R.id.recyclerViewArtworkVisited);
        recyclerArtwork.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerArtwork.setHasFixedSize(true);
        final OnItemListenerArtWork listenerArtWork = this;
        adapterArtwork = new ArtworkCardAdapter(listenerArtWork, artworkList, getActivity());
        recyclerArtwork.setAdapter(adapterArtwork);

        showArtworks = view.findViewById(R.id.tvShowArtwork);
        showArtworks.setOnClickListener(v -> showArtworks());

        dbRef.child("ArtWork").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (count >= 5) {
                        continue;
                    }
                    ArtWork artWork = dataSnapshot.getValue(ArtWork.class);
                    artworkList.add(artWork);
                    count++;
                }
                adapterArtwork.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (artistList != null) {
            artistList.clear();
        }
        recyclerArtist = view.findViewById(R.id.recyclerViewArtist);
        recyclerArtist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerArtist.setHasFixedSize(true);
        final OnItemListenerArtist listenerArtist = this;
        adapterArtist = new ArtistCardAdapter(listenerArtist, artistList, getActivity());
        recyclerArtist.setAdapter(adapterArtist);

        showArtists = view.findViewById(R.id.tvShowArtist);
        showArtists.setOnClickListener(v -> showArtists());

        dbRef.child("Artist").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (count >= 5) {
                        continue;
                    }
                    Artist artist = dataSnapshot.getValue(Artist.class);
                    artistList.add(artist);
                    count++;
                }
                adapterArtist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        /*
        SearchView searchView = getActivity().findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapterArtwork.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText == ""){
                    adapterArtwork.notFilter();
                }
                return false;
            }

        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapterArtwork.notFilter();
                return false;
            }
        });*/
    }

    private void showMuseums() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new AllMuseumFragment(), AllMuseumFragment.class.getSimpleName());
    }

    private void showArtworks() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new AllArtworkFragment(), AllArtworkFragment.class.getSimpleName());
    }

    private void showArtists() {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new AllArtistFragment(), AllArtistFragment.class.getSimpleName());
    }

    @Override
    public void onItemClickMuseum(int position) {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new MuseumDetailsFragment(adapterMuseum.getMuseumSelected(position)), MuseumDetailsFragment.class.getSimpleName());
    }

    @Override
    public void onItemClickArtWork(int position) {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new ArtworkDetailsFragment(adapterArtwork.getArtWorkSelected(position)), ArtworkDetailsFragment.class.getSimpleName());
    }

    @Override
    public void onItemClickArtist(int position) {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new ArtistDetailsFragment(adapterArtist.getArtistSelected(position)), ArtistDetailsFragment.class.getSimpleName());

    }
}