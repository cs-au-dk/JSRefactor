package dk.brics.jsrefactoring;

import java.util.Scanner;

public class CommandLineUtil {
    private static Scanner scanner = new Scanner(System.in);

    public static int promptInt(String question) {
        System.out.println(question);
        System.out.print("> ");
        return scanner.nextInt();
    }

    public static String promptString(String question) {
        System.out.println(question);
        System.out.print("> ");
        return scanner.next();
    }
}
