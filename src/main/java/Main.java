import eventloop.VertxStarter;
import forkjoin.ForkJoinController;
import reactivestreams.RxController;
import ui.RegexGUI;
import ui.RegexUI;

public class Main {

    public static void main(String args[]) {
        RegexUI ui = new RegexGUI();
        switch(ui.askMethod()){
            case "E":
                new VertxStarter(ui).start();
                break;
            case "R":
                new RxController(ui).start();
                break;
            default:
                new ForkJoinController(ui).start();
        }
    }

}
