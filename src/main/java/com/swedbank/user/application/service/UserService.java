package com.swedbank.user.application.service;

import com.swedbank.account.application.service.AccountService;
import com.swedbank.user.application.dto.UserAccountRequest;
import com.swedbank.user.application.dto.UserDto;
import com.swedbank.user.domain.model.User;
import com.swedbank.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto getUserByEmail(String email) {
        User user = findUserByEmail(email);
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional
    public void createUserAccount(UserAccountRequest userAccountRequest) {
        User user = modelMapper.map(userAccountRequest.getUser(), User.class);
        userRepository.save(user);

        accountService.createAccount(userAccountRequest.getCreateAccount());
    }

}
