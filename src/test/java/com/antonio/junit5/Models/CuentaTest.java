package com.antonio.junit5.Models;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.antonio.junit5.exceptions.DineroInsuficienteException;

public class CuentaTest {
    Cuenta cuenta;

    @BeforeEach
    void initMetodoTest() {
        this.cuenta = new Cuenta("Andrés", new BigDecimal(1000.12345));
        System.out.println("Iniciando el método");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando el método de prueba.");
    }

    @Nested
    @DisplayName("Probando atributos de la cuenta")
    class CuentaTestNombreSaldo {
        @Test
        @DisplayName("el nombre")
        void testNombreCuenta() {
            String esperado = "Andrés";
            String real;

            real = cuenta.getPersona();

            Assertions.assertEquals(esperado, real);
        }

        @Test
        void testSaldoCuenta() {

            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        void testReferenciaCuenta() {
            cuenta = new Cuenta("John Doe", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("8900.9997"));

            assertEquals(cuenta, cuenta2);
        }
    }

    @Nested
    class CuentaOperacionesTest {
        @Test
        void testDebitoCuenta() {
            cuenta = new Cuenta("Andés", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(100));

            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testCreditoCuenta() {
            cuenta.credito(new BigDecimal(100));

            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.1234500000000480213202536106109619140625", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Andrés", new BigDecimal("1500.8989"));

            Banco banco = new Banco();
            banco.setNombre("Banco del Estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal("500"));

            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }

    @Test
    void testDineroInsuficienteExceptionCuenta() {
        String esperado = "Dinero Insuficiente";
        String actual;

        Exception exception = assertThrows(DineroInsuficienteException.class,
                () -> cuenta.debito(new BigDecimal(1500)));
        actual = exception.getMessage();

        assertEquals(esperado, actual);

    }

    @Test
    void testRelacionBancoCuentas() {
        Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Andrés", new BigDecimal("1500.8989"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);

        banco.setNombre("Banco del Estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal("500"));

        assertAll(() -> {
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
        }, () -> {
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }, () -> {
            assertEquals(2, banco.getCuentas().size());
        }, () -> {
            assertEquals("Banco del Estado", cuenta1.getBanco().getNombre());
        }, () -> {
            assertEquals("Andrés", banco.getCuentas().stream().filter(c -> c.getPersona().equals("Andrés")).findFirst()
                    .get().getPersona());
        }, () -> {
            assertTrue(banco.getCuentas().stream().anyMatch(c -> c.getPersona().equals("John Doe")));
        });
    }

    @RepeatedTest(value = 5, name = "Repetición numero {currentRepetition} de {totalRepetitions}")
    void testDebitoCuentaRepetir(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3) {
            System.out.println("Estamos en la repeticion " + info.getCurrentRepetition());
        }
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
    }

    @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @ValueSource(strings = { "100", "200", "300", "500", "700", "1000.12346" })
    void testDebitoCuentaParametrizado(String monto) {
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @CsvSource({ "1,100", "2,200", "3,300", "4,500", "5,700", "6,1000.12346" })
    void testDebitoCuentaCsvSource(String index, String monto) {
        System.out.println(index + " -> " + monto);
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @MethodSource("montoList")
    void testDebitoCuentaMethodSource(String monto) {
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    static private List<String> montoList() {
        return Arrays.asList("100", "200", "300", "500", "700", "1000.12346");
    }
}
