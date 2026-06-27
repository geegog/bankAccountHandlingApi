package com.swedbank.bankservice.common.utils;

import com.swedbank.auth.application.util.JwtUtil;
import com.swedbank.user.application.dto.UserDto;

public class TestUtil {

    public static String getToken(UserDto user) {
        return JwtUtil.generateToken(user, "c33d58c950b29223f195d56981314c6a23d8044ea17de009c9a51c7abb30103a", 30000);
    }


}
