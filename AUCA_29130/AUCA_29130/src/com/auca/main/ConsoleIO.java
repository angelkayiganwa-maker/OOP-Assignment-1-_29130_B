package com.auca.main;

import java.util.Scanner;


public final class ConsoleIO {

    private ConsoleIO() {
    }

    static Scanner sc = new Scanner(System.in);

    public static String readString(String message) {
        System.out.print(message);

        if (!sc.hasNextLine()) {
            System.out.println();
            System.out.println("Input ended. Closing the application.");
            System.exit(0);
        }

        return sc.nextLine().trim();
    }

    public static int readInt(String message) {
        while (true) {
            String input = readString(message);

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("! Please enter a valid whole number.");
            }
        }
    }

    public static int readNumber(String message) {
        String input = readString(message);

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
