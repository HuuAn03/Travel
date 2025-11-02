package fpt.edu.vn.assigment_travelapp.ui.discover;

public class Destination {
    public String name;
    public String country;
    public String imageUrl;
    public double rating;
    public String price;

    public Destination(String name, String country, String imageUrl, double rating, String price) {
        this.name = name;
        this.country = country;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.price = price;
    }
}