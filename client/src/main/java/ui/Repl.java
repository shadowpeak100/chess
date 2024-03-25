package ui;


import server.Server;

import java.util.Scanner;
import ui.EscapeSequences;

public class Repl {
    private final Server server;
    private final ChessClient client;

    public Repl(String serverUrl) {
        server = new Server();
        client = new ChessClient();
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to 240 chess. Type help to get started.");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        //this is the outmost loop for not being signed in
        while (!result.equals("quit")) {
            printPrompt();
            scanner.reset();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);

                while (!result.equals("quit") && client.state == State.SIGNEDIN){
                    printPrompt();
                        //this is the middle loop for being logged in
                        while (!result.equals("quit") && client.state == State.INGAME) {
                            printPrompt();
                            if (client.state == State.INGAME) {
                                //this is the innermost loop for being in game
                            }
                        }

                }



            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

//    public void notify(Notification notification) {
//        System.out.println(RED + notification.message());
//        printPrompt();
//    }

    private void printPrompt() {
        System.out.print("\n" + EscapeSequences.RESET + ">>> ");
    }

//    @Override
//    public HandlerResult handleNotification(Notification notification, Object attachment) {
//        return null;
//    }
}
