import eventloop.VertxController;
import forkjoin.ForkJoinController;
import ui.RegexCommandLineUI;
import ui.RegexGUI;
import ui.RegexUI;

public class Main {

    public static void main(String args[]) {
        RegexUI ui = new RegexGUI();
        switch(ui.ask("Select computation method:\n(T) Task [Default]\n(E) Eventloop\n(R)Reactive Stream")){
            case "E":
                new VertxController(ui).start();
                break;
            case "R":
                new VertxController(ui).start();
                break;
            default:
                new ForkJoinController(ui).start();
        }
    }

}
