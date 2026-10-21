package fr.ulco.hotelbooking.dto;

public record DashboardDto(
        long nombreReservations,
        long nombreClients,
        long nombreChambres,
        long nombreSalles
) {
}