package pl.ee.gameServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.ee.gameServer.service.BoardService;
import pl.ee.gameServer.service.ShipValidatorService;
import static org.assertj.core.api.Assertions.*;

public class ShipValidatorServiceTests {

    char[][] validGameMap = new char[10][10];
    char[][] invalidGameMap = new char[10][10];

    @BeforeEach
    void setTestMaps() throws Exception {
        String validInput= "0000000000111100000000000000000000222000000000000000300500000030050004003005000400000500000000000000";
        validGameMap = BoardService.parseToCharArray(validInput);

        String invalidInput= "6600000000111100000000000000000000222000000000000000300500000030050004003005000400000500000000000000";
        invalidGameMap = BoardService.parseToCharArray(invalidInput);

    }


    @Test
    void testValidateShipPlacement() {
        assertThat(ShipValidatorService.validateShipPlacement(validGameMap)).isEqualTo(true);
        assertThat(ShipValidatorService.validateShipPlacement(invalidGameMap)).isEqualTo(false);
    }

}
