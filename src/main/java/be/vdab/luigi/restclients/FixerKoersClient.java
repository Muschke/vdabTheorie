package be.vdab.luigi.restclients;

import be.vdab.luigi.exceptions.KoersClientException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/*We hebben 2 clients, spring weet niet welke te kiezen @Primary zou dit oplossen, maar dat werkt enkel bij 2 opties
wij werken het daarom uit met @Qualifier, de keuze hiertussen word in de service bepaald*/
@Qualifier("Fixer")
@Component
class FixerKoersClient implements KoersClient {
    private final static Pattern PATTERN = Pattern.compile("^.*\"USD\": *(\\d+\\.?\\d*).*$");
    private final URL url;

    /* Deze constructor is niet aan te raden omdat URL kunnen wijzigen, plaats je urls in ja application.properties
    FixerKoersClient() {
        try {
            url = new URL("http://data.fixer.io/api/latest?access_key=50c41e4529e328e0e462ffc591f1feae&symbols=USD");
        } catch (MalformedURLException ex) {
            throw new KoersClientException("Fixer URL is verkeerd");
        }
    }
    een beter constructor(duidelijk veel minder code):
     */

    FixerKoersClient(@Value("${fixerKoersURL}") URL url) {
        this.url = url;
    }
    @Override
    public BigDecimal getDollarKoers() {
        try (var stream = url.openStream()) {
            var matcher = PATTERN.matcher(new String(stream.readAllBytes()));
            if (!matcher.matches()) {
                throw new KoersClientException("Fixer data ongeldig");
            }
            return new BigDecimal(matcher.group(1));
        } catch (IOException ex) {
            throw new KoersClientException("Kan koers niet lezen via Fixer.", ex);
        }
    }

}
