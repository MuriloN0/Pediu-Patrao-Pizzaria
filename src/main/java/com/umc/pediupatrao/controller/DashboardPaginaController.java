package com.umc.pediupatrao.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardPaginaController {

    @GetMapping("/dashboard")
    public String abrirDashboard() {

        return "dashboard";
    }
}