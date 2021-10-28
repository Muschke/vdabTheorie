package be.vdab.luigi.controllers;


import be.vdab.luigi.sessions.Identificatie;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
/*Session data je in elke controller nodig hebt. In tegenstelling tot onze identificatieController geven we hier geen pagina
* aan. In theorie zou je dit dus nergens kunnen oproepen. Maar dankzij ControllerAdvice en ModelAttribute wel. We gaan deze dus
* in index proberen oproepen */
class MyControllerAdvice {
    private final Identificatie identificatie;

    MyControllerAdvice(Identificatie identificatie) {
        this.identificatie = identificatie;
    }

    @ModelAttribute
    void extraDataToevoegenAanModel(Model model) {
        model.addAttribute(identificatie);
    }
}
