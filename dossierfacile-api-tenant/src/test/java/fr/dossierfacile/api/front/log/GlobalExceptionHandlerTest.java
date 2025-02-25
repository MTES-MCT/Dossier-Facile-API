package fr.dossierfacile.api.front.log;

import fr.dossierfacile.api.front.controller.UserController;
import fr.dossierfacile.api.front.exception.UserNotFoundException;
import fr.dossierfacile.api.front.security.interfaces.AuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GlobalExceptionHandlerTest {

    private MockMvc mvc;
    private final UserService userService = mock(UserService.class);

    @Nested
    class withoutGlobalExceptionHandler {
        @BeforeEach
        void setUp() {
            var controller = new UserController(userService, mock(AuthenticationFacade.class));
            mvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

        @Test
        void shouldReturnA404() throws Exception {
            doThrow(new UserNotFoundException("user not found")).when(userService).forgotPassword(any());
            mvc.perform(post("/api/user/forgotPassword").contentType("application/json").content("{\"email\":\"test@test.fr\"}")).andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnA500(){
            // Throw a random error not caught
            doThrow(new ArrayIndexOutOfBoundsException()).when(userService).forgotPassword(any());
            try {
                mvc.perform(post("/api/user/forgotPassword").contentType("application/json").content("{\"email\":\"test@test.fr\"}"));
            } catch (Exception e) {
                // Means that the exception is not caught by the system and will be propagated to Spring boot
                assertThat(e).isInstanceOf(ServletException.class);
            }

        }
    }

    @Nested
    class withGlobalExceptionHandler {
        @BeforeEach
        void setUp() {
            var controller = new UserController(userService, mock(AuthenticationFacade.class));
            mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
        }

        @Test
        void shouldReturnA404() throws Exception {
            doThrow(new UserNotFoundException("user not found")).when(userService).forgotPassword(any());
            mvc.perform(post("/api/user/forgotPassword").contentType("application/json").content("{\"email\":\"test@test.fr\"}")).andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnA500() throws Exception {
            doThrow(new ArrayIndexOutOfBoundsException()).when(userService).forgotPassword(any());
            // Now unhandled exception are caught by the GlobalExceptionHandler
            mvc.perform(post("/api/user/forgotPassword").contentType("application/json").content("{\"email\":\"test@test.fr\"}")).andExpect(status().isInternalServerError());
        }
    }

}
