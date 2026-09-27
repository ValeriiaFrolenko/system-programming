package vfrolenko;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        String source;

        if (args.length > 0) {
            // run with file argument: java Main program.txt
            source = Files.readString(Path.of(args[0]));
            System.out.println("=== FILE: " + args[0] + " ===");
        } else {
            // fallback: read from stdin until EOF (Ctrl+D / Ctrl+Z)
            System.out.println("Введіть програму (Ctrl+D для завершення):");
            Scanner scanner = new Scanner(System.in);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append('\n');
            }
            source = sb.toString();
        }

        if (source.isBlank()) {
            System.out.println("Порожній ввід.");
            return;
        }

        System.out.println("\n=== TOKENS ===");

        Lexer lexer = new Lexer(source);
        Token token;
        int total = 0, errors = 0;

        do {
            token = lexer.nextToken();
            total++;
            System.out.println(token);
            if (token.type()== TokenType.ERROR) errors++;
        } while (token.type() != TokenType.EOF);

        System.out.println("\n=== SUMMARY ===");
        System.out.printf("Total tokens : %d%n", total - 1);
        System.out.printf("Error tokens : %d%n", errors);
    }
}