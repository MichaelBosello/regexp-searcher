package eventloop;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import regex.RegexController;
import regex.regexresult.RegexResult;
import regex.regexresult.RegexSearchingResult;
import ui.RegexUI;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VertxController implements RegexController {

    private final RegexResult result = new RegexSearchingResult();
    private final Verticle verticle;
    private final Semaphore endEvent = new Semaphore(0);

    public VertxController(RegexUI ui) {
        verticle = new RegexVerticle(ui, result, endEvent);
    }

    public VertxController(RegexUI ui, String path, String regex, int depth) {
        verticle = new RegexVerticle(ui, result, endEvent, path, regex, depth);
    }

    @Override
    public RegexController start() {
        //Don't want to see Vertx warning when wait for user input?
        //Logger.getLogger("io.vertx.core.impl.BlockedThreadChecker").setLevel(Level.OFF);
        Vertx  vertx = Vertx.vertx();
        vertx.deployVerticle(verticle);
        try {
            endEvent.acquire();
        } catch (InterruptedException e) {
            System.out.println("[WARNING] Computation not ended");
        }
        vertx.close();
        return this;
    }

    @Override
    public RegexResult getResult() {
        return result;
    }
}
