package com.example.dailyart;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dailyart.ArtWorkRecyclerView.ArtWork;
import com.example.dailyart.ArtWorkRecyclerView.ArtworkCardAdapter;
import com.example.dailyart.ArtWorkRecyclerView.OnItemListenerArtWork;
import com.example.dailyart.MuseumRecyclerView.Museum;
import com.example.dailyart.MuseumRecyclerView.MuseumCardAdapter;
import com.example.dailyart.MuseumRecyclerView.OnItemListenerMuseum;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentVisited extends Fragment implements OnItemListenerArtWork, OnItemListenerMuseum{

    DatabaseReference dbRef;

    MuseumCardAdapter adapterMuseum;
    RecyclerView recyclerViewMuseum;

    List<Museum> museumList = new ArrayList<>();

    ArtworkCardAdapter adapterArtwork;
    RecyclerView recyclerViewArtwork;

    List<ArtWork> artWorkList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dailyart-a8682-default-rtdb.firebaseio.com/");

        return inflater.inflate(R.layout.fragment_favourites, container, false);
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

        dbRef.child("VisitedMuseum").child(LoginActivity.loggedUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String museumName = dataSnapshot.child("name").getValue(String.class);
                    dbRef.child("Museum").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotMuseum) {
                            for (DataSnapshot dataSnapshotMuseum : snapshotMuseum.getChildren()) {
                                if (dataSnapshotMuseum.child("name").getValue().equals(museumName)){
                                    Museum m = dataSnapshotMuseum.getValue(Museum.class);
                                    museumList.add(m);
                                    System.out.println(museumList.toString());
                                }
                                System.out.println(dataSnapshotMuseum.child("name").getValue());
                                System.out.println(museumName);
                            }
                            adapterMuseum.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                adapterMuseum.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (artWorkList != null) {
            artWorkList.clear();
        }
        recyclerViewArtwork = view.findViewById(R.id.recyclerViewArtworkVisited);
        recyclerViewArtwork.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewArtwork.setHasFixedSize(true);
        final OnItemListenerArtWork listenerArtWork = this;
        adapterArtwork= new ArtworkCardAdapter(listenerArtWork, artWorkList, getActivity());
        recyclerViewArtwork.setAdapter(adapterArtwork);

        dbRef.child("VisitedArtwork").child(LoginActivity.loggedUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String artworkName = dataSnapshot.child("name").getValue(String.class);
                    dbRef.child("ArtWork").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotArtwork) {
                            for (DataSnapshot dataSnapshotArtwork : snapshotArtwork.getChildren()) {
                                if (dataSnapshotArtwork.child("name").getValue().equals(artworkName)){
                                    ArtWork a = dataSnapshotArtwork.getValue(ArtWork.class);
                                    artWorkList.add(a);
                                    System.out.println(artWorkList.toString());
                                }
                                System.out.println(dataSnapshotArtwork.child("name").getValue());
                                System.out.println(artworkName);
                            }
                            adapterArtwork.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                adapterArtwork.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemClickMuseum(int position) {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new MuseumDetailsFragment(adapterMuseum.getMuseumSelected(position)), MuseumDetailsFragment.class.getSimpleName());
    }

    @Override
    public void onItemClickArtWork(int position) {
        Utilities.insertFragment((AppCompatActivity) getActivity(), new ArtworkDetailsFragment(adapterArtwork.getArtWorkSelected(position)), ArtworkDetailsFragment.class.getSimpleName());

    }
}