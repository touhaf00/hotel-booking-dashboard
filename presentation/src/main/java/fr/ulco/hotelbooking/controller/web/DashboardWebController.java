package fr.ulco.hotelbooking.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fr.ulco.hotelbooking.service.DashboardService;

@Controller
public class DashboardWebController {

    private final DashboardService dashboardService;

    public DashboardWebController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String afficherDashboard(Model model) {
        model.addAttribute(
                "statistiques",
                dashboardService.obtenirStatistiques()
        );

        return "dashboard";
    }
}