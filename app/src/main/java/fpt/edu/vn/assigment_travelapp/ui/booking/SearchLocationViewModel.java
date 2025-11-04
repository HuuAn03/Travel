package fpt.edu.vn.assigment_travelapp.ui.booking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import fpt.edu.vn.assigment_travelapp.data.model.Place;
import fpt.edu.vn.assigment_travelapp.data.repository.IPlaceRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PlaceRepository;

public class SearchLocationViewModel extends ViewModel {

    private final PlaceRepository placeRepository;
    private final MutableLiveData<List<Place>> places = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public SearchLocationViewModel() {
        placeRepository = new PlaceRepository();
    }

    public LiveData<List<Place>> getPlaces() {
        return places;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void searchPlaces(String query) {
        if (query == null || query.trim().isEmpty()) {
            getAllPlaces();
            return;
        }

        isLoading.setValue(true);
        placeRepository.searchPlaces(query.trim(), new IPlaceRepository.OnPlacesFetchedListener() {
            @Override
            public void onSuccess(List<Place> placeList) {
                isLoading.postValue(false);
                places.postValue(placeList);
            }

            @Override
            public void onFailure(String errorMessage) {
                isLoading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }

    public void getAllPlaces() {
        isLoading.setValue(true);
        placeRepository.getAllPlaces(new IPlaceRepository.OnPlacesFetchedListener() {
            @Override
            public void onSuccess(List<Place> placeList) {
                isLoading.postValue(false);
                places.postValue(placeList);
            }

            @Override
            public void onFailure(String errorMessage) {
                isLoading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
}

