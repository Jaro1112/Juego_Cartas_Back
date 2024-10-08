package com.elementalcards.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError() {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", "Página no encontrada");
        return modelAndView;
    }
}
