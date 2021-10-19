package be.vdab.luigi.services;

import be.vdab.luigi.restclients.KoersClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service //om bean te creëren, dit zorgt voor andere effect als je deze code in test uitvoert dan de echte applicatie
class EuroService {
    private final KoersClient koersClient;
    EuroService(KoersClient koersClient) { // de koersClient is in testmode een dummy, in echte uitvoering gaat hij deze
        this.koersClient = koersClient; // zoeken via bean.
    }
    public BigDecimal naarDollar(BigDecimal euro) {
        return euro.multiply(koersClient.getDollarKoers()).setScale(2, RoundingMode.HALF_UP);
    }
}
