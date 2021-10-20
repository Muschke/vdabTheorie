package be.vdab.luigi.services;

import be.vdab.luigi.exceptions.KoersClientException;
import be.vdab.luigi.restclients.KoersClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


/*Syntax om alle restclients te injecteren en de eerste beste die werkt te gebruiken*/

@Service
class EuroDefaultService implements EuroService {
    private final KoersClient[] koersClients;

    EuroDefaultService(KoersClient[] koersClients) {
        this.koersClients = koersClients;
    }

    @Override
    public BigDecimal naarDollar(BigDecimal euro) {
        Exception laatste = null; //Je gaat itereren door je restclients, stel je nu voor dat je er 7 hebt en ze geven allemaal fout
        for (var client : koersClients) {
            try {
                return euro.multiply(client.getDollarKoers()).setScale(2, RoundingMode.HALF_UP);
            } catch (KoersClientException ex) {
                laatste = ex; //we slaan de foutmelding op en enkel de laatste, anders zou je foutmelding krijgen vanaf de eerste die faalt, wat we net proberen te vermijden
            }
        }
        throw new KoersClientException("Kan dollarKoers nergens lezen", laatste); /* als ze allemaal fout geven, geef je dit aan, en geven we nog de laatste
         opgeslagen foutmelding mee door */
    }
}


/*   Dit is syntax te gebruiken wanneer je manueel wil kiezen welke restclient er gebruikt wordt adhv qualifier

@Service //om bean te creëren, dit zorgt voor andere effect als je deze code in test uitvoert dan de echte applicatie
class EuroDefaultService implements EuroService {
    private final KoersClient koersClient;
//via @Qualifier kan je hier kiezen welke Koersklient in de service geinjecteerd word
    EuroDefaultService(@Qualifier("Fixer") KoersClient koersClient) { // de koersClient is in testmode een dummy, in echte uitvoering gaat hij deze
        this.koersClient = koersClient; // zoeken via bean.
    }
    @Override
    public BigDecimal naarDollar(BigDecimal euro) {
        return euro.multiply(koersClient.getDollarKoers()).setScale(2, RoundingMode.HALF_UP);
    }
}
*/