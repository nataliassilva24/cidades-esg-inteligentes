package br.com.fiap.healthtrack.domain.dto.response;

public record TokenResponse(
        String token,
        String tipo,
        long expiresIn
) {
    public static TokenResponse of(String token, long expiresIn) {
        return new TokenResponse(token, "Bearer", expiresIn);
    }
}
