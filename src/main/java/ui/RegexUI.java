package ui;

import java.util.List;

public interface RegexUI {

    String ask(String message);

    String askPath();

    String askRegex();

    int askDepth();

    void updateResult(List<String> files, double percent, double mean, int error);

    void end();

    void start();
}
