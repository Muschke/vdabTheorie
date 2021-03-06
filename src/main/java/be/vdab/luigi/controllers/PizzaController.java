package be.vdab.luigi.controllers;

import be.vdab.luigi.domain.Pizza;
import be.vdab.luigi.exceptions.KoersClientException;
import be.vdab.luigi.forms.VanTotPrijsForm;
import be.vdab.luigi.services.EuroService;
import be.vdab.luigi.services.PizzaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;


@Controller
@RequestMapping("pizzas")

class PizzaController {
    /* Onderstaande stond er altijd en enkel maar in voor de sake van de uitleg van de controllers in het begin. Cursus
    legt gewoon dom uit. Ze zouden beter eerst deel 3, dan deel 2 en dan pas controllers uitleggen

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
    NIET HIER dit wordt hier verkeerdelijk gezegd omdat

    * de cursus gatachterlijk is en niet uitlegt vanaf het begint dat deze methode in repositiries wordt gedaan
    *
    variabele bij getmapping prijzen, filtering van unieke prijzen in lijst
    *
    private List<BigDecimal> uniekePrijzen() {
        return Arrays.stream(pizzas).map(Pizza::getPrijs).distinct().sorted()
                .collect(Collectors.toList());
    }
    variabele om lijst te krijgen met pizzas van elke unieke prijs
    *
    private List<Pizza> pizzasMetPrijs(BigDecimal prijs) {
        return Arrays.stream(pizzas)
                .filter(pizza -> pizza.getPrijs().compareTo(prijs) == 0)
                .collect(Collectors.toList());
    }*/

    /*Logger --> Dat is voor logging te voorzien in je applicatie. Is vooral belangrijk voor applicaties in productie.
    Is in de plek van een Sytem.out. Dat gebruik je in webapplicaties niet meer.*/
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /*Oproepen van EuroService in deze controller*/
    private final EuroService euroService;
    /*Oproepen van PizzaService in deze controller, alles boven in comment wordt hierdoor vervangen*/
    private final PizzaService pizzaService;
    /*constructor*/
    PizzaController(EuroService euroService, PizzaService pizzaService) {
        this.euroService = euroService;
        this.pizzaService = pizzaService;
    }


    //METHODES CONTROLLER alles wordt plots simpeler omdat het nu correct wordt gebruikt:
    @GetMapping
    public ModelAndView pizzas() {
        return new ModelAndView("pizzas", "pizzas", pizzaService.findAll());
    }
    @GetMapping("{id}")
    public ModelAndView pizza(@PathVariable long id) {
        var modelAndView = new ModelAndView("pizza");
        pizzaService.findByIdLong(id)
                .ifPresent(pizza -> {
                    modelAndView.addObject("pizza", pizza);
        try {
            modelAndView.addObject("InDollar", euroService.naarDollar(pizza.getPrijs()));
        } catch (KoersClientException ex) {
                logger.error("Kan dollar koers niet lezen", ex);
            }
        });
        return modelAndView;
    }
    /*getmapping voor prijzen, geeft lijst weer in die pagina met unieke prijzen*/
    @GetMapping("prijzen")
    public ModelAndView prijzen() {
        return new ModelAndView("prijzen", "prijzen", pizzaService.findUniekePrijzen());
    }
    /*getmapping voor elke unieke prijspagina*/
    @GetMapping("prijzen/{prijs}")
    public ModelAndView pizzasMetEenPrijs(@PathVariable BigDecimal prijs) {
        return  new ModelAndView("prijzen", "pizzas", pizzaService.findByPrijs(prijs))
                .addObject("prijzen", pizzaService.findUniekePrijzen());
    }
    //methode om DTO te implementeren
    @GetMapping("aantalPizzasPerPrijs")
    public ModelAndView aantalPizzasPerPrijs() {
        return new ModelAndView("aantalPizzasPerPrijs", "aantalpizzasperprijs", pizzaService.findAantalPizzasPerPrijs());
    }

    //om form aan vantotprijs.html door te geven
    @GetMapping("vantotprijs/form")
    public ModelAndView vanTotPrijsForm() {
        return new ModelAndView("vantotprijs")
                .addObject(new VanTotPrijsForm(BigDecimal.ONE, BigDecimal.TEN));
        /*Als je lege invulvakken wenst, geef je tweemaal null in*/
    }
    /*verwerken van request van ingevulde form
    *   je gaat hier ook errors opvangen, zodat je niet op errorpagina terechtkomt wanneer iemand foute waardes in je formulier ingeeft*/
    @GetMapping("vantotprijs")
    public ModelAndView vanTotPrijs(@Valid VanTotPrijsForm form, Errors errors) {
        var modelAndView = new ModelAndView("vantotprijs");
        /*met onderstaande if-clause vermijden we dat een fout ingevuld formulier leid tot een foutpagina, je wordt dan
        * gewoon teruggeleid naar het formulier. Het lijkt als gebruiker alsof hij op de pagina blijft */
        if(errors.hasErrors()) {
            return modelAndView;
        }
        return modelAndView.addObject("pizzas", pizzaService.findByPrijsBetween(form.getVan(), form.getTot()));
    }
    /*om form aan pizzaToevoegen door te geven, voor uiteindelijk POST request*/
    @GetMapping("toevoegen/form")
    public ModelAndView toevoegenForm() {
        return new ModelAndView("toevoegen").addObject(new Pizza(0,"",null,false));
    }
    /*methode om post form request te verwerken*/
    @PostMapping
    public String toevoegen(@Valid Pizza pizza, Errors errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "toevoegen";
        }
        redirect.addAttribute("idNieuwePizza", pizzaService.create(pizza));
        return "redirect:/pizzas";
    }
}
