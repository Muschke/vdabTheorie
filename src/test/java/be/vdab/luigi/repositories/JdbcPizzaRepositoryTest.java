package be.vdab.luigi.repositories;


import be.vdab.luigi.domain.Pizza;
import be.vdab.luigi.exceptions.PizzaNietGevondenException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@JdbcTest
@Import(JdbcPizzaRepository.class)
@Sql("/insertPizzas.sql")
class JdbcPizzaRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    /*The above is syntax, remembah*/
    private static final String PIZZAS = "pizzas";
    private final JdbcPizzaRepository repository;

    JdbcPizzaRepositoryTest(JdbcPizzaRepository repository) {
        this.repository = repository;
    }
    /*Bij het uitvoeren van élke test doet spring:
    *   - Transactie starten
    *   - insertPizza.sql uitvoeren (=insert into pizzas(naam, prijs, pikant) values ('test', 10, false), ('test2', 20, true);)
    *           !Dit word uitgevoerd bovenop de bestaande tabel in de test mode, dwz dat ook de originele records erin zitten
    *   - De test uitvoeren in die transactie
    *   - Rollback transactie (of ongedaan maken)
    * Dit is heel belangrijk gegeven. We gaan 2 private functies maken voor een test op te doen, normaal plaats je die van onder.
    * in de curus staan ze heel ongelukkig geplaatst en verwarrend, maar eigenlijk fungeren die functies meer als before each.
    * want voor elke test worden test en test2 ingevoegd in de tabel, dus als je daarvan iets wil opvragen in je test, je kan dat
    * best in private functie voorzien*/

    private long idVanTestPizza() {
        return jdbcTemplate.queryForObject("select id from pizzas where naam = 'test'", long.class);
    }
    private long idVanTest2Pizza() {
        return jdbcTemplate.queryForObject("select id from pizzas where naam = 'test2'", long.class);
    }

    @Test
    void findAantal() {
        assertThat(repository.findAantal()).isEqualTo(countRowsInTable(PIZZAS));
    }
    @Test
    void findAllGeeftAllePizzasGesorteerdOpId() {
        assertThat(repository.findAll()).hasSize(countRowsInTable(PIZZAS)).extracting(Pizza::getId).isSorted();

    }
    /*Volgende test gaan in onze insertPizzas.Sql nakijken*/
    @Test
    void create() {
        var id = repository.create(new Pizza(0, "test2", BigDecimal.TEN, false));
        /*Als de autonummering van ons systeem klopt, moet de ingave van het id 0 falen, en moet hij automatische positief nummeren*/
        assertThat(id).isPositive();
        /*Dat wordt hier getest*/
        assertThat(countRowsInTableWhere(PIZZAS, "id = " + id)).isOne();
        /*Je resultaat van opzoeking van unieke gegeven moet kan altijd maar 1 rij zijn*/
    }
    @Test
    void delete() {
        var id = idVanTestPizza();
        repository.delete(id);
        /*cursus(deze zoekt op of je het verwijderde id nog kan vinden):*/
        assertThat(countRowsInTableWhere(PIZZAS, "id = " + id)).isZero();
    }
    @Test
    void findById() {
        assertThat(repository.findById(idVanTestPizza()))
                /*hasValueSatisfying --> Om lambda op optional toe te passen*/
                .hasValueSatisfying(pizza -> assertThat(pizza.getNaam()).isEqualTo("test"));
                /* Anders ga je nooit uit de net gevonden rij de naam kunnen halen om te controleren*/
    }
    @Test
    void findByOnbestaandeIdVindtGeenPizza() {
        assertThat(repository.findById(-1)).isEmpty();
    }
    @Test
    void update() {
        var id = idVanTestPizza();
        /*de var pizza van de cursus is compleet nutteloos, de test slaagt op niets want je update de gegevens met crack dezelfde gegevens.
        * Best dus om effectief iets te wijzigen */
        var pizza = new Pizza(id, "test", BigDecimal.ONE, false);
        repository.update(pizza);
        assertThat(countRowsInTableWhere(PIZZAS, "prijs=1 and id=" + id)).isOne();
    }
    @Test
    void updateOnbestaandePizzaGeeftEenFout() {
        assertThatExceptionOfType(PizzaNietGevondenException.class).isThrownBy(
                () -> repository.update(new Pizza(-1, "test", BigDecimal.TEN, false)));
        /*Dit moet fout geven, want je probeert pizza met id -1 up te daten, want onmogelijk is*/
    }
    @Test
    void findByPrijsBetween() {
        var van = BigDecimal.ONE;
        var tot = BigDecimal.TEN;
        assertThat(repository.findPrijsBetween(van, tot))
                .hasSize(super.countRowsInTableWhere(PIZZAS, "prijs between 1 and 10"))
               /* .extracting(pizza -> pizza.getPrijs())  onderstaande is de lambda verkort */
                .extracting(Pizza::getPrijs)
                .allSatisfy(prijs -> assertThat(prijs).isBetween(van, tot));
                /*allSatisfy -> test alle objecten in lijst aan parameter*/
    }
    @Test
    void findUniekePrijzenGeeftPrijzenOplopend() {
        assertThat(repository.findUniekePrijzen())
                .hasSize(jdbcTemplate.queryForObject("select count(distinct prijs) from pizzas", Integer.class))
                /*Controle 1: we tellen aantal unieke prijzen en kijken of dit aantal even groot is als wat methode
                * findUniekePrijzen terugbrengt*/
                .doesNotHaveDuplicates() //controle 2: distinct, zijn ze uniek?
                .isSorted(); //controle 3: sortering
    }
    @Test
    void findByPrijs() {
        assertThat(repository.findByPrijs(BigDecimal.TEN))
                .hasSize(super.countRowsInTableWhere(PIZZAS, "prijs = 10"))
                /*hasSize super't iets van de class waaruit we extenden, ik weet ook niet wat*/
                .allSatisfy(pizza -> assertThat(pizza.getPrijs()).isEqualByComparingTo(BigDecimal.TEN))
                .extracting(Pizza::getNaam)
                .isSortedAccordingTo(String::compareToIgnoreCase);
                /*(String::compareToIgnoreCase) haalt gewoon de hoofdletters weg uit de sortering die je controleert
                * anders geeft die fout want hoofdletters worden voor kleine lettters gesorteerd (A, B, C, a, b, c,..).
                * SQL sorteert hoofdletter onafhankelijk*/
    }
    @Test
    void findByIds() {
        long id1 = idVanTestPizza();
        long id2 = idVanTest2Pizza();
        assertThat(repository.findByIds(Set.of(id1, id2)))
                .extracting(Pizza::getId)
                .containsOnly(id1, id2)
                .isSorted();
    }
    @Test
    void findByIdsGeeftLegeVerzamelingPizzasBijLegeVerzamelingIds() {
        assertThat(repository.findByIds(Set.of())).isEmpty();
    }
    @Test
    void findByIdsGeeftLegeVerzamelingPizzasBijOnbestaandeIds() {
        assertThat(repository.findByIds(Set.of(-1L, -2L))).isEmpty();
    }

    @Test
    void aantalPizzasPerPrijs() {
        /*Eerste statement voert de functie uit, hierin moet nu lijst zitten van aantal pizzas per prijs*/
        var aantalPizzasPerPrijs = repository.findAantalPizzasPerPrijs();
        /*Nu gaan we testen of dat klopt. De eerste test is verifiëren dat onze lijst maximaal evenveel rijen heeft
        * als er unieke prijzen zijn */
        assertThat(aantalPizzasPerPrijs).hasSize(super.jdbcTemplate.queryForObject(
                "select count(distinct prijs) from pizzas", Integer.class));
        /*Nu halen we de eerste rij op van onze lijst. Nu het aantal in deze rij moet gelijk zijn aan het totaal
        * aantal pizzas van die rij*/
        var eersteRij = aantalPizzasPerPrijs.get(0);
        assertThat(eersteRij.getAantal()).isEqualTo(super.countRowsInTableWhere(PIZZAS, "prijs =" + eersteRij.getPrijs()));
    }

}