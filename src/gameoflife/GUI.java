package gameoflife;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * GUI class extends javaFX Application and displays the Game of Life GUI
 * 
 * @author Folz
 *
 */
public class GUI extends Application{
	Board gameBoard;
	GridPane board;
	int generations = 0;
	int tick = 50;
	String liveCellColor = "green";
	String deadCellColor = "black";
	String gridColor = "grey";
	int gridSizeX, gridSizeY;
	int frequency = 100;
	boolean impulsing = false;
	double impulseStrength;
	
	@Override
	public void start(Stage primaryStage) {
		gameBoard = new Board(35, 35);

		primaryStage.setTitle("Conway's Game of Life");
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 700, 700);
		primaryStage.getIcons().add(raiseFlag());
		
		board = new GridPane();
		board.setPadding(new Insets(5, 5, 10, 5));
		board.setStyle("-fx-background-color: " + gridColor + ";");
		board.setHgap(2);
		board.setVgap(2);
		setUpBoard();
		
		root.setCenter(board);
		root.setTop(makeMenuBar(primaryStage));
		root.setBottom(makeControlPanel());
		
		primaryStage.setScene(scene);
		primaryStage.show();
		display();
		
		primaryStage.setOnCloseRequest((event) -> {
			System.exit(0);;
		});
	}
	
	/**
	 * Refresh function for the display.  Updates the color of each cell.
	 */
	private void display() {
		//optimization
		if (gridSizeX > gameBoard.getXLength() || gridSizeY > gameBoard.getYLength()) {
			Iterator<Node> iterator = board.getChildren().iterator();
			while (iterator.hasNext()) {
				Node square = iterator.next();
				if (gameBoard.getXLength() <= GridPane.getColumnIndex(square) || gameBoard.getYLength() <= GridPane.getRowIndex(square)) {
					iterator.remove();
				}
			}
		}
		
		Pane temp;
		final boolean[] drawLive = {false};
		for(int x = 0; x < gameBoard.getXLength(); ++x){
			for(int y = 0; y < gameBoard.getYLength(); ++y){
				try {
					if(gameBoard.getBoard()[y][x]){ 
						getNodeByCoordinate(x, y).setStyle("-fx-background-color: " + liveCellColor + ";");
					}else{
						getNodeByCoordinate(x, y).setStyle("-fx-background-color: " + deadCellColor + ";");
					}
				} catch (NullPointerException e) { //expanding grid
					temp = new Pane();
					temp.setPrefSize(28, 28);
					board.add(temp, x, y);
					
					if(gameBoard.getBoard()[y][x]){ 
						temp.setStyle("-fx-background-color: " + liveCellColor + ";");
					}else{
						temp.setStyle("-fx-background-color: " + deadCellColor + ";");
					}
					
					temp.setOnMouseClicked((event) -> {
						Pane source = (Pane)event.getSource();
						int xLocation = GridPane.getColumnIndex(source);
						int yLocation = GridPane.getRowIndex(source);
						gameBoard.getBoard()[yLocation][xLocation] = !gameBoard.getBoard()[yLocation][xLocation];
						display();
					});
					
					
					temp.setOnMouseDragEntered((event) -> {
						Pane source = (Pane)event.getSource();
						int xLocation = GridPane.getColumnIndex(source);
						int yLocation = GridPane.getRowIndex(source);
						
						gameBoard.getBoard()[yLocation][xLocation] = drawLive[0];
						
						display();
					});
					
					
					temp.setOnDragDetected((event) -> {
						Pane source = (Pane)event.getSource();
						int xLocation = GridPane.getColumnIndex(source);
						int yLocation = GridPane.getRowIndex(source);
						drawLive[0] = !gameBoard.getBoard()[yLocation][xLocation];
						((Pane)event.getSource()).startFullDrag();
					});
				}
			}
		}
	}

	/**
	 * Method for initializing the main control panel for the program.
	 * 
	 * @return Returns the completed control panel.
	 */
	private Node makeControlPanel() {
		HBox controlPanel = new HBox(10);
		File playFile = new File("src\\gameoflife\\images\\play.png");
		File pauseFile = new File("src\\gameoflife\\images\\pause.png");
		final boolean[] playing = {false};
		final Image[] play = {null};
		final Image[] pause = {null};
		
		FileInputStream input;
		try{
			input = new FileInputStream(playFile);
			play[0] = new Image(input);
			input.close();
			input = new FileInputStream(pauseFile);
			pause[0] = new Image(input);
			input.close();
		}catch(IOException e){
			
		}
		
		final Thread[] taskThread = {null}; //there has to be a better way to do this

		HBox button = new HBox();
		ImageView image = new ImageView(play[0]);
		button.getChildren().add(image);
		button.setAlignment(Pos.CENTER);
		button.setOnMouseClicked((event) -> {
			if(playing[0]){ //if it is playing, click to pause
				image.setImage(play[0]);
				//stop doing things
				taskThread[0].interrupt();
			}else{
				image.setImage(pause[0]);
				//resume doing things
				taskThread[0] = new Thread( () -> {
					while(!Thread.currentThread().isInterrupted()){
						try {
							Thread.sleep(tick);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								gameBoard.refreshBoard();
								if(impulsing && generations%frequency == 0){
									gameBoard.impulse(impulseStrength);
								}
								generations++;
								display();
							}//run
						});//runLater
					}//while
				});//taskThread
				taskThread[0].start();
			}
			playing[0] = !playing[0];
		});
		
		HBox liveColorBox = new HBox();
		ColorPicker liveColorPicker = new ColorPicker(Color.GREEN);
		Label liveColorLabel = new Label("Live cell color: ");
		liveColorBox.getChildren().addAll(liveColorLabel, liveColorPicker);
		liveColorBox.setAlignment(Pos.CENTER);
		liveColorPicker.setOnAction((event) -> {
	         Color temp = liveColorPicker.getValue();
	         int r,g,b;
	         r = (int)(temp.getRed()  *255);
	         g = (int)(temp.getGreen()*255);
	         b = (int)(temp.getBlue() *255);
	         liveCellColor = "#" + Integer.toHexString(r) + (Integer.toHexString(r).length() == 1? "0":"") +
					   Integer.toHexString(g) + (Integer.toHexString(g).length() == 1? "0":"") +
					   Integer.toHexString(b) + (Integer.toHexString(b).length() == 1? "0":"");
	         display();
		     
		 });
		 
		 HBox deadColorBox = new HBox();
		 ColorPicker deadColorPicker = new ColorPicker(Color.BLACK);
		 Label deadColorLabel = new Label("Dead cell color: ");
		 deadColorBox.getChildren().addAll(deadColorLabel, deadColorPicker);
		 deadColorBox.setAlignment(Pos.CENTER);
		 deadColorPicker.setOnAction((event) -> {
	         Color temp = deadColorPicker.getValue();
	         int r,g,b;
	         r = (int)(temp.getRed()  *255);
	         g = (int)(temp.getGreen()*255);
	         b = (int)(temp.getBlue() *255);
	         deadCellColor = "#" + Integer.toHexString(r) + (r==0? "0":"") +
		 						   Integer.toHexString(g) + (g==0? "0":"") +
	 							   Integer.toHexString(b) + (b==0? "0":"");
	         display();
		     
		 });
		 
		 controlPanel.getChildren().addAll(liveColorBox, button, deadColorBox);
		 controlPanel.setAlignment(Pos.CENTER);
		 controlPanel.setPrefHeight(60);
		 controlPanel.setStyle("-fx-background-color: #dde8f0; "
							+ "-fx-effect: dropshadow(gaussian, #dde8f0, 15, .75, 0, 0);");
		 return controlPanel;
	}

	/**
	 * Method for initializing and displaying the board.
	 */
	private void setUpBoard() {
		gameBoard.setup();
		gridSizeX = gameBoard.getXLength();
		gridSizeY = gameBoard.getYLength();
		final boolean[] drawLive = {false};
		
		Pane temp;
		for(int x = 0; x < gameBoard.getXLength(); ++x){
			for(int y = 0; y < gameBoard.getYLength(); ++y){
				temp = new Pane();
				if(gameBoard.getBoard()[y][x]){
					temp.setStyle("-fx-background-color: " + liveCellColor + ";");
				}else{
					temp.setStyle("-fx-background-color: " + deadCellColor + ";");
				}
				temp.setPrefSize(28, 28);
				
				temp.setOnMouseClicked((event) -> {
					Pane source = (Pane)event.getSource();
					int xLocation = GridPane.getColumnIndex(source);
					int yLocation = GridPane.getRowIndex(source);
					
					gameBoard.getBoard()[yLocation][xLocation] = !gameBoard.getBoard()[yLocation][xLocation];
					display();
				});
				
				temp.setOnMouseDragEntered((event) -> {
					Pane source = (Pane)event.getSource();
					int xLocation = GridPane.getColumnIndex(source);
					int yLocation = GridPane.getRowIndex(source);
					
					gameBoard.getBoard()[yLocation][xLocation] = drawLive[0];
					
					display();
				});
				
				
				temp.setOnDragDetected((event) -> {
					Pane source = (Pane)event.getSource();
					int xLocation = GridPane.getColumnIndex(source);
					int yLocation = GridPane.getRowIndex(source);
					drawLive[0] = !gameBoard.getBoard()[yLocation][xLocation];
					((Pane)event.getSource()).startFullDrag();
				});
				
				board.add(temp, x, y);
			}
		}
	}

	/**
	 * Creates a menu bar full of all sorts of fancy options.
	 * 
	 * @param primaryStage Parent stage of the sub-windows that certain options create
	 * @return Returns the completed menu bar.
	 */
	private Node makeMenuBar(Stage primaryStage) {
		MenuBar mainMenu = new MenuBar();
		
		
		Menu file = new Menu("File");
		
		MenuItem save = new MenuItem("Save As...");
		save.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("LIFE files (*.life)", "*.life");
            fileChooser.getExtensionFilters().add(extFilter);
			File selectedFile = fileChooser.showSaveDialog(primaryStage);
			
			try{
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile));
				oos.writeObject(gameBoard);
				oos.close();
			}catch(IOException e){
				
			}
			
		});
		
		MenuItem load = new MenuItem("Load...");
		load.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("LIFE files (*.life)", "*.life");
            fileChooser.getExtensionFilters().add(extFilter);
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
            
			try{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile));
				gameBoard = (Board)ois.readObject();
				ois.close();
				display();
			}catch(FileNotFoundException e){
				System.out.println("B");
				
			}catch(ClassNotFoundException e){
				System.out.println("C");
				
			}catch(IOException e){
				System.out.println("A");
			}
			
		});
		
		/*
		 * unimplemented
		 * Plan was to import an image from a common format, convert to an image of RGB values,
		 * convert the RGB into a binary image where each pixel is true if corresponding RBG pixel is within a certain %error of the current live cell color,
		 * then interpolate that image from whatever the native image resolution is, into whatever the current resolution of the board is,
		 * save the resulting board into the gameBoard, then display the new board
		 */
//		MenuItem importImage = new MenuItem("Import Image");
//		importImage.setOnAction((event) -> {
//			FileChooser fileChooser = new FileChooser();
//			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(".png files (*.png)", "*.png");
//            fileChooser.getExtensionFilters().add(extFilter);
//			File selectedFile = fileChooser.showOpenDialog(primaryStage);
//            
//			try{
//				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile));
//				
//				
//				
//				
//				//ois.close();
//			}catch(FileNotFoundException e){
//				System.out.println("B");
//			}catch(IOException e){
//				System.out.println("A");
//			}
//		});
		
		MenuItem newGame = new MenuItem("New Game");
		newGame.setOnAction((event) -> {
			gameBoard.setup();
			display();
		});
		
		file.getItems().addAll(save, load, newGame);
		
		
		Menu edit = new Menu("Edit");
		
		MenuItem clone = new MenuItem("Clone Board");
		clone.setOnAction((event) -> {
			GUI application = new GUI();
			Stage applicationStage = new Stage();
			application.start(applicationStage);
			application.setGameBoard(gameBoard);
			application.display();
		});
		
		MenuItem resize = new MenuItem("Resize Grid");
		resize.setOnAction((event) -> {
			Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            
            VBox resizeLabels = new VBox(10);
            Label xSizeLabel = new Label("X size: ");
            Label ySizeLabel = new Label("Y size: ");
            resizeLabels.getChildren().addAll(xSizeLabel, ySizeLabel);
            resizeLabels.setAlignment(Pos.CENTER);
            
            VBox resizeFields = new VBox(5);
            TextField xSizeField = new TextField("" + (gameBoard.getXLength()));
            TextField ySizeField = new TextField("" + (gameBoard.getYLength()));
            resizeFields.getChildren().addAll(xSizeField, ySizeField);
            resizeFields.setAlignment(Pos.CENTER);
            
            HBox resizeBox = new HBox();
            resizeBox.getChildren().addAll(resizeLabels, resizeFields);
            resizeBox.setAlignment(Pos.CENTER);
            
            VBox root = new VBox(10);
            Button submit = new Button("submit");
            submit.setOnMouseClicked((subEvent) -> {
            	int xSize = Integer.parseInt(xSizeField.getText());
            	int ySize = Integer.parseInt(ySizeField.getText());
            	gameBoard.setBoardSize(xSize, ySize);
            	
            	display();
        		gridSizeX = gameBoard.getXLength();
        		gridSizeY = gameBoard.getYLength();
            	dialog.close();
            });

            HBox submitBox = new HBox();
            submitBox.setAlignment(Pos.CENTER);
            submitBox.getChildren().add(submit);
            root.getChildren().addAll(resizeBox, submitBox);
            root.setAlignment(Pos.CENTER);
            
            Scene dialogScene = new Scene(root, 250, 150);
            dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Resize");
            dialog.setScene(dialogScene);
            dialog.show();
		});
		
		MenuItem clearBoard = new MenuItem("Clear Board");
		clearBoard.setOnAction((event) -> {
			gameBoard.clearBoard();
			display();
		});
		
		MenuItem advanced = new MenuItem("Advanced Options");
		ColorPicker gridColorPicker = new ColorPicker(Color.GREY);
		advanced.setOnAction((event) -> {
			Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
			VBox root = new VBox(10);
			root.setPadding(new Insets(20));
            
            HBox gridColorBox = new HBox();
    		Label gridColorLabel = new Label("Grid line color: ");
    		gridColorBox.getChildren().addAll(gridColorLabel, gridColorPicker);
    		gridColorBox.setAlignment(Pos.CENTER);
			gridColorPicker.setOnAction((subEvent) -> {
		        Color temp = gridColorPicker.getValue();
		        int r,g,b;
		        r = (int)(temp.getRed()  *255);
		        g = (int)(temp.getGreen()*255);
		        b = (int)(temp.getBlue() *255);
		        gridColor = "#" + Integer.toHexString(r) + (r==0? "0":"") +
								  Integer.toHexString(g) + (g==0? "0":"") +
	 						      Integer.toHexString(b) + (b==0? "0":"");

				board.setStyle("-fx-background-color: " + gridColor + ";");
			});
			
			HBox speedBox = new HBox();
			Slider gameSpeed = new Slider();
			Label speedLabel = new Label("Game speed: ");
			gameSpeed.setMin(50);
			gameSpeed.setMax(1500);
			gameSpeed.setValue(1550 - tick);
			gameSpeed.setOnDragDetected((subEvent) -> {
				try {
					((Node)event.getSource()).startFullDrag();
				} catch (ClassCastException e) {
					//dragging wrong element
				}
			});
			gameSpeed.setOnMouseDragged((subEvent) -> {
				tick = 1550 - (int)gameSpeed.getValue();
			});
			
			speedBox.getChildren().addAll(speedLabel, gameSpeed);
			speedBox.setAlignment(Pos.CENTER);
			
			
			VBox impulseBox = new VBox(8);
			impulseBox.setPadding(new Insets(20));
			
			HBox impulseToggleBox = new HBox(5);
			Label toggleLabel = new Label("Impulse:        ");
			RadioButton toggleOn = new RadioButton("On");
			RadioButton toggleOff = new RadioButton("Off");
			ToggleGroup impulseToggle = new ToggleGroup();
			toggleOn.setToggleGroup(impulseToggle);
			toggleOff.setToggleGroup(impulseToggle);
			if (impulsing){
				impulseToggle.selectToggle(toggleOn);
			}else{
				impulseToggle.selectToggle(toggleOff);
			}
			impulseToggleBox.getChildren().addAll(toggleLabel, toggleOn, toggleOff);

			Label frequencyLabel = new Label("Frequency: ");
			TextField frequencyField = new TextField("" + frequency);

			HBox frequencyBox = new HBox(10);
			frequencyBox.getChildren().addAll(frequencyLabel, frequencyField);
			
			HBox strengthBox = new HBox();
			Slider strengthSlider = new Slider();
			Label strengthLabel = new Label("Impulse Strength: ");
			strengthSlider.setMin(0);
			strengthSlider.setMax(33);
			strengthSlider.setValue(0);
			strengthSlider.setOnDragDetected((subEvent) -> {
				try {
					((Node)event.getSource()).startFullDrag();
				} catch (ClassCastException e) {
					//dragging wrong element
				}
			});
			strengthSlider.setOnMouseDragged((subEvent) -> {
				impulseStrength = strengthSlider.getValue()/100;
			});
			strengthBox.getChildren().addAll(strengthLabel, strengthSlider);
			strengthBox.setAlignment(Pos.CENTER);
			
			Button applyImpulse = new Button("Apply Impulse");
			applyImpulse.setOnAction((subEvent) -> {
				frequency = Integer.parseInt(frequencyField.getText());
				impulsing = impulseToggle.getSelectedToggle().equals(toggleOn);
			});
			
			impulseBox.getChildren().addAll(impulseToggleBox, frequencyBox, strengthBox, applyImpulse);
			
			root.getChildren().addAll(gridColorBox, speedBox, impulseBox);
			Scene dialogScene = new Scene(root, 300, 250);
            dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Advanced");
            dialog.setScene(dialogScene);
            dialog.show();
		});
		
		
		edit.getItems().addAll(clone, resize, clearBoard, advanced);
		
		
		Menu help = new Menu("Help");
		
		MenuItem helpItem = new MenuItem("Help");
		helpItem.setOnAction((event) -> {
			Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            
            VBox dialogBox = new VBox();
            Label rules = new Label(  "The program iterates once every " + tick/1000.0 + " second" + (tick==1000? "":"s") + " according to the following rules: \n"
				            		+ "\t1) A dead cell with exactly three live neighbours will become alive, \n"
				            		+ "\t    otherwise it will stay dead.\n"
				            		+ "\t2) A live cell with two or three live neighbours will stay alive, \n"
				            		+ "\t    otherwise it will die.");
            Label userInputInfo = new Label(  "The user of this program can switch a cell from alive to dead, or from dead to alive \n"
            								+ "by clicking on the desired cell.  If there is no user input, the program will continue\n"
            								+ "iterating.  The user may also pause and resume the simulation, as well as change \n"
            								+ "the colors of the alive and dead cells.  Additional user options can be found in the \n"
            								+ "advanced settings under the edit tab.");
            dialogBox.getChildren().addAll(rules, userInputInfo);
            
            Scene dialogScene = new Scene(dialogBox, 450, 200);
            dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Help");
            dialog.setScene(dialogScene);
            dialog.show();
		});
		
		MenuItem info = new MenuItem("Info");
		info.setOnAction((event) -> {
			//display info about Conway's Game of Life
			Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            
            VBox dialogBox = new VBox();
            Label summary = new Label("“The Game of Life is a cell-simulation game invented by John Conway in 1970. A\n"
		            				+ "grid of square cells is presented. Each cell has eight neighbors. Each cell can be\n"
		            				+ "in one of two states—alive or dead. The cells experience generations. Each\n"
		            				+ "generation, a living cell with two or three living neighbors stays alive. A cell\n"
		            				+ "with any other number of neighbors (less or more) dies. A dead cell with three\n"
		            				+ "living neighbors comes to life (Dart for Absolute Beginners p 138, Kopec).”");
            //Label history = new Label("PLACEHOLDER");
            dialogBox.getChildren().addAll(summary);
            
            Scene dialogScene = new Scene(dialogBox, 450, 200);
            dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Information");
            dialog.setScene(dialogScene);
            dialog.show();
            
		});
		
		help.getItems().addAll(helpItem, info);
		
		mainMenu.getMenus().addAll(file, edit, help);
		return mainMenu;
	}

	/**
	 * Utility function to locate a node in a GridPane by a particular pair of coordinates
	 * 
	 * @param x X coordinate of desired Node
	 * @param y Y coordinate of desired Node
	 * @return Returns the desired Node
	 */
	private Node getNodeByCoordinate(int x, int y){ 
		Node temp = null;
		for(Node node : board.getChildren()){ //there has to be a better way to do this
			if(x == GridPane.getColumnIndex(node) && y == GridPane.getRowIndex(node)){
				temp = node;
				break;
			}
		}
		return temp;
	}

	/**
	 * There's a long story behind this flag that I really don't feel like putting in a javadoc.
	 * @return Returns the flag.
	 */
	private Image raiseFlag() {
		File flagFile = new File("src\\gameoflife\\images\\Flag.png");
		InputStream input = null;
		Image flag = null;
		try{
			input = new FileInputStream(flagFile);
			flag = new Image(input);
			input.close();
		}catch(FileNotFoundException e){
			
		}catch(IOException e){
			
		}
		return flag;
	}
	
	/**
	 * Sets a given board as the board to display.
	 * @param gameBoard Board to display
	 */
	private void setGameBoard(Board gameBoard){
		this.gameBoard = gameBoard;
	}

	public static void main(String[] args) {
	    launch(args);
	}
}
