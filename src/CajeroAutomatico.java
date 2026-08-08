import java.util.Scanner;

public class CajeroAutomatico {

    // ---------------- DATOS INICIALES ----------------
    static final String TITULAR = "Jaime Alejandro Monzón Contreras";
    static final String NUMERO_CUENTA = "1049";
    static final int PIN_CORRECTO = 2026;
    static double saldo = 1000.00;
    static final double COMISION_OTRA_RED = 10.00;
    static final int MAX_INTENTOS = 3;

    static final double LIMITE_DEPOSITO = 5000.00;
    static final double LIMITE_RETIRO = 2000.00;
    static final double MULTIPLO_RETIRO = 20.00;

    // ---------------- CONTADORES Y ACUMULADORES DE LA SESIÓN ----------------
    static double saldoInicialSesion;
    static int depositosExitosos = 0;
    static double totalDepositado = 0.0;
    static int retirosExitosos = 0;
    static double totalRetirado = 0.0;
    static double totalComisiones = 0.0;
    static int operacionesRechazadas = 0;
    static int opcionesInvalidas = 0;

    static Scanner sc = new Scanner(System.in);

    // ================= MAIN (coordinador) =================
    public static void main(String[] args) {
        saldoInicialSesion = saldo;

        boolean accesoPermitido = validarAcceso();

        if (accesoPermitido) {
            mostrarBienvenida();
            ejecutarMenu();
        } else {
            System.out.println("\nHa superado el número máximo de intentos permitidos.");
            System.out.println("Cuenta bloqueada durante esta sesión. El programa finalizará.");
        }

        sc.close();
    }

