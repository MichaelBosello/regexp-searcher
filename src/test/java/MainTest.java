import forkjoin.forkJoinController;
import regex.regexresult.RegexResult;
import org.junit.Test;
import ui.RegexCommandLineUI;
import ui.RegexUI;

import static org.junit.Assert.assertEquals;

public class MainTest {

    /*
    *
    * Tested on:
    * https://regexr.com/
    *
    * */

    private final  String PATH = getClass().getClassLoader().getResource("depth0").getFile();
    private final static String REGEXP_WORDS_FIRST_CHAR_UPPERCASE = "([A-Z])\\w+";
    private final static String REGEXP_NUMBERS = "([0-9])+";
    private final static String REGEXP_HTML = "<\\w+>(.*?)<\\/\\w+>";
    private final static int TOTAL_FILE = 3;
    private final static int WORDS_RESULT = 45;
    private final static int NUMBER_RESULT = 75;
    private final static int HTML_RESULT = 3;
    private final static String[] REGEXP = {REGEXP_WORDS_FIRST_CHAR_UPPERCASE,REGEXP_NUMBERS,REGEXP_HTML};
    private final static int[] RESULT = {WORDS_RESULT,NUMBER_RESULT,HTML_RESULT};

    @Test
    public void forkJoinTest() {

        for(int regexpIndex = 0; regexpIndex < REGEXP.length; regexpIndex++)
            for (int depth = 0; depth < 3; depth++) {
                RegexResult result =
                        new forkJoinController(PATH, REGEXP[regexpIndex], depth, new RegexCommandLineUI())
                        .start()
                        .getResult();
                assertEquals("LIST SIZE, REGEXP: " + REGEXP[regexpIndex] + " depth " + depth,
                        (depth + 1), result.getMatchingFiles().size());
                assertEquals("PERCENT, REGEXP: " + REGEXP[regexpIndex] + " depth " + depth,
                        (double) 1 / TOTAL_FILE , result.matchingFilePercent(), 0.001);
                assertEquals("MEAN, REGEXP: " + REGEXP[regexpIndex] + " depth " + depth,
                        (double) RESULT[regexpIndex], result.matchMean(), 0.001);
            }
    }
}
