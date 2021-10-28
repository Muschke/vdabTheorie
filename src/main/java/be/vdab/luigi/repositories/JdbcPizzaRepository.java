package be.vdab.luigi.repositories;

import be.vdab.luigi.DTO.AantalPizzasPerPrijs;
import be.vdab.luigi.domain.Pizza;
import be.vdab.luigi.exceptions.PizzaNietGevondenException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/*In een repository staat 1 methode gelijke aa n1 sql transactie, als je er meerdere wilt gebruiken in 1 actie
* moet je de methods oproepen in je services*/

@Repository
class JdbcPizzaRepository implements PizzaRepository{
    private final JdbcTemplate template; //om uw bean (@repository) te injecteren, nu kan je gegevens selecteren en updaten
    private final SimpleJdbcInsert insert; //Om records toe te kunnen voegen
    /*Hieronder maken we een rowmapper, die mapt alle gegevens vh object in 1 rij op de juiste manier, zo kan je makkelijk lijsten maken*/
    private final RowMapper<Pizza> pizzaMapper = (result, rowNum) -> new Pizza(result.getLong("id"), result.getString("naam"), result.getBigDecimal("prijs"), result.getBoolean("pikant"));
    /*Voor elke soort rijweergave heb je aparte mapper nodig, deze is dus om de unieke prijzen weer te geven, rijen met enkel de kolom prijs*/
    private final RowMapper<BigDecimal> prijsMapper = (result, rowNum) -> result.getBigDecimal("prijs");
    JdbcPizzaRepository(JdbcTemplate template) {
        this.template = template;
        insert = new SimpleJdbcInsert(template).withTableName("pizzas").usingGeneratedKeyColumns("id");
        /*voor SimpleJdbcInsert, je moet de tabel benoemen, en als een kolom met PK automatisch nummert, ook die benoemen */
    }

    @Override
    /*Deze methode werkt niet zoder de SimpleJdbcInsert*/
    public long create(Pizza pizza) {
        return insert.executeAndReturnKey(Map.of("naam", pizza.getNaam(), "prijs", pizza.getPrijs(), "pikant", pizza.isPikant())).longValue();
        /*executeAndReturnKey voegt een record toe. De parameter is een Map. Die bevat een entry per kolom van het toe te voegen record. De key is een kolom naam.
        De value is de waarde die je invult in die kolom. De method maakt zelf een SQL insert statement en voert dit uit.
        De method geeft de automatisch gegenereerde primary key waarde als een Number. Jij neemt daarvan de long waarde.*/
    }

    @Override
    /*het updaten van meerdere records*/
    public void update(Pizza pizza) {
        var sql = "update pizzas set naam=?, prijs=?, pikant=? where id = ?";
        if(template.update(sql, pizza.getNaam(), pizza.getPrijs(), pizza.isPikant(), pizza.getId()) == 0){
        /*Bij meerdere statements doe je hetzelfde en geef je de parameters chronologisch in*/
        throw new PizzaNietGevondenException();
        }
    }

    @Override
    public void delete(long id) {
        template.update("delete from pizzas where id = ?", id);
        /*tweede parameter is de waarde van wat in "?" ingegeven word*/
    }

    @Override
    /*Om dit efficient te doen moet je dus eerst je rowmapper maken*/
    public List<Pizza> findAll() {
        var sql = "select id, naam, prijs, pikant from pizzas order by id";
        return template.query(sql, pizzaMapper);
    }

    @Override
    /*hier gaan we hetzelfde doen als de list al, maar nu met parameters. Je geeft gewoon de where prijs =? als parameter en geeft
    * de waardes dan in na de sql en mapper*/
    public List<Pizza> findPrijsBetween(BigDecimal van, BigDecimal tot) {
        var sql = "select id, naam, prijs, pikant from pizzas where prijs between ? and ? order by prijs";
        return template.query(sql, pizzaMapper, van, tot);
    }

    @Override
    /*Het lezen van 1 record, je gebruikt optional zodat je geen null waarde terugkrijgt, er komt iets of er komt niets, komt er niets, wil je exceptie krijgen die
    * aangeeft dat hij de waarde niet kan  vinden*/
    public Optional<Pizza> findById(long id) {
        /*! ALLES in tryblock*/
        try {
            var sql = "select id, naam, prijs, pikant from pizzas where id = ? ";
            return Optional.of(template.queryForObject(sql, pizzaMapper, id));
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }


    @Override
    //Scalar Value --> is het resultaat van een select statement met 1 rij en 1 kolom.
    public long findAantal() {
        return template.queryForObject("select count(*) from pizzas", Long.class);
        /*tweede parameteer is het type van je returnwaarde*/
    }

    @Override
    /*aparte rowmapper nodig, deze zoekt gewoon lijst van de prijzen, eigenlijk echt domme opzoeking*/
    public List<BigDecimal> findUniekePrijzen() {
        return template.query("select distinct prijs from pizzas order by prijs", prijsMapper);
    }

    @Override
    public List<Pizza> findByPrijs(BigDecimal prijs) {
        var sql = "select id, naam, prijs, pikant from pizzas where prijs = ? order by naam";
        return template.query(sql, pizzaMapper,prijs);
    }

    @Override
    public List<Pizza> findByIds(Set<Long> ids) {
        if(ids.isEmpty()) {
            return List.of();
        }
        /*Als de verzameling id's leeg is geef je lege lijst terug, je doet geen opzoeking*/
        var sql = "select id, naam, prijs, pikant from pizzas where id in (" +
                "?,".repeat(ids.size()-1) +
                "?) order by id";
        return template.query(sql, pizzaMapper, ids.toArray());
        /*De logica hiervan is moeilijk te begrijpen en kan je best gewoon vanbuiten leren*/
    }

    //voor DTO
    @Override
    public List<AantalPizzasPerPrijs> findAantalPizzasPerPrijs() {
        var sql = "select prijs, count(*) as aantal from pizzas group by prijs order by prijs";
        /*Onderstaande variabele zou je ook vanboven tussen de variabele kunnen plaatsen. Omdat we hem maar 1 keer gebruiken
        * plaatsen we hem hier*/
        RowMapper<AantalPizzasPerPrijs> mapper = (result, rowNum) ->
                new AantalPizzasPerPrijs(result.getBigDecimal("prijs"), result.getInt("aantal"));
        return template.query(sql,mapper);
    }
}
