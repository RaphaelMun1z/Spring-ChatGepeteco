package io.github.raphaelmun1z.ChatGepeteco.services;

import io.github.raphaelmun1z.ChatGepeteco.dtos.security.AccountCredentialsDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.security.SignUpRequestDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.security.TokenDTO;
import io.github.raphaelmun1z.ChatGepeteco.dtos.security.UsuarioResponseDTO;
import io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao.Papel;
import io.github.raphaelmun1z.ChatGepeteco.entities.usuario.Usuario;
import io.github.raphaelmun1z.ChatGepeteco.entities.usuario.UsuarioComum;
import io.github.raphaelmun1z.ChatGepeteco.exceptions.models.BadCredentialsException;
import io.github.raphaelmun1z.ChatGepeteco.exceptions.models.BusinessException;
import io.github.raphaelmun1z.ChatGepeteco.repositories.UsuarioRepository;
import io.github.raphaelmun1z.ChatGepeteco.repositories.autorizacao.PapelRepository;
import io.github.raphaelmun1z.ChatGepeteco.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PapelRepository papelRepository;

    public AuthService(
        AuthenticationManager authenticationManager,
        JwtTokenProvider tokenProvider,
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder, PapelRepository papelRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.papelRepository = papelRepository;
    }

    public TokenDTO signin(AccountCredentialsDTO credentials) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(credentials.email(), credentials.senha());
            var auth = authenticationManager.authenticate(usernamePassword);
            var user = (Usuario) auth.getPrincipal();
            return tokenProvider.criarTokenDTO(user);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Email ou senha inválidos.");
        }
    }

    public TokenDTO refreshToken(String email, String refreshToken) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("E-mail " + email + " não encontrado."));
        return tokenProvider.atualizarToken(refreshToken, usuario);
    }

    @Transactional
    public UsuarioResponseDTO signup(SignUpRequestDTO dto) {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException("O email informado já está em uso.");
        }

        Papel papelPadrao = papelRepository.findByNome("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("Erro: Papel ROLE_USER não existe"));

        UsuarioComum usuario = new UsuarioComum(
            dto.nome(),
            dto.email(),
            passwordEncoder.encode(dto.senha()),
            papelPadrao
        );
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return UsuarioResponseDTO.fromEntity(usuarioSalvo);
    }
}
