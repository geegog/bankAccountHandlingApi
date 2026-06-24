package com.swedbank.user.application.service;

import com.swedbank.user.application.dto.UserDto;
import com.swedbank.user.domain.model.User;
import com.swedbank.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto getUserByEmail(String email) {
        User user = findUserByEmail(email);
        return modelMapper.map(user, UserDto.class);
    }

    public void createUser(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        userRepository.save(user);
    }

}
