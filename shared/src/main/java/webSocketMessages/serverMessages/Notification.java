package webSocketMessages.serverMessages;

public class Notification extends ServerMessage{
    public Notification(ServerMessageType type, String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMessage(){
        return message;
    }

    public String message;
}
