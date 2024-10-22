import java.util.HashMap;
import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.shape.Rectangle;

public class GuiClient extends Application {


	private HashMap<String, Scene> sceneMap;
	private Stage primaryStage;
	private Board enemyBoard, playerBoard, onlinePlayerBoard, opponentBoard;
	private Scene previousScene;
	private Board board;
	private String boardString = "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE";
	public boolean isGameFound = false;
	public boolean gameOver = false;


	Client clientConnection;
	ListView<String> chatListView;

	//-----------------------------------------------------------
	// Unused
	private boolean running = false;
	private int shipsToPlace = 5;
	private boolean enemyTurn = false;
	private Random random = new Random();
	private String username;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		sceneMap = new HashMap<String, Scene>(); // All the scenes

		Scene startScene = createStartScene();
		Scene rulesScene = createRulesScene();
		Scene playerScene = createPlayerScene();
		Scene winScene = createWinScreen();
		Scene loseScene = createLoseScreen();
		Scene OnlinePlayerScene = createOnlinePlayerScene();

		sceneMap.put("startScreen", startScene);
		sceneMap.put("rulesScreen", rulesScene);
		sceneMap.put("player", playerScene);
		sceneMap.put("winScreen", winScene);
		sceneMap.put("loseScreen", loseScene);
		sceneMap.put("onlinePlayer", OnlinePlayerScene);


		primaryStage.setScene(startScene);
		primaryStage.setMaximized(true);
		primaryStage.setTitle("Battleship");
		primaryStage.show();

