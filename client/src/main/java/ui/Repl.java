package ui;


import server.Server;
import webSocketMessages.serverMessages.Notification;
import websocket.NotificationHandler;

import java.util.Objects;
import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final ChessClientFacade client;

    public Repl(String serverUrl) {
        client = new ChessClientFacade(serverUrl, this);
    }

    public void run() {
        System.out.println("Welcome to 240 chess. Type help to get started.");
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
                    //this is the middle loop for being logged in
                    printPrompt();
                    scanner.reset();
                    line = scanner.nextLine();
                    result = client.eval(line);
                    System.out.print(result);
                    if (Objects.equals(result, "quit")){
                        break;
                    }

                    while (!result.equals("quit") && !result.equals("leave") && client.state == State.INGAME) {
                        //this is the innermost loop for being in game
                        printPrompt();
                        scanner.reset();
                        line = scanner.nextLine();
                        if(Objects.equals(line, "resign")){
                            System.out.println("Are you sure you want to resign? Type YES to confirm");
                            scanner.reset();
                            line = scanner.nextLine();
                            if(Objects.equals(line, "YES")){
                                result = client.eval("resign");
                            }
                        }else{
                            result = client.eval(line);
                        }
                        System.out.print(result);
                    }
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
        System.exit(0);
    }

    public void notify(Notification notification) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + notification.message + EscapeSequences.SET_TEXT_COLOR_WHITE);
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + EscapeSequences.RESET + ">>> ");
    }
}
