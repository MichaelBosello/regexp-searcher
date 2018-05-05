package ui;

import java.util.List;
import java.util.Map;

public interface RegexUI {

    String ask(String message);

    String askMethod();

    String askPath();

    String askRegex();

    int askDepth();

    void updateResult(List<String> files, double percent, Map.Entry<Long, Long> mean, int error) throws InterruptedException;

    void start();

    void end();
}
