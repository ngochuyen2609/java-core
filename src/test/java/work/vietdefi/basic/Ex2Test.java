package work.vietdefi.basic;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class Ex2Test {

    @Test
    @DisplayName("Test sum")
    public void testSum() {
        Ex2 ex2 = new Ex2();

        assertEquals(15, ex2.sum(5));
        assertEquals(55, ex2.sum(10));
    }
}
