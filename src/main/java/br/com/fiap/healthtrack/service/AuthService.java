package br.com.fiap.healthtrack.service;

import br.com.fiap.healthtrack.domain.dto.request.LoginRequest;
import br.com.fiap.healthtrack.domain.dto.request.RegisterRequest;
import br.com.fiap.healthtrack.domain.dto.response.TokenResponse;
import br.com.fiap.healthtrack.domain.entity.Usuario;
import br.com.fiap.healthtrack.domain.repository.UsuarioRepository;
import br.com.fiap.healthtrack.security.JwtService;
import jakarta.validation.ValidationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ValidationException("Credenciais inválidas"));

        String token = jwtService.generateToken(usuario);
        return TokenResponse.of(token, jwtService.getExpiration());
    }

    public TokenResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ValidationException("E-mail já cadastrado: " + request.email());
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .tipoUsuario(request.tipoUsuario())
                .build();

        usuarioRepository.save(usuario);
        String token = jwtService.generateToken(usuario);
        return TokenResponse.of(token, jwtService.getExpiration());
    }
}
