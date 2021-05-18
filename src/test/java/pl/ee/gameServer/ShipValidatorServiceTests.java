package pl.ee.gameServer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.ee.gameServer.service.ShipValidatorService;
import static org.assertj.core.api.Assertions.*;

public class ShipValidatorServiceTests {

    char[][] validGameMap = new char[5][5];
    char[][] invalidGameMap = new char[5][5];

    @BeforeEach
    void setTestMaps(){
        validGameMap[0][0]='a';
        validGameMap[0][1]='a';
        validGameMap[0][2]='a';
        validGameMap[0][3]='a';

        validGameMap[4][1]='b';
        validGameMap[3][1]='b';

        //ships a and c are in contact
        invalidGameMap[0][1]='a';
        invalidGameMap[0][2]='a';
        invalidGameMap[0][3]='a';

        invalidGameMap[4][1]='b';
        invalidGameMap[3][1]='b';

        invalidGameMap[1][1]='c';
        invalidGameMap[1][2]='c';
    }


    @Test
    void testValidateShipPlacement() {
        assertThat(ShipValidatorService.validateShipPlacement(validGameMap)).isEqualTo(true);
        assertThat(ShipValidatorService.validateShipPlacement(invalidGameMap)).isEqualTo(false);
    }

}
