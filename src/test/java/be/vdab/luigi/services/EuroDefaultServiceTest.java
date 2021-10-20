package be.vdab.luigi.services;

import be.vdab.luigi.restclients.KoersClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) //Oproepen van mockito
class EuroDefaultServiceTest {
    @Mock
    private KoersClient koersClient; // creatie mock object van type koersclient
    private EuroDefaultService euroService; // creatie mock object van type euroService
    @BeforeEach
    void beforeEach() {
        euroService = new EuroDefaultService(koersClient); // toewijzen van mockobject koersclient aan EuroService constructor
    }
    @Test //wat we effectief gaan testen
    void naarDollar() {
        when(koersClient.getDollarKoers()).thenReturn(BigDecimal.valueOf(1.5)); // hier zorgen we dus voor implementatie van dummy koers 1,5
        assertThat(euroService.naarDollar(BigDecimal.valueOf(2))).isEqualByComparingTo("3");
        verify(koersClient).getDollarKoers(); /*controleert .. test dat methode getDollarKoers uit de interface Koersclient is opgeroepen
        als dat niet het geval is, gaat het ook niet werken in de applicatie, enkel met de dummy */
    }
}