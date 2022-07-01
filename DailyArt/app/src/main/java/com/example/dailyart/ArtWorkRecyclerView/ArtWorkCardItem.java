package com.example.dailyart.ArtWorkRecyclerView;

public class ArtWorkCardItem {
    String image;
    String artwork;
    String museum;

    public ArtWorkCardItem(String image, String artwork, String museum) {
        this.image = image;
        this.museum = museum;
        this.artwork = artwork;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getArtwork() {
        return artwork;
    }

    public void setArtwork(String artwork) {
        this.artwork = artwork;
    }

    public String getMuseum() {
        return museum;
    }

    public void setMuseum(String museum) {
        this.museum = museum;
    }
}
