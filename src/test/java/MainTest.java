import eventloop.VertxController;
import forkjoin.ForkJoinController;
import reactivestreams.RxController;
import regex.RegexController;
import regex.regexresult.Result;
import org.junit.Test;
import ui.RegexCommandLineUI;

import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class MainTest {

    /*
    *
    * Tested on:
    * https://regexr.com/
    *
    * */

    private final  String PATH = getClass().getClassLoader().getResource("depth0").getFile();
    private final static String REGEX_WORDS_FIRST_CHAR_UPPERCASE = "([A-Z])\\w+";
    private final static String REGEX_NUMBERS = "([0-9])+";
    private final static String REGEX_HTML = "<\\w+>(.*?)<\\/\\w+>";
    private final static int TOTAL_FILE = 3;
    private final static int WORDS_RESULT = 45;
    private final static int NUMBER_RESULT = 75;
    private final static int HTML_RESULT = 3;
    private final static String[] REGEX = {REGEX_WORDS_FIRST_CHAR_UPPERCASE, REGEX_NUMBERS, REGEX_HTML};
    private final static int[] RESULT = {WORDS_RESULT,NUMBER_RESULT,HTML_RESULT};

    @Test
    public void forkJoinTest() {
        regexTest( (regex, depth) -> new ForkJoinController(new RegexCommandLineUI(), PATH, regex, depth));
    }

    @Test
    public void vertxTest() {
        regexTest( (regex, depth) -> new VertxController(new RegexCommandLineUI(), PATH, regex, depth));
    }

    @Test
    public void rxTest() {
        regexTest( (regex, depth) -> new RxController(new RegexCommandLineUI(), PATH, regex, depth));
    }

    private void regexTest(BiFunction<String, Integer, RegexController> controllerFabric){
        for(int regexIndex = 0; regexIndex < REGEX.length; regexIndex++)
            for (int depth = 0; depth < 3; depth++) {
                Result result =
                        controllerFabric.apply(REGEX[regexIndex], depth)
                                .start()
                                .getResult();
                assertEquals("LIST SIZE, REGEX: " + REGEX[regexIndex] + " depth " + depth,
                        (depth + 1), result.getMatchingFiles().size());
                assertEquals("PERCENT, REGEX: " + REGEX[regexIndex] + " depth " + depth,
                        (double) 1 / TOTAL_FILE , result.matchingFilePercent(), 0.001);
                assertEquals("MEAN, REGEX: " + REGEX[regexIndex] + " depth " + depth,
                        (double) RESULT[regexIndex], result.matchMean().getKey(), 0.001);
            }
    }
}
