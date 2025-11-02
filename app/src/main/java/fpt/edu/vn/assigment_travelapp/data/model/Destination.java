package fpt.edu.vn.assigment_travelapp.data.model;

import java.util.List;

public class Destination {
    private String destinationId;
    private String name;
    private String location;
    private String address;
    private String description;
    private double rating;
    private int reviewCount;
    private double pricePerPerson;
    private String imageUrl;
    private List<String> galleryImages;
    private String category; // Adventure, Beach, Food & Drink, etc.
    private double latitude;
    private double longitude;

    public Destination() {
        // Default constructor for Firebase
    }

    public Destination(String destinationId, String name, String location, String address,
                      String description, double rating, int reviewCount, double pricePerPerson,
                      String imageUrl, List<String> galleryImages, String category,
                      double latitude, double longitude) {
        this.destinationId = destinationId;
        this.name = name;
        this.location = location;
        this.address = address;
        this.description = description;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.pricePerPerson = pricePerPerson;
        this.imageUrl = imageUrl;
        this.galleryImages = galleryImages;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public double getPricePerPerson() {
        return pricePerPerson;
    }

    public void setPricePerPerson(double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getGalleryImages() {
        return galleryImages;
    }

    public void setGalleryImages(List<String> galleryImages) {
        this.galleryImages = galleryImages;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

