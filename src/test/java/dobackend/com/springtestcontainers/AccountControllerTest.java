package dobackend.com.springtestcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers= AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountRepository accountRepository;

    @Test
    public void shouldCreateAccountOnPost() throws Exception {
        UUID id = getUuid();
        Account account = getAccount(id);
        willDoNothing().given(accountRepository).create(account);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/accounts/" + id));

        verify(accountRepository).create(account);
    }

    @Test
    public void shouldUpdateAccountOnPut() throws Exception {
        UUID id = getUuid();
        Account account = getAccount(id);

        given(accountRepository.update(account)).willReturn(true);

        mockMvc.perform(put("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(accountRepository).update(account);
    }

    @Test
    public void willReturn404WhenAccountDoesNotExistOnPut() throws Exception {
        UUID id = getUuid();
        Account account = getAccount(id);

        given(accountRepository.update(account)).willReturn(false);

        mockMvc.perform(put("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(accountRepository).update(account);
    }

    @Test
    public void shouldReturnAccountOnGet() throws Exception {

        UUID id = getUuid();
        given(accountRepository.findByAccountId(id)).willReturn(new Account(id, "John Doe"));

        mockMvc.perform(get("/accounts/" + id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("John Doe"))
                .andExpect(jsonPath("accountId").value(id.toString()));

        verify(accountRepository).findByAccountId(id);
    }

    @Test
    public void shouldReturn404WhenAccountNotExistsOnGet() throws Exception {

        UUID id = getUuid();
        given(accountRepository.findByAccountId(id)).willThrow(EmptyResultDataAccessException.class);

        mockMvc.perform(get("/accounts/" + id))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(accountRepository).findByAccountId(id);
    }

    @Test
    public void shouldDeleteAccountOnDelete() throws Exception {
        UUID id = getUuid();

        given(accountRepository.delete(id)).willReturn(true);

        mockMvc.perform(delete("/accounts/" + id))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(accountRepository).delete(id);
    }

    @Test
    public void shouldReturn404WhenAccountDoesNotExistOnDelete() throws Exception {
        UUID id = getUuid();
        given(accountRepository.delete(id)).willReturn(false);

        mockMvc.perform(delete("/accounts/" + id))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(accountRepository).delete(id);
    }

    private static @NotNull UUID getUuid() {
        UUID id = UUID.randomUUID();
        return id;
    }

    private static @NotNull Account getAccount(UUID id) {
        Account account = new Account(id, "John Doe");
        return account;
    }
}