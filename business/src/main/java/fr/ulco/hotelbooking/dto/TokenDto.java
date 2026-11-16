package fr.ulco.hotelbooking.dto;

public record TokenDto(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    @Override
    public String toString() {
        return "TokenDto[tokenType=" + tokenType
                + ", expiresIn=" + expiresIn + "]";
    }
}