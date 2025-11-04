package fpt.edu.vn.assigment_travelapp.data.repository;

import java.util.List;
import fpt.edu.vn.assigment_travelapp.data.model.Place;

public interface IPlaceRepository {
    interface OnPlacesFetchedListener {
        void onSuccess(List<Place> places);
        void onFailure(String error);
    }

    void searchPlaces(String query, OnPlacesFetchedListener listener);
    void getAllPlaces(OnPlacesFetchedListener listener);
    void getPlaceById(String placeId, OnPlaceFetchedListener listener);
    
    interface OnPlaceFetchedListener {
        void onSuccess(Place place);
        void onFailure(String error);
    }
}

