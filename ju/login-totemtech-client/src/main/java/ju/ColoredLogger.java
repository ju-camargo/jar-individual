package ju;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class ColoredLogger {

    public static final String RESET = Ansi.ansi().reset().toString();

    public static final String COLOR_RAM = Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString();
    public static final String COLOR_DISCO = Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString();
    public static final String COLOR_CPU = Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString();
    public static final String COLOR_PROCESSOS = Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString();
    public static final String COLOR_REDE = Ansi.ansi().fg(Ansi.Color.RED).bold().toString();

    public static void log(String message, String color) {
        AnsiConsole.systemInstall();
        System.out.println(color + message + RESET);
        AnsiConsole.systemUninstall();
    }
}