package be.vdab.luigi.restclients;

import be.vdab.luigi.exceptions.KoersClientException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;

/*Wanneer je de data zelf opent, zie je dat de data in xmlns is of XML, onderstaande legt uit hoe je deze moet lezen*/

/*We hebben 2 clients, spring weet niet welke te kiezen @Primary zou dit oplossen, maar dat werkt enkel bij 2 opties
wij werken het daarom uit met @Qualifier, de keuze hiertussen word in de service bepaald*/
@Qualifier("ECB")
@Component
class ECBKoersClient implements KoersClient{
    private final URL url;
    private final XMLInputFactory factory = XMLInputFactory.newFactory();
    /*Deze factory is nodig om een XML stream reader te kunnen maken, die we later in de methodes zien*/

    /* Deze constructor is niet aan te raden omdat URL kunnen wijzigen, plaats je urls in ja application.properties
   

    ECBKoersClient() { //Constructor om url te initialiseren.
        try {
            url = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
        } catch (MalformedURLException ex) {
            throw new KoersClientException("ECB URL is verkeerd.", ex);
        }
    }
    betere manier voor contructor(duidelijk veel minder code):
*/

    ECBKoersClient(@Value("${ecbKoersURL}") URL url) {
        this.url = url;
    }

    @Override
    public BigDecimal getDollarKoers() {
        try (var stream = url.openStream()) {
            var reader = factory.createXMLStreamReader(stream);
            /*Hier maken we de xml reader uit de eerder gemaakte factory*/
            while(reader.hasNext()) { //while loop die controleert dat er nog volgend element is om te lezen
                reader.next(); //instructie om volgend element in te lezen
                if(reader.isStartElement()) { //als de reader het eerste element is, geeft deze true terug
                    if("USD".equals(reader.getAttributeValue(0))) { //controle of eerste element USD is
                        return new BigDecimal(reader.getAttributeValue(1)); // zoja, dollarkoers aflezen in volgende element
                    }
                }
            }
            throw new KoersClientException("XML van ECB bevat geen USD"); //hebben we alle eerste elementen gelezen en geen $ koers gevonden? --> error gooien
        } catch (IOException | NumberFormatException | XMLStreamException ex) {
            throw new KoersClientException("Kan koers niet lezen via ECB.", ex); // errors voor  algemeen try, Openstream en CreateXMLStreamReader
        }
    }
}
