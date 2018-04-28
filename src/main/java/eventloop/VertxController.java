package eventloop;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import regex.RegexController;
import regex.regexresult.RegexResult;
import regex.regexresult.RegexSearchingResult;
import ui.RegexUI;

public class VertxController implements RegexController {

    private RegexResult result = new RegexSearchingResult();
    private Verticle verticle;

    public VertxController(RegexUI ui) {
        verticle = new RegexVerticle(ui, result);
    }

    public VertxController(RegexUI ui, String path, String regex, int depth) {
        verticle = new RegexVerticle(ui, result, path, regex, depth);
    }

    @Override
    public RegexController start() {
        Vertx  vertx = Vertx.vertx();
        vertx.deployVerticle(verticle);
        return this;
    }

    @Override
    public RegexResult getResult() {
        return result;
    }
}
