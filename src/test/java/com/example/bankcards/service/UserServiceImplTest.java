package com.example.bankcards.service;

import com.example.bankcards.dto.UserRequestDto;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_Success() {
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("testUser");
        dto.setPassword("plainPassword");
        dto.setRoles(Set.of(Role.valueOf("USER")));

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPassword")
                .roles(Set.of(Role.valueOf("USER")))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto response = userService.createUser(dto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testUser", response.getUsername());
        assertIterableEquals(Set.of("USER"), response.getRoles());

        verify(userRepository).existsByUsername("testUser");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UserAlreadyExists() {
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("existingUser");

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(dto));

        verify(userRepository).existsByUsername("existingUser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_ReturnsList() {
        User user1 = User.builder().id(1L).username("user1").roles(Set.of(Role.valueOf("USER"))).build();
        User user2 = User.builder().id(2L).username("user2").roles(Set.of(Role.valueOf("ADMIN"))).build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponseDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_UserExists() {
        User user = User.builder().id(1L).username("user1").roles(Set.of(Role.valueOf("USER"))).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user1", response.getUsername());

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));

        verify(userRepository).findById(99L);
    }

    @Test
    void deleteUser_UserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));

        verify(userRepository).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
