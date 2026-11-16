package fr.ulco.hotelbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConnexionDto(
        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        String motDePasse
) {

    @Override
    public String toString() {
        return "ConnexionDto[identifiants masques]";
    }
}