package be.vdab.luigi.controllers;

import be.vdab.luigi.domain.Pizza;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.Arrays;

@Controller
@RequestMapping("pizzas")

class PizzaController {
    private final Pizza[] pizzas = {
            new Pizza(1 , "Proscuitto", BigDecimal.valueOf(4), false),
            new Pizza(2, "Margherita", BigDecimal.valueOf(5), false),
            new Pizza(3 , "Calzone", BigDecimal.valueOf(4), false),
            new Pizza(4, "PestoPaprika", BigDecimal.valueOf(7), true)
    };
    @GetMapping
    public ModelAndView pizzas() {
        return new ModelAndView("pizzas", "pizzas", pizzas);
    }
    @GetMapping("{id}")
    public ModelAndView pizza(@PathVariable long id) {
        var modelAndView = new ModelAndView("pizza");
        Arrays.stream(pizzas).filter(pizza -> pizza.getId() == id).findFirst()
                .ifPresent(pizza -> modelAndView.addObject("pizza", pizza));
        return modelAndView;
    }
}
