package net.roodt.springtestcontainers.authz.user;

import net.roodt.springtestcontainers.authz.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DynamoDbTemplate dynamoDbTemplate;

    @Test
    public void shouldCreateUserOnPost() throws Exception {
        UUID id = getUuid();

        User user = getUser(id);

        willDoNothing().given(userRepository).create(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + id));

        verify(userRepository).create(user);
    }

    @Test
    public void shouldUpdateUserOnPut() throws Exception {
        UUID id = getUuid();

        User user = getUser(id);

        given(userRepository.update(user)).willReturn(true);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository).update(user);
    }

    @Test
    public void willReturn404WhenUserDoesNotExistOnPut() throws Exception {
        UUID id = getUuid();

        User user = getUser(id);

        given(userRepository.update(user)).willReturn(false);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository).update(user);
    }

    @Test
    public void shouldReturnUserOnGet() throws Exception {

        UUID id = getUuid();

        given(userRepository.findByUserId(id)).willReturn(new User(id, "John Doe"));

        mockMvc.perform(get("/users/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("John Doe"))
                .andExpect(jsonPath("userId").value(id.toString()));

        verify(userRepository).findByUserId(id);
    }

    @Test
    public void shouldReturn404WhenUserNotExistsOnGet() throws Exception {

        UUID id = getUuid();
        given(userRepository.findByUserId(id)).willThrow(EmptyResultDataAccessException.class);

        mockMvc.perform(get("/users/" + id))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository).findByUserId(id);
    }

    @Test
    public void shouldDeleteUserOnDelete() throws Exception {
        UUID id = getUuid();

        given(userRepository.delete(id)).willReturn(true);

        mockMvc.perform(delete("/users/" + id))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userRepository).delete(id);
    }

    @Test
    public void shouldReturn404WhenUserDoesNotExistOnDelete() throws Exception {
        UUID id = getUuid();
        given(userRepository.delete(id)).willReturn(false);

        mockMvc.perform(delete("/users/" + id))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userRepository).delete(id);
    }

    private static @NotNull UUID getUuid() {
        return UUID.randomUUID();
    }

    private static @NotNull User getUser(UUID id) {
        return new User(id, "John Doe");
    }
}