package com.piaand.co2emissions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Default controller that redirects to the root of API when given non-matching path
 */
@Controller
public class DefaultController {
    @GetMapping("*")
    public String getDefault() {
        return "redirect:/";
    }
}
