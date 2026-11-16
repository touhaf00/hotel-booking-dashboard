package fr.ulco.hotelbooking.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ulco.hotelbooking.persistence.entity.Compte;
import fr.ulco.hotelbooking.persistence.repository.CompteRepository;

@Service
@Transactional(readOnly = true)
public class CompteUserDetailsService implements UserDetailsService {

    private final CompteRepository compteRepository;

    public CompteUserDetailsService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException(
                    "Identifiants invalides."
            );
        }

        Compte compte = compteRepository.findByEmail(email.strip())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Identifiants invalides."
                ));

        String nomRole = compte.getRole().getNom();

        String autorite = nomRole.startsWith("ROLE_")
                ? nomRole
                : "ROLE_" + nomRole;

        return User.withUsername(compte.getEmail())
                .password(compte.getMotDePasse())
                .authorities(autorite)
                .build();
    }
}