package com.swedbank.bankservice.common.utils;

import com.swedbank.user.application.dto.UserDto;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;

public class ApiUtil {

    public static ResultActions mockPostApi(
        MockMvc mockMvc,
        String content,
        String url,
        UserDto user,
        String contentType
    ) {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.post("/api/v1" + url)
            .contentType(contentType == null ? MediaType.APPLICATION_JSON : MediaType.valueOf(contentType))
            .accept(MediaType.APPLICATION_JSON);

        if (user != null) {
            String accessToken = TestUtil.getToken(user);
            mockHttpServletRequestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        mockHttpServletRequestBuilder.content(content);

        try {
            return mockMvc.perform(mockHttpServletRequestBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultActions mockPostApi(MockMvc mockMvc, Integer port, String content, String url) {
        try {
            return mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1" + url).contentType("application/json").content(content)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultActions mockGetApi(
        MockMvc mockMvc,
        Integer port,
        String url,
        UserDto user,
        MultiValueMap<String, String> queryParams,
        String contentType
    ) {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder = MockMvcRequestBuilders.get(
            "/api/v1" + url
        ).contentType(contentType == null ? "application/json" : contentType);

        if (user != null) {
            String accessToken = TestUtil.getToken(user);
            mockHttpServletRequestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        if (queryParams != null) {
            mockHttpServletRequestBuilder.params(queryParams);
        }

        try {
            return mockMvc.perform(mockHttpServletRequestBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
