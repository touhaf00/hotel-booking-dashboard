package fr.ulco.hotelbooking.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.ulco.hotelbooking.service.DashboardService;
import fr.ulco.hotelbooking.service.ReservationService;

@Controller
public class DashboardWebController {

    private final DashboardService dashboardService;
    private final ReservationService reservationService;

    public DashboardWebController(DashboardService dashboardService, ReservationService reservationService) {
        this.dashboardService = dashboardService;
        this.reservationService = reservationService;
    }

    @GetMapping("/dashboard")
    public String afficherDashboard(Model model) {
        remplirModel(model);
        return "dashboard";
    }
    @GetMapping("/dashboard/contenu")
    public String actualiserDashboard(Model model) {
        remplirModel(model);
        return "dashboard :: contenu";
    }

    private void remplirModel(Model model) {
        model.addAttribute(
                "statistiques",
                dashboardService.obtenirStatistiques()
        );

        model.addAttribute(
                "dernieresReservations",
                reservationService.listerDernieres()
        );
    }
}