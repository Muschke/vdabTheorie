package be.vdab.luigi.DTO;

import java.math.BigDecimal;

public class AantalPizzasPerPrijs {
    private final BigDecimal prijs;
    private final int aantal;

    public AantalPizzasPerPrijs(BigDecimal prijs, int aantal) {
        this.prijs = prijs;
        this.aantal = aantal;
    }

    public BigDecimal getPrijs() {
        return prijs;
    }

    public int getAantal() {
        return aantal;
    }
}
