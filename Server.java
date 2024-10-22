import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;


/*
	Paolo Carino, Nick Stancari, Ethan Ignacio CS 342 Project 4
	A Battleship application between clients and the server, where clients can
	play against an AI, or against individual clients they match up with.
 */

public class Server{

    int count = 1;

    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
    ArrayList<String> Lobby = new ArrayList<String>();
    HashMap<String, String> boardStrings = new HashMap<>(); // <Username, BoardString>

    ArrayList<ArrayList<String>> ActiveGameSessions = new ArrayList<ArrayList<String>>();
    TheServer server;
    private Consumer<Message> callback;
    HashMap<String, ClientThread> clientsHashMap = new HashMap<>();



    Server(Consumer<Message> call){

        callback = call;
        server = new TheServer();
        server.start();
    }


    public class TheServer extends Thread{

        public void run() {

            try(ServerSocket mysocket = new ServerSocket(5555);){
                System.out.println("Server is waiting for a client!\n");


                while(true) {

                    Socket clientSocket = mysocket.accept();
                    System.out.println("Client has connected\n"); // outputs if the client connects
                    ClientThread c = new ClientThread(clientSocket, count); // creates new clientThread
                    clients.add(c); // adds them to the list
                    clientsHashMap.put(c.username, c); // adds them to the hashmap
                    c.start();
                    count++; // counts the user it is
                }
            }//end of try
            catch(IOException e) {
                Message messageException = new Message(Message.MessageType.ERROR, "Server", "Server socket did not launch");
                callback.accept(messageException);
            }
        }//end of while
    }


    class ClientThread extends Thread{


        Socket connection;
        int count;
        private String username;
        ObjectInputStream in;
        ObjectOutputStream out;
        private boolean gameOver = false;

        ClientThread(Socket s, int count){
            this.connection = s;
            this.count = count;
            username = "user" + count + "\n";
        }

        public void setUsername(String username) {
            this.username = username;
        }
        public String getUsername() {
            return username;
        }

