package be.vdab.luigi.domain;

public class Adres {
    private final String Straat;
    private final String huisNr;
    private final int postcode;
    private final String gemeente;

    public String getStraat() {
        return Straat;
    }

    public String getHuisNr() {
        return huisNr;
    }

    public int getPostcode() {
        return postcode;
    }

    public String getGemeente() {
        return gemeente;
    }

    public Adres(String straat, String huisNr, int postcode, String gemeente) {
        Straat = straat;
        this.huisNr = huisNr;
        this.postcode = postcode;
        this.gemeente = gemeente;
    }
}
