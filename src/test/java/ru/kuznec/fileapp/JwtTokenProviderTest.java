package ru.kuznec.fileapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kuznec.fileapp.security.jwt.JwtConfig;
import ru.kuznec.fileapp.security.jwt.JwtTokenProvider;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("ZjU2NjFlZTY3OGVjMTM5Mzg0ZDg3MjZiOTVkZTA5NTQ5NjA4ODc3Njg4OTZjNTQ4Y2ZlZjNlYzAwMjMwNzc2MA==");
        jwtConfig.setExpiration(60000);
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
    }


    @Test
    void testGenerateToken() {
        String token = jwtTokenProvider.generateToken("testUser");
        assertNotNull(token);
    }

    @Test
    void testValidateToken() {
        String token = jwtTokenProvider.generateToken("testUser");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtTokenProvider.generateToken("testUser");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("testUser", username);
    }
}

