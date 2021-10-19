package be.vdab.luigi.controllers;

import be.vdab.luigi.domain.Pizza;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;


@Controller
@RequestMapping("pizzas")

class PizzaController {
    private final Pizza[] pizzas = {
            new Pizza(1 , "Proscuitto", BigDecimal.valueOf(4), false),
            new Pizza(2, "Margherita", BigDecimal.valueOf(5), false),
            new Pizza(3 , "Calzone", BigDecimal.valueOf(4), false),
            new Pizza(4, "PestoPaprika", BigDecimal.valueOf(7), true),
            new Pizza(5 , "Di Mare", BigDecimal.valueOf(7), false),
            new Pizza(6, "Turkish", BigDecimal.valueOf(6), true),
            new Pizza(7 , "OliveSpecial", BigDecimal.valueOf(6), false),
            new Pizza(8, "ChickenBbq", BigDecimal.valueOf(7), true)
    };
    /*variabele bij getmapping prijzen, filtering van unieke prijzen in lijst*/
    private List<BigDecimal> uniekePrijzen() {
        return Arrays.stream(pizzas).map(Pizza::getPrijs).distinct().sorted()
                .collect(Collectors.toList());
    }
    /*variabele om lijst te krijgen met pizzas van elke unieke prijs*/
    private List<Pizza> pizzasMetPrijs(BigDecimal prijs) {
        return Arrays.stream(pizzas)
                .filter(pizza -> pizza.getPrijs().compareTo(prijs) == 0)
                .collect(Collectors.toList());
    }
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
    /*getmapping voor prijzen, geeft lijst weer in die pagina met unieke prijzen*/
    @GetMapping("prijzen")
    public ModelAndView prijzen() {
        return new ModelAndView("prijzen", "prijzen", uniekePrijzen());
    }
    /*getmapping voor elke unieke prijspagina*/
    @GetMapping("prijzen/{prijs}")
    public ModelAndView pizzasMetEenPrijs(@PathVariable BigDecimal prijs) {
        return  new ModelAndView("prijzen", "pizzas", pizzasMetPrijs(prijs))
                .addObject("prijzen", uniekePrijzen());
    }
}