        public void updateClients(Message message) {
            for(int i = 0; i < clients.size(); i++) {
                ClientThread t = clients.get(i);
                try {
                    t.out.writeObject(message);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void run(){

            try {

                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                connection.setTcpNoDelay(true);
            }
            catch(Exception e) {
                System.out.println("Streams not open");
            }


            while(true) {
                try {
                    Message data = (Message) in.readObject(); // reads in Message Sent




                    switch (data.getType()) {

                        case PLAYER_LOOKING_FOR_GAME:
                            System.out.print("Player looking for game message received\n");
                            System.out.print(data.getUsername());
                            // if there is another player looking for a game (Lobby size is >= 2), add them to ArrayList "ActiveGameSessions"
                            // send message back to both Clients (type: GAME_FOUND)
                            ArrayList<String> activeGameSession = null;
                            if (!Lobby.isEmpty()) {
                                //add the first player in lobby to ActiveGameSession
                                String player1 = Lobby.get(0); // Get player from front of list
                                activeGameSession = new ArrayList<>();
                                activeGameSession.add(player1);
                                activeGameSession.add(data.getUsername());

                                // Add the activeGameSession to the ActiveGameSessions list
                                ActiveGameSessions.add(activeGameSession);
                                System.out.print("Updated Game Sessions: " + ActiveGameSessions + "\n");

                                Message gameFoundMessagePlayer1 = new Message();
                                gameFoundMessagePlayer1.setMessageType(Message.MessageType.GAME_FOUND);
                                clientsHashMap.get(player1).send(gameFoundMessagePlayer1);
                                System.out.println("Sent to: " + player1);

                                Message gameFoundMessagePlayer2 = new Message();
                                gameFoundMessagePlayer2.setMessageType(Message.MessageType.GAME_FOUND);
                                clientsHashMap.get(data.getUsername()).send(gameFoundMessagePlayer2);
                                System.out.print("Sent to: " + data.getUsername());

                                Lobby.remove(0);
                            } else {
                                Lobby.add(data.getUsername());
                                System.out.print("Updated Lobby: " + Lobby + "\n");
                            }
                            break;

                        case REGULAR_MOVE:
                            // Receive move from client. This message should have the client's username.
                            // Use this client's username to find the username of complement client in ActiveGameSessions
                            // Send message with move to complement client
                            System.out.println("Move (" + data.getX() + ", " + data.getY() + ") Received from " + data.getUsername() + "\n");
                            String currentUser = data.getUsername();

                            ArrayList<String> currentGameSession = null;
                            for (ArrayList<String> gameSession : ActiveGameSessions) {
                                if (gameSession.contains(currentUser)) {
                                    currentGameSession = gameSession;
                                    break;
                                }
                            }

                            if (currentGameSession != null) {
                                // Get the complement user's username
                                String complementUser = currentGameSession.get(0).equals(currentUser) ? currentGameSession.get(1) : currentGameSession.get(0);
                                Message moveMessage = new Message();
                                moveMessage.setMessageType(Message.MessageType.REGULAR_MOVE);
                                moveMessage.setUsername(currentUser); // Set the username of the player making the move
                                moveMessage.setX(data.getX());
                                moveMessage.setY(data.getY());
                                clientsHashMap.get(complementUser).send(moveMessage); // Send the message to the complement user
                                System.out.println("Move (" + data.getX() + ", " + data.getY() + ") sent to + complementUser\n");
                            } else {
                                System.out.println("Error: No active game session found for user " + currentUser + "\n");
                            }
                            break;

                        case REQUEST_USERNAME:
                            System.out.print("Username requested: " + username + "\n");
                            Message sendUsername = new Message();
                            sendUsername.setUsername(username);
                            sendUsername.setMessageType(Message.MessageType.USER_ID_CREATE);
                            clientsHashMap.get(username).send(sendUsername);
                            System.out.print("Username sent: " + username + "\n");
                            break;

                        case START_BOARD:
                            System.out.print("Received starting Board from " + data.getUsername() + "\n");
                            currentUser = data.getUsername();

                            boardStrings.put(currentUser, data.getContent());
                            Thread.sleep(10000);
                            currentGameSession = null;
                            for (ArrayList<String> gameSession : ActiveGameSessions) {
                                if (gameSession.contains(currentUser)) {
                                    currentGameSession = gameSession;
                                    break;
                                }
                            }
                            String complementUser = currentGameSession.get(0).equals(currentUser) ? currentGameSession.get(1) : currentGameSession.get(0);
                            Message startBoardMessage = new Message();
                            startBoardMessage.setMessageType(Message.MessageType.START_BOARD);
                            startBoardMessage.setUsername(currentUser);
                            startBoardMessage.setContent(data.getContent());
                            clientsHashMap.get(complementUser).send(startBoardMessage);
                            System.out.print("Sent starting Board to " + clientsHashMap.get(complementUser) + "\n");
                            break;

                        case WINNER_WINNER_CHICKEN_DINNER:
                            currentUser = data.getUsername();
                            gameOver = true;
                            currentGameSession = null;
                            System.out.println(gameOver);
                            System.out.println(currentUser);
                            for (ArrayList<String> gameSession : ActiveGameSessions) {
                                if (gameSession.contains(currentUser)) {
                                    currentGameSession = gameSession;
                                }
                            }
                            complementUser = currentGameSession.get(0).equals(currentUser) ? currentGameSession.get(1) : currentGameSession.get(0);
                            Message winMessage = new Message();
                            winMessage.setMessageType(Message.MessageType.WINNER_WINNER_CHICKEN_DINNER);
                            winMessage.setUsername(complementUser);
                            clientsHashMap.get(complementUser).send(winMessage);
                            System.out.println("Win message sent to " + complementUser);
                    }
                } catch(Exception e) {

                    clients.remove(this); // remove them from the list of clients

                    Message messageError = new Message(Message.MessageType.LEAVE, "Server", "OOOOPPs...Something wrong with the socket from " + username + "....closing down!");
                    callback.accept(messageError); // output an error message to the server

                    //Notifies everyone who left
                    Message messageLeave = new Message(Message.MessageType.LEAVE, "Server", username + " has left");
                    updateClients(messageLeave);

                    // removes them from the hashmap of clients
                    clientsHashMap.remove(username);

                    // updates the connected clients list
                    sendUserListToClients();
                    closeConnection(); // closes the connection of the lost client
                    break;
                }
            }
        }//end of run

        public void send(Message data) { // sends message to client

            try {
                out.writeObject(data);
                out.flush();
            } catch (IOException e) {

                if (e instanceof SocketException && "Connection reset by peer".equals(e.getMessage())) {
                    closeConnection();
                }
                else {
                    e.printStackTrace();
                }
            }
        }

        private void sendUserListToClients() {
            List<String> usernames = new ArrayList<>(); // creates a list of usernames who are connected

            // adds each clientThreads username
            for (ClientThread clientThread : clientsHashMap.values()) {
                usernames.add(clientThread.getUsername());
            }

            // sends the list of usernames to everyone
            Message usernamesListSend = new Message(Message.MessageType.LIST_OF_NAMES, usernames);
            for (ClientThread clientThread : clientsHashMap.values()) {
                clientThread.send(usernamesListSend);
            }
        }

        public void closeConnection() { // securely closes the connection
            try {
                if (out != null) {
                    out.close();
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }//end of client thread
}