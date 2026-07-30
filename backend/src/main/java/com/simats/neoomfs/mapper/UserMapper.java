package com.simats.neoomfs.mapper;

import com.simats.neoomfs.dto.response.UserProfileResponse;
import com.simats.neoomfs.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        if (user == null) return null;
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .licenseNumber(user.getLicenseNumber())
                .department(user.getDepartment())
                .institution(user.getInstitution())
                .phoneNumber(user.getPhoneNumber())
                .active(user.isActive())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
