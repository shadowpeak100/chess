package webSocketMessages.serverMessages;

public class Error extends ServerMessage{
    public Error(ServerMessageType type, String errorMessage) {
        super(type);
        //must contain "Error"
        if(!errorMessage.toLowerCase().contains("error")){
            errorMessage = "Error" + errorMessage;
        }
        this.errorMessage = errorMessage;
    }

    private String errorMessage;
}
