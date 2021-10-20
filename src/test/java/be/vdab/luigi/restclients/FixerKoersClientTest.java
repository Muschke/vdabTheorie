package be.vdab.luigi.restclients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@PropertySource("application.properties")
@Import(FixerKoersClient.class)
class FixerKoersClientTest {
    private FixerKoersClient client;
    @BeforeEach
    void beforeEach() {
        client = new FixerKoersClient();
    }
    @Test
    void deKoersIsPositief() {
        assertThat(client.getDollarKoers()).isPositive();
    }
}