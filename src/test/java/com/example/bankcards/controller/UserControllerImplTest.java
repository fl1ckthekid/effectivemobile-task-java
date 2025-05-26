package com.example.bankcards.controller;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void createUser_validRequest_shouldReturnUserResponse() throws Exception {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("testuser");
        requestDto.setPassword("password");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setUsername("testuser");

        when(userServiceImpl.createUser(any(UserRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.username").value(responseDto.getUsername()));

        verify(userServiceImpl).createUser(any(UserRequestDto.class));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        UserResponseDto user1 = new UserResponseDto();
        user1.setId(1L);
        user1.setUsername("user1");

        UserResponseDto user2 = new UserResponseDto();
        user2.setId(2L);
        user2.setUsername("user2");

        when(userServiceImpl.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(user1.getId()))
                .andExpect(jsonPath("$[0].username").value(user1.getUsername()))
                .andExpect(jsonPath("$[1].id").value(user2.getId()))
                .andExpect(jsonPath("$[1].username").value(user2.getUsername()));

        verify(userServiceImpl).getAllUsers();
    }

    @Test
    void getUserById_existingId_shouldReturnUser() throws Exception {
        Long id = 1L;
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(id);
        responseDto.setUsername("testuser");

        when(userServiceImpl.getUserById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userServiceImpl).getUserById(id);
    }

    @Test
    void deleteUser_existingId_shouldReturnNoContent() throws Exception {
        Long id = 1L;

        doNothing().when(userServiceImpl).deleteUser(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userServiceImpl).deleteUser(id);
    }
}