    // ================= 1. CONTROL DE ACCESO =================
    static boolean validarAcceso() {
        boolean accesoConcedido = false;

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            System.out.print("Ingrese su PIN: ");
            int pinIngresado = leerEntero();

            if (pinIngresado == PIN_CORRECTO) {
                accesoConcedido = true;
                break; // uso funcional de break: termina el ciclo de intentos
            } else {
                int intentosRestantes = MAX_INTENTOS - intento;
                if (intentosRestantes > 0) {
                    System.out.println("PIN incorrecto. Intentos restantes: " + intentosRestantes);
                } else {
                    System.out.println("PIN incorrecto. No le quedan más intentos.");
                }
            }
        }
        return accesoConcedido;
    }

    static void mostrarBienvenida() {
        System.out.println("\n¡Bienvenido(a), " + TITULAR + "!");
        System.out.println("Acceso concedido a la cuenta " + NUMERO_CUENTA + ".\n");
    }

    // ================= 2. MENÚ PRINCIPAL =================
    static void ejecutarMenu() {
        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero();

            // condición compuesta
            if (opcion < 1 || opcion > 6) {
                System.out.println("Opción inválida. No existe esa opción en el menú.");
                opcionesInvalidas++;
                continue; // uso funcional de continue: regresa de inmediato al menú
            }

            switch (opcion) {
                case 1:
                    consultarSaldo();
                    break;
                case 2:
                    double montoDeposito = solicitarMontoDeposito();
                    procesarDeposito(montoDeposito);
                    break;
                case 3:
                    System.out.print("Ingrese el monto a retirar: Q");
                    double montoRetiroNormal = leerDouble();
                    procesarRetiro(montoRetiroNormal); // sobrecarga: retiro normal
                    break;
                case 4:
                    System.out.print("Ingrese el monto a retirar (otra red): Q");
                    double montoRetiroComision = leerDouble();
                    procesarRetiro(montoRetiroComision, COMISION_OTRA_RED); // sobrecarga: retiro con comisión
                    break;
                case 5:
                    mostrarResumen();
                    break;
                case 6:
                    mostrarResumen();
                    System.out.println("\nGracias por utilizar nuestro cajero automático, " + TITULAR + ".");
                    System.out.println("¡Hasta pronto!");
                    break;
            }

        } while (opcion != 6);
    }

    static void mostrarMenu() {
        System.out.println("===================================");
        System.out.println("        MENÚ - CAJERO AUTOMÁTICO");
        System.out.println("===================================");
        System.out.println("1. Consultar saldo");
        System.out.println("2. Depositar dinero");
        System.out.println("3. Realizar retiro normal");
        System.out.println("4. Realizar retiro con comisión");
        System.out.println("5. Mostrar resumen de la sesión");
        System.out.println("6. Salir");
        System.out.print("Seleccione una opción: ");
    }

    // ================= 3. CONSULTA DE SALDO =================
    static void consultarSaldo() {
        System.out.println("\n--- Consulta de saldo ---");
        System.out.println("Titular: " + TITULAR);
        System.out.println("Cuenta: " + NUMERO_CUENTA);
        System.out.printf("Saldo disponible: Q%.2f%n%n", saldo);
    }

    // ================= 4. DEPÓSITOS =================
    static double solicitarMontoDeposito() {
        boolean valido = false;
        double monto = 0;

        while (!valido) {
            System.out.print("Ingrese el monto a depositar: Q");
            monto = leerDouble();

            if (monto <= 0) {
                System.out.println("Monto inválido: debe ser mayor que Q0.00. Intente de nuevo.");
            } else if (monto > LIMITE_DEPOSITO) {
                System.out.println("Monto inválido: no puede superar Q5,000.00 por operación. Intente de nuevo.");
            } else {
                valido = true;
            }
        }
        return monto;
    }

    static void procesarDeposito(double monto) {
        double saldoAnterior = saldo;
        saldo += monto;

        depositosExitosos++;
        totalDepositado += monto;

        System.out.println("\nDepósito realizado exitosamente.");
        System.out.printf("Monto depositado: Q%.2f%n", monto);
        System.out.printf("Saldo anterior: Q%.2f%n", saldoAnterior);
        System.out.printf("Saldo actualizado: Q%.2f%n%n", saldo);
    }

    // ================= 5 y 6. RETIROS (MÉTODOS SOBRECARGADOS) =================

    // Versión 1: retiro normal (un solo parámetro)
    static void procesarRetiro(double monto) {
        String motivoRechazo = validarRetiro(monto, 0.0);

        if (motivoRechazo != null) {
            System.out.println("\nRetiro rechazado: " + motivoRechazo);
            operacionesRechazadas++;
            System.out.println();
            return;
        }

        double saldoAnterior = saldo;
        saldo -= monto;

        retirosExitosos++;
        totalRetirado += monto;

        System.out.println("\nRetiro normal realizado exitosamente.");
        System.out.printf("Monto solicitado: Q%.2f%n", monto);
        System.out.printf("Saldo anterior: Q%.2f%n", saldoAnterior);
        System.out.printf("Total debitado: Q%.2f%n", monto);
        System.out.printf("Saldo actualizado: Q%.2f%n%n", saldo);
    }

    // Versión 2: retiro con comisión (dos parámetros) -> sobrecarga válida
    static void procesarRetiro(double monto, double comision) {
        String motivoRechazo = validarRetiro(monto, comision);

        if (motivoRechazo != null) {
            System.out.println("\nRetiro rechazado: " + motivoRechazo);
            operacionesRechazadas++;
            System.out.println();
            return;
        }

        double saldoAnterior = saldo;
        double totalDebitado = monto + comision;
        saldo -= totalDebitado;

        retirosExitosos++;
        totalRetirado += monto;
        totalComisiones += comision;

        System.out.println("\nRetiro con comisión realizado exitosamente.");
        System.out.printf("Monto solicitado: Q%.2f%n", monto);
        System.out.printf("Comisión: Q%.2f%n", comision);
        System.out.printf("Total debitado: Q%.2f%n", totalDebitado);
        System.out.printf("Saldo anterior: Q%.2f%n", saldoAnterior);
        System.out.printf("Saldo actualizado: Q%.2f%n%n", saldo);
    }

    // Método adicional de validación/cálculo reutilizado por ambas versiones de retiro
    static String validarRetiro(double monto, double comisionAplicable) {
        if (monto <= 0) {
            return "el monto debe ser mayor que Q0.00.";
        }
        if (!esMultiploDeVeinte(monto)) {
            return "el monto debe ser múltiplo de Q20.00.";
        }
        if (monto > LIMITE_RETIRO) {
            return "el monto no puede superar Q2,000.00 por operación.";
        }
        if (monto + comisionAplicable > saldo) {
            if (comisionAplicable > 0) {
                return "el saldo no cubre el monto solicitado más la comisión.";
            } else {
                return "fondos insuficientes. El monto supera el saldo disponible.";
            }
        }
        return null; // significa que el monto es válido
    }

    // Método adicional con valor de retorno booleano
    static boolean esMultiploDeVeinte(double monto) {
        double resto = monto % MULTIPLO_RETIRO;
        return Math.abs(resto) < 0.001;
    }

    // ================= 8. RESUMEN DE LA SESIÓN =================
    static void mostrarResumen() {
        System.out.println("\n========== RESUMEN DE LA SESIÓN ==========");
        System.out.printf("Saldo inicial: Q%.2f%n", saldoInicialSesion);
        System.out.println("Depósitos exitosos: " + depositosExitosos);
        System.out.printf("Total depositado: Q%.2f%n", totalDepositado);
        System.out.println("Retiros exitosos: " + retirosExitosos);
        System.out.printf("Total entregado en retiros: Q%.2f%n", totalRetirado);
        System.out.printf("Total cobrado en comisiones: Q%.2f%n", totalComisiones);
        System.out.println("Operaciones rechazadas: " + operacionesRechazadas);
        System.out.println("Opciones inválidas seleccionadas: " + opcionesInvalidas);
        System.out.printf("Saldo actual: Q%.2f%n", saldo);
        System.out.println("============================================\n");
    }

    // ================= MÉTODOS AUXILIARES DE LECTURA =================
    static int leerEntero() {
        while (!sc.hasNextInt()) {
            System.out.print("Entrada no válida. Ingrese un número entero: ");
            sc.next();
        }
        int valor = sc.nextInt();
        return valor;
    }

    static double leerDouble() {
        while (!sc.hasNextDouble()) {
            System.out.print("Entrada no válida. Ingrese un número: ");
            sc.next();
        }
        double valor = sc.nextDouble();
        return valor;
    }
}

