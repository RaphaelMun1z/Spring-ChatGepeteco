package io.github.raphaelmun1z.ChatGepeteco.controllers;


import io.github.raphaelmun1z.ChatGepeteco.controllers.docs.AuthControllerDocs;
import io.github.raphaelmun1z.ChatGepeteco.dtos.security.AccountCredentialsDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.security.SignUpRequestDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.security.TokenDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.security.UsuarioResponseDTO;
import io.github.raphaelmun1z.ChatGepeteco.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController implements AuthControllerDocs {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    @Override
    public ResponseEntity<TokenDTO> signin(@RequestBody @Valid AccountCredentialsDTO credentials) {
        return ResponseEntity.ok(authService.signin(credentials));
    }

    @PutMapping("/refresh/{email}")
    @Override
    public ResponseEntity<TokenDTO> refreshToken(
        @PathVariable("email") String email,
        @RequestHeader("Authorization") String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(email, refreshToken));
    }

    @PostMapping("/signup")
    @Override
    public ResponseEntity<UsuarioResponseDTO> signup(@RequestBody @Valid SignUpRequestDTO dto) {
        UsuarioResponseDTO novoUsuario = authService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }
}