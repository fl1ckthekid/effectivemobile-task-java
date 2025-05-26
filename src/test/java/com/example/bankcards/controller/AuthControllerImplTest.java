package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(controllers = AuthControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private final String username = "testuser";
    private final String password = "password";

    @Test
    void register_userDoesNotExist_shouldRegisterSuccessfully() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        mockMvc.perform(post("/api/auth/register")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь зарегистрирован"));

        verify(userRepository).save(Mockito.argThat(user ->
                user.getUsername().equals(username)
                        && user.getRoles().contains(Role.USER)
                        && user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void register_userAlreadyExists_shouldReturnBadRequest() throws Exception {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(User.builder().username(username).build()));

        mockMvc.perform(post("/api/auth/register")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пользователь уже существует"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_shouldReturnToken() throws Exception {
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(username, user.getRoles())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void login_wrongPassword_shouldReturnBadRequest() throws Exception {
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Неверный пароль"));
    }

    @Test
    void login_userNotFound_shouldReturnServerError() throws Exception {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Имя пользователя не найдено"));
    }
}
