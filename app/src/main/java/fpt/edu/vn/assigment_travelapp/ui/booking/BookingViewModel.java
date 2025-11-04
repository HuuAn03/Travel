package fpt.edu.vn.assigment_travelapp.ui.booking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import fpt.edu.vn.assigment_travelapp.data.model.Place;
import fpt.edu.vn.assigment_travelapp.data.repository.IPlaceRepository;
import fpt.edu.vn.assigment_travelapp.data.repository.PlaceRepository;

public class BookingViewModel extends ViewModel {

    private final PlaceRepository placeRepository;
    private final MutableLiveData<Place> place = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public BookingViewModel() {
        placeRepository = new PlaceRepository();
    }

    public LiveData<Place> getPlace() {
        return place;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadPlace(String placeId) {
        placeRepository.getPlaceById(placeId, new IPlaceRepository.OnPlaceFetchedListener() {
            @Override
            public void onSuccess(Place loadedPlace) {
                place.postValue(loadedPlace);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue(errorMessage);
            }
        });
    }
}

