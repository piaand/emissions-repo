package com.piaand.co2emissions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DefaultController {
    @GetMapping("*")
    public String getDefault() {
        return "redirect:/";
    }
}
