package fr.ulco.hotelbooking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ulco.hotelbooking.dto.ReservationDto;
import fr.ulco.hotelbooking.mapper.ReservationMapper;
import fr.ulco.hotelbooking.persistence.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    public List<ReservationDto> lister() {
        return reservationRepository
                .findAll(Sort.by("id").ascending())
                .stream()
                .map(reservationMapper::toDto)
                .toList();
    }

    public Optional<ReservationDto> trouverParId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "L'identifiant doit être strictement positif."
            );
        }

        return reservationRepository
                .findById(id)
                .map(reservationMapper::toDto);
    }
}