import eventloop.VertxController;
import forkjoin.ForkJoinController;
import ui.RegexCommandLineUI;
import ui.RegexUI;

public class main {

    public static void main(String args[]) {
        RegexUI ui = new RegexCommandLineUI();
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
