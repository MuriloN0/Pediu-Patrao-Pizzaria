package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.service.AuditoriaService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/auditoria")
    public String listar(Model model) {
        model.addAttribute("eventos", auditoriaService.listar());
        model.addAttribute("content", "auditoria/lista :: content");

        return "layout";
    }
}