package ru.kuznec.fileapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.kuznec.fileapp.controller.FileController;
import ru.kuznec.fileapp.model.File;
import ru.kuznec.fileapp.model.User;
import ru.kuznec.fileapp.security.jwt.JwtTokenProvider;
import ru.kuznec.fileapp.service.CustomUserDetailsService;
import ru.kuznec.fileapp.service.FileService;
import ru.kuznec.fileapp.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testUser");
        testUser.setFileLimit(10);
        testUser.setStorageLimit(104857600);


        jwtToken = "mockedJwtToken";
        Mockito.when(jwtTokenProvider.generateToken(testUser.getUsername()))
                .thenReturn(jwtToken);

    }

    @Test
    @WithMockUser(username = "testUser")
    void testUploadFile() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test file content".getBytes());

        File file = new File(UUID.randomUUID(), "test.txt", mockFile.getSize(), false, null, testUser, "2024-10-17");

        Mockito.when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        Mockito.when(fileService.canUploadFile(eq(testUser), eq(mockFile.getSize()))).thenReturn(true);
        Mockito.when(fileService.saveFile(any(MockMultipartFile.class), eq(testUser))).thenReturn(file);

        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testUser")
    void testGetAllFiles() throws Exception {

        File file1 = new File(UUID.randomUUID(), "file1.txt", 1024, false, null, testUser, "2024-10-17");
        File file2 = new File(UUID.randomUUID(), "file2.txt", 2048, false, null, testUser, "2024-10-17");

        Mockito.when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        Mockito.when(fileService.getFilesByUser(eq(testUser))).thenReturn(Arrays.asList(file1, file2));

        mockMvc.perform(get("/api/files")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("file1.txt"))
                .andExpect(jsonPath("$[0].size").value(1024))
                .andExpect(jsonPath("$[1].filename").value("file2.txt"))
                .andExpect(jsonPath("$[1].size").value(2048));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testDeleteFile() throws Exception {
        UUID fileId = UUID.randomUUID();

        Mockito.when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        Mockito.when(fileService.deleteFile(eq(fileId), eq(testUser))).thenReturn(true);

        mockMvc.perform(delete("/api/files/" + fileId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Файл успешно удален"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testGeneratePublicLink() throws Exception {
        UUID fileId = UUID.randomUUID();
        String publicLink = "http://localhost:8080/api/files/download/" + fileId;

        Mockito.when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        Mockito.when(fileService.generatePublicLink(eq(fileId), eq(testUser))).thenReturn(Optional.of(publicLink));

        mockMvc.perform(get("/api/files/" + fileId + "/public-link")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().string(publicLink));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testGetAllFilesEmpty() throws Exception {
        Mockito.when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        Mockito.when(fileService.getFilesByUser(eq(testUser))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/files")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @WithMockUser(username = "testUser")
    @Test
    void testDeleteFileOfAnotherUser() throws Exception {
        UUID fileId = UUID.randomUUID();

        User anotherUser = new User(UUID.randomUUID(), "anotherUser", "password", User.Role.USER, false, 10, 104857600);
        Mockito.when(fileService.deleteFile(eq(fileId), eq(testUser))).thenReturn(false);

        mockMvc.perform(delete("/api/files/" + fileId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(username = "testUser")
    @Test
    void testGetPublicLinkForAnotherUserFile() throws Exception {
        UUID fileId = UUID.randomUUID();
        User anotherUser = new User(UUID.randomUUID(), "anotherUser", "password", User.Role.USER, false, 10, 104857600);

        Mockito.when(userService.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        Mockito.when(fileService.generatePublicLink(eq(fileId), eq(testUser))).thenReturn(Optional.empty());
        Mockito.when(fileService.fileExists(fileId)).thenReturn(true);

        mockMvc.perform(get("/api/files/" + fileId + "/public-link")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Файл не найден или у вас нет прав на его просмотр"));
    }
}
