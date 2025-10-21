package com.dztech.auth.service;

import com.dztech.auth.client.RayderVehicleClient;
import com.dztech.auth.dto.UpdateUserProfileRequest;
import com.dztech.auth.dto.UserProfileView;
import com.dztech.auth.exception.ResourceNotFoundException;
import com.dztech.auth.model.UserProfile;
import com.dztech.auth.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final RayderVehicleClient rayderVehicleClient;

    public ProfileService(
            UserProfileRepository userProfileRepository, RayderVehicleClient rayderVehicleClient) {
        this.userProfileRepository = userProfileRepository;
        this.rayderVehicleClient = rayderVehicleClient;
    }

    @Transactional(readOnly = true)
    public UserProfileView getProfile(Long userId, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return toView(profile, accessToken);
    }

    @Transactional
    public UserProfileView updateProfile(Long userId, UpdateUserProfileRequest request, String accessToken) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            profile.setName(name);
        }

        if (request.email() != null) {
            String email = request.email().trim();
            if (email.isEmpty()) {
                throw new IllegalArgumentException("Email cannot be blank");
            }
            profile.setEmail(email.toLowerCase());
        }

        if (request.address() != null) {
            String address = request.address().trim();
            profile.setAddress(StringUtils.hasText(address) ? address : null);
        }

        UserProfile updated = userProfileRepository.save(profile);
        return toView(updated, accessToken);
    }

    private UserProfileView toView(UserProfile profile, String accessToken) {
        long vehicleCount = rayderVehicleClient.fetchVehicleCount(accessToken);
        long completedBookings = 0L;
        long activeBookings = 0L;

        return new UserProfileView(
                profile.getUserId(),
                profile.getName(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getAddress(),
                vehicleCount,
                completedBookings,
                activeBookings);
    }
}