		clientConnection = new Client(data-> {
//			chatListView.getItems().add(data.toString());
			HandleResponse(data);
		});

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});

		clientConnection.start();
	}


	public void handleAIButton() throws Exception {
		primaryStage.setScene(sceneMap.get("player"));
		primaryStage.setMaximized(true);
	}

	public void handleRulesButton() throws Exception {
		primaryStage.setScene(sceneMap.get("rulesScreen"));
		primaryStage.setMaximized(true);
	}

	public void handleBackButton(Scene previousScene) throws Exception {
		primaryStage.setScene(previousScene);
		primaryStage.setMaximized(true);
	}

	//function to reset the whole board when leave game is pressed.
	public void handleLeaveGameButton() throws Exception {
		sceneMap.remove("player");
		Scene newScene = createPlayerScene();
		sceneMap.put("player", newScene);
		running = false;
		shipsToPlace = 5;
		enemyTurn = false;
		primaryStage.setScene(sceneMap.get("startScreen"));
		primaryStage.setMaximized(true);
	}


	private Scene createStartScene() {
		//defining all needed fx
		BorderPane root = new BorderPane();
		Screen screen = Screen.getPrimary();
		double screenWidth = screen.getBounds().getWidth();
		double screenHeight = screen.getBounds().getHeight();

		root.setPrefSize(screenWidth, screenHeight);
		root.getStyleClass().add("battleship-start");

		Button rulesButton =  new Button("Rules");
		rulesButton.getStyleClass().add("rules-button");
		rulesButton.setOnAction(event -> {
			try {
				previousScene = primaryStage.getScene();
				handleRulesButton();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		Button playAgainstAIButton = new Button("Play Against AI");
		playAgainstAIButton.getStyleClass().add("play-button");
		playAgainstAIButton.setOnAction(event -> {
			try {
				handleAIButton();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		HBox topContainer = new HBox(rulesButton);
		topContainer.setAlignment(Pos.CENTER_RIGHT);
		root.setTop(topContainer);

		Label titleLabel = new Label("BATTLESHIP");
		titleLabel.getStyleClass().add("title-label");

		Button playAgainstPlayerButton = new Button("Play Against Player");
		playAgainstPlayerButton.getStyleClass().add("play-button");
		playAgainstPlayerButton.setOnAction(event -> {
			playAgainstPlayerButton.setText("Waiting for player...");

			playAgainstPlayerButton.setDisable(true);
			playAgainstAIButton.setDisable(true);
			rulesButton.setDisable(true);

			try {
				if (clientConnection != null) {
					// Create a new message with the client's username and the message type
					Message message = new Message();
					message.setMessageType(Message.MessageType.PLAYER_LOOKING_FOR_GAME);
					message.setUsername(clientConnection.getUsername());
					clientConnection.send(message);
				} else {
					System.err.println("Client connection is null.");
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		HBox playButtons = new HBox(20, playAgainstPlayerButton, playAgainstAIButton);
		playButtons.setAlignment(Pos.CENTER);

		VBox centerContainer = new VBox(20, titleLabel, playButtons);
		centerContainer.setAlignment(Pos.CENTER);
		root.setCenter(centerContainer);

		Scene startScene = new Scene(root);
		startScene.getStylesheets().add("/styles/styles1.css");

		return startScene;
	}

	private Scene createRulesScene() {
		BorderPane root = new BorderPane();
		Screen screen = Screen.getPrimary();
		double screenWidth = screen.getBounds().getWidth();
		double screenHeight = screen.getBounds().getHeight();

		root.setPrefSize(screenWidth, screenHeight);

		root.getStyleClass().add("battleship-start");

		HBox topBox = new HBox();
		topBox.getStyleClass().add("button-container");
		Button backButton = new Button("Back");
		backButton.getStyleClass().add("back-button");

		backButton.setOnAction(event -> {
			try {
				handleBackButton(previousScene);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		topBox.getChildren().add(backButton);

		root.setTop(topBox);

		StackPane centerStackPane = new StackPane();
		centerStackPane.setAlignment(Pos.TOP_CENTER);

		Rectangle rectangle = new Rectangle(600, 700);
		rectangle.getStyleClass().add("rules-rectangle");

		Label titleLabel = new Label("HOW TO PLAY");
		titleLabel.getStyleClass().add("title-label");
		Label ruleLabel = new Label("\nEach player deploys his ships (of lengths varying\nfrom 2 to 5 squares) secretly on a square grid.\n\n" +
				"Then each player alternates shooting at the\nother's grid by clicking a location.\n\n" +
				"Each move is classified as a Hit! or Miss!\nYou try to deduce where the enemy ships are and sink \nthem. " +
				"\n\nFirst to do so wins.\n\n" +
				"There are 5 ships, all spanning 5-2 spaces.\n\n" +
				"Press Play Against Player to find a player.\n\n" +
				"Press Play Against AI to play against a computer.");

		ruleLabel.getStyleClass().add("title-rules");

		VBox rulesList = new VBox(titleLabel, ruleLabel);
		rulesList.setAlignment(Pos.TOP_CENTER);

		centerStackPane.getChildren().addAll(rectangle, rulesList);

		root.setCenter(centerStackPane);
		Scene rulesScene = new Scene(root);
		rulesScene.getStylesheets().add("/styles/styles3.css");


		return rulesScene;
	}

	private Scene createPlayerScene() {

		BorderPane root = new BorderPane();

		root.setPrefSize(600, 800);
		root.getStyleClass().add("battleship-start");

		Button rulesButton =  new Button("Rules");
		Button leaveGameButton = new Button("Leave Game");
		Button multiAttackButton = new Button("Multi Attack");

		rulesButton.getStyleClass().add("rules-button");
		leaveGameButton.getStyleClass().add("rules-button");
		multiAttackButton.getStyleClass().add("rules-button");

		rulesButton.setOnAction(event -> {
			previousScene = primaryStage.getScene();

			try {
				handleRulesButton();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		leaveGameButton.setOnAction(event -> {
			try {
				handleLeaveGameButton();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		multiAttackButton.setDisable(true);

		multiAttackButton.setOnAction(event -> {
			// Call the multi-attack method
			enemyBoard.enableMultiAttackMode();
			multiAttackButton.setDisable(true);

		});





		enemyBoard = new Board(true, event -> {
			if (!running)
				return;


			Board.Cell cell = (Board.Cell) event.getSource();
			if (cell.wasShot)
				return;

			enemyTurn = !cell.shoot();

			if (enemyBoard.ships == 0) {
				System.out.println("YOU WIN");
				primaryStage.setScene(sceneMap.get("winScreen"));
				primaryStage.setMaximized(true);
			}

			if (enemyTurn)
				enemyMove();
		});

		playerBoard = new Board(false, event -> {

			if (running)
				return;

			Board.Cell cell = (Board.Cell) event.getSource();
			if (playerBoard.placeShip(new Ship(shipsToPlace, event.getButton() == MouseButton.PRIMARY), cell.x, cell.y)) {
				if (--shipsToPlace == 0) {
					multiAttackButton.setDisable(false);
					startGame();
				}
			}
		});
		placeEnemyShips();

		HBox topContainer = new HBox(1000, leaveGameButton, rulesButton);
		topContainer.setAlignment(Pos.CENTER);
		root.setTop(topContainer);

		enemyBoard.setStyle("-fx-background-color: transparent;");
		playerBoard.setStyle("-fx-background-color: transparent;");

		VBox vbox = new VBox(35, enemyBoard, playerBoard, multiAttackButton);
		vbox.setAlignment(Pos.TOP_CENTER);

		root.setCenter(vbox);


		Screen screen = Screen.getPrimary();
		double screenWidth = screen.getBounds().getWidth();
		double screenHeight = screen.getBounds().getHeight();

		root.setPrefSize(screenWidth, screenHeight);
		Scene playerScreen = new Scene(root);
		playerScreen.getStylesheets().add("/styles/styles2.css");

		return playerScreen;
	}
	private void placeEnemyShips() {
		int type = 5;

		while (type > 0) {
			int x = random.nextInt(10);
			int y = random.nextInt(10);

			if (enemyBoard.placeShip(new Ship(type, Math.random() < 0.5), x, y)) {
				type--;
			}
		}
	}

	private Scene createWinScreen(){
		BorderPane root = new BorderPane();
		Screen screen = Screen.getPrimary();
		double screenWidth = screen.getBounds().getWidth();
		double screenHeight = screen.getBounds().getHeight();

		root.setPrefSize(screenWidth, screenHeight);

		root.getStyleClass().add("battleship-start");

		root.setPrefSize(600, 800);
		Button returnToLobbyButton = new Button("Return to Lobby");
		returnToLobbyButton.getStyleClass().add("back-button");


		returnToLobbyButton.setOnAction(event -> {
			try {
				handleLeaveGameButton();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		Label winLabel = new Label("YOU WIN");
		winLabel.getStyleClass().add("title-label2");

		VBox vbox = new VBox(winLabel, returnToLobbyButton);
		vbox.setAlignment(Pos.CENTER);


		root.setCenter(vbox);
		Scene winScene = new Scene(root);
		winScene.getStylesheets().add("/styles/styles3.css");
		return winScene;

	}
	private Scene createLoseScreen(){
		BorderPane root = new BorderPane();
		Screen screen = Screen.getPrimary();
		double screenWidth = screen.getBounds().getWidth();
		double screenHeight = screen.getBounds().getHeight();

		root.setPrefSize(screenWidth, screenHeight);

		root.getStyleClass().add("battleship-start");

		root.setPrefSize(600, 800);
		Button returnToLobbyButton = new Button("Return to Lobby");
		returnToLobbyButton.getStyleClass().add("back-button");

		returnToLobbyButton.setOnAction(event -> {
			try {
				handleLeaveGameButton();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		Label loseLabel = new Label("YOU LOSE");
		loseLabel.getStyleClass().add("title-label2");

		VBox vbox = new VBox(loseLabel, returnToLobbyButton);
		vbox.setAlignment(Pos.CENTER);


		root.setCenter(vbox);
		Scene loseScene = new Scene(root);
		loseScene.getStylesheets().add("/styles/styles3.css");
		return loseScene;

	}

	private Scene createOnlinePlayerScene() {
		BorderPane root = new BorderPane();

		root.setPrefSize(600, 800);
		root.getStyleClass().add("battleship-start");

		Button rulesButton = new Button("Rules");
		Button leaveGameButton = new Button("Leave Game");

		rulesButton.getStyleClass().add("rules-button");
		leaveGameButton.getStyleClass().add("rules-button");

		rulesButton.setOnAction(event -> {
			previousScene = primaryStage.getScene();
			try {
				handleRulesButton();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		leaveGameButton.setOnAction(e -> {
			primaryStage.setScene(sceneMap.get("startScreen"));
		});

		onlinePlayerBoard = new Board(false, event -> {
			int x, y;
			if (running)
				return;
			Board.Cell cell = (Board.Cell) event.getSource();
			if (onlinePlayerBoard.placeShip(new Ship(shipsToPlace, event.getButton() == MouseButton.PRIMARY), cell.x, cell.y)) {
				if (--shipsToPlace == 0) {
					// Send message to server that says player placed all ships
					Message boardMessage = createBoardMessage(onlinePlayerBoard);
					boardMessage.setUsername(clientConnection.getUsername());
					// Send the message to the server
					System.out.println("Board Sent to Server from " + clientConnection.getUsername());
					clientConnection.send(boardMessage);
				}
			}
		});

		opponentBoard = new OpponentBoard(boardString, event -> {
			int x, y;
			if (!running)
				return;
			// Write the for loop to check ships here
			boolean shipAlive = false;
			for (x = 0; x < 10; x++) {
				for (y = 0; y < 10; y++) {
					Board.Cell cell = onlinePlayerBoard.getCell(x, y);
					if (cell.ship != null && cell.ship.isAlive()) {
						System.out.println("Ship is alive.");
						shipAlive = true;
						break; // Exit the inner loop
					}
				}
				if (shipAlive) {
					break; // Exit the outer loop
				}
			}
			if (!shipAlive) {
				System.out.println("YOU LOSE");
				primaryStage.setScene(sceneMap.get("loseScreen"));
				Message winMessage = new Message();
				winMessage.setMessageType(Message.MessageType.WINNER_WINNER_CHICKEN_DINNER);
				winMessage.setUsername(clientConnection.getUsername());
				System.out.println("Win message sent to server");
				clientConnection.send(winMessage);
				return;

			}

			Board.Cell cell = (Board.Cell) event.getSource();
			x = cell.x;
			y = cell.y;


			// Handle the logic for shooting the opponent's cell
			boolean hit = cell.shoot();
			System.out.println(hit);
			if (hit) {
				// For example, check if the opponent has lost all ships and end the game if necessary
				if (opponentBoard.ships == 0) {
					System.out.println("YOU WIN");
					primaryStage.setScene(sceneMap.get("winScreen"));
					primaryStage.setMaximized(true);
				}
			}
			if (shipAlive) {
				Message moveMessage = new Message();
				moveMessage.setMessageType(Message.MessageType.REGULAR_MOVE);
				moveMessage.setUsername(clientConnection.getUsername());
				moveMessage.setX(x);
				moveMessage.setY(y);
				clientConnection.send(moveMessage);
				System.out.print("Move message sent from " + clientConnection.getUsername() + "\n");
			}

		});


		HBox topContainer = new HBox(rulesButton);
		topContainer.setAlignment(Pos.CENTER_RIGHT);
		root.setTop(topContainer);

		opponentBoard.setStyle("-fx-background-color: transparent;");
		onlinePlayerBoard.setStyle("-fx-background-color: transparent;");

		VBox vbox = new VBox(35, opponentBoard, onlinePlayerBoard);
		vbox.setAlignment(Pos.TOP_CENTER);

		root.setCenter(vbox);

		Screen screen = Screen.getPrimary();
		double screenWidth = screen.getBounds().getWidth();
		double screenHeight = screen.getBounds().getHeight();

		root.setPrefSize(screenWidth, screenHeight);
		Scene onlinePlayerScreen = new Scene(root);
		onlinePlayerScreen.getStylesheets().add("/styles/styles2.css");

		return onlinePlayerScreen;
	}

	private void enemyMove() {
		while (enemyTurn) {
			//AI uses random number generator to decide moves.
			int x = random.nextInt(10);
			int y = random.nextInt(10);
			Board.Cell cell = playerBoard.getCell(x, y);
			if (cell.wasShot)
				continue;

			enemyTurn = cell.shoot();

			if (playerBoard.ships == 0) {
				System.out.println("YOU LOSE");
				primaryStage.setScene(sceneMap.get("loseScreen"));
				primaryStage.setMaximized(true);
			}
		}
	}

	private void startGame() {
		running = true;
	}

	public void HandleResponse(Message data) {
		Message message = data; // reads in message

		// If they're sending a username back to the Client, else then upload to ClientGUI
		if (message.getType() == Message.MessageType.USER_ID_CREATE) {
			//sends when the username is created
			clientConnection.setUsername(message.getUsername());
			System.out.println("Username received from server: " + clientConnection.getUsername());
		} else if (message.getType() == Message.MessageType.GAME_FOUND) {
			//prints when 2 players are matched up
			System.out.println("Game Found!");
			Platform.runLater(() -> primaryStage.setScene(sceneMap.get("onlinePlayer")));
		} else if (message.getType() == Message.MessageType.START_BOARD){
			// create opponent board using the message content
			boardString = message.getContent(); // Get the board string from the message content
			OpponentBoard opponentBoard = createOpponentBoard(boardString); // Create the opponent's board using the board string
			System.out.println(boardString);

			// Start the game or update the GUI with the opponent's board
			startOnlineGame();
		} else if (message.getType() == Message.MessageType.REGULAR_MOVE) {

			// Update the client's board with the opponent's move
			boolean hit = onlinePlayerBoard.shootCell(message.getX(), message.getY());
			System.out.println("Sent move from " + clientConnection.getUsername());
		} else if (message.getType() == Message.MessageType.WINNER_WINNER_CHICKEN_DINNER) {
			gameOver = true;
			System.out.println("win message received");
			System.out.println("YOU WIN");
			primaryStage.setScene(sceneMap.get("winScreen"));
			primaryStage.setMaximized(true);
		}
	}


	private void startOnlineGame() {
		running = true;
	}

	private Message createBoardMessage(Board board) {
		// Create a new message with the board information
		Message message = new Message();
		message.setMessageType(Message.MessageType.START_BOARD);

		// Convert the board state into a string
		String boardState = board.serializeBoardState(); // Removed the argument
		message.setContent(boardState);

		return message;
	}
	private OpponentBoard createOpponentBoard(String boardString) {
		return new OpponentBoard(boardString, event -> {
			// Define the event handler if needed
		});
	}
}