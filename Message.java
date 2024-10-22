import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    public enum MessageType {
        REGULAR_MOVE,
        ERROR,
        LEAVE,
        GAME_FOUND,
        LIST_OF_NAMES,
        PLAYER_LOOKING_FOR_GAME,
        USER_ID_CREATE,
        REQUEST_USERNAME,
        START_BOARD,
        WINNER_WINNER_CHICKEN_DINNER
    }

    private MessageType type;
    private String sender;
    private String content;
    private String username;
    private int x;
    private int y;

    public Message() {}

    //     Constructor for Username creating
    public Message(MessageType type, String content) {
        sender = content;
        this.type = type;
        this.content = content;
    }

    //     Constructor for joining or leaving
    public Message(MessageType type, String sender, String content) {
        this.sender = sender;
        this.type = type;
        this.content = content;
    }

    //      Constructor for a group message / to all
    public Message(MessageType type, String sender, List<String> receivers, String content) {
        this.sender = sender;
        this.type = type;
        this.content = content;

    }

    //      Constructor for Transferring all clients on server
    public Message(MessageType type, List<String> clientsOnServer) {
        this.type = type;
        content = null;
        sender = "Server";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public void setMessageType(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

}