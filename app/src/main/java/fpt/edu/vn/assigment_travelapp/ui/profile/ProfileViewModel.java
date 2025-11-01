package fpt.edu.vn.assigment_travelapp.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull; // Hãy chắc chắn bạn đã import

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fpt.edu.vn.assigment_travelapp.data.model.User;
import fpt.edu.vn.assigment_travelapp.data.repository.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final FirebaseAuth mAuth;
    private final UserRepository userRepository;

    // BỘ LẮNG NGHE (LISTENER) MỚI
    private FirebaseAuth.AuthStateListener authStateListener;

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> user = _user;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public ProfileViewModel() {
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        // 1. Tạo bộ lắng nghe
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // 3. Ngay khi Firebase báo "đã đăng nhập", gọi hàm tải dữ liệu
                    loadUserProfileData(firebaseUser);
                } else {
                    // Người dùng đã đăng xuất
                    _error.setValue("Không có người dùng nào đang đăng nhập.");
                }
            }
        };
        mAuth.addAuthStateListener(authStateListener);
    }

    // Đổi tên hàm này và để nó nhận FirebaseUser
    private void loadUserProfileData(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        if (email != null) {
            userRepository.getUserByEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User dbUser = task.getResult().getValue(User.class);
                    if (dbUser != null) {
                        _user.setValue(dbUser);
                        _error.setValue(null); // Xóa lỗi cũ nếu có
                    } else {
                        _error.setValue("Không tìm thấy dữ liệu người dùng trong database.");
                    }
                } else {
                    _error.setValue("Lỗi khi tải dữ liệu: " + task.getException().getMessage());
                }
            });
        } else {
            _error.setValue("Email người dùng bị null.");
        }
    }

    // Rất quan trọng: Gỡ bỏ listener khi ViewModel bị hủy để tránh rò rỉ bộ nhớ
    @Override
    protected void onCleared() {
        super.onCleared();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}