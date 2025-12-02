package ca.georgiancollege.pinnell_takehomeassignment2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import javafx.scene.media.AudioClip;

public class DragAndDropController {
    @FXML
    AnchorPane layout;
    @FXML
    Pane pane1, pane2, pane3;
    @FXML
    Label timer, score, message, category1, category2, category3, combo;
    @FXML
    Button backtoMenu;
    @FXML
    Button strtNewGame;

    private final List<Label> draggableLabels = new ArrayList<>();
    private final List<Double> originalX = new ArrayList<>();
    private final List<Double> originalY = new ArrayList<>();

    private int comboCounter = 0;

    private String difficulty;
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        musicController();
    }

    private String selectedMode;
    public void setSelectedMode(String mode) {
        this.selectedMode = mode;
    }

    private int millisecond = 1000;
    private long timeStart = System.currentTimeMillis();
    public int maxTimeInSeconds, intervalInMS, timeBonus;
    private double orginalPositionX, orginalPositionY;
    private int numberOfRectanglesToAnswer, numOfCorrectAnswers, numOfIncorrectAnswers;
    Timeline timelineGameOver, timelineRemoveMessage, timelineRunTimer;

    String[] fourthGen = {"Sega Genesis","Super Nintendo","TurboGrafx-16"};
    String[] sixthGen = {"Sega Dreamcast","Playstation 2","GameCube"};
    String[] ninethGen = {"Playstation 5","Xbox Series X"};

    private AudioClip correctSound;
    private AudioClip incorrectSound;
    private AudioClip tickSound;
    private AudioClip gameoverSound;
    private AudioClip game;
    private AudioClip game2;
    private AudioClip game3;

    double currentTime;

    public DragAndDropController(){
        intervalInMS = 100;
        timeBonus = 5;
    }

    @FXML
    private void openMainMenu(ActionEvent event) {
        game.stop();
        game2.stop();
        game3.stop();

        if (timelineGameOver != null) timelineGameOver.stop();
        if (timelineRemoveMessage != null) timelineRemoveMessage.stop();
        if (timelineRunTimer != null) timelineRunTimer.stop();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainmenu.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Main Menu");
            stage.setScene(new Scene(root));
            stage.show();

            Stage gameStage = (Stage) backtoMenu.getScene().getWindow();
            gameStage.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startNew(ActionEvent event) {
        if (timelineRunTimer != null) timelineRunTimer.stop();
        if (timelineGameOver != null) timelineGameOver.stop();
        if (timelineRemoveMessage != null) timelineRemoveMessage.stop();

        resetLabels();

        if (difficulty.equals("Easy"))
            maxTimeInSeconds = 120;
        else if (difficulty.equals("Medium"))
            maxTimeInSeconds = 60;
        else
            maxTimeInSeconds = 40;

        clearScoreboard();
        timedGame();
    }

    private void resetLabels() {
        for (int i = 0; i < draggableLabels.size(); i++) {
            Label lbl = draggableLabels.get(i);

            if (lbl.getParent() instanceof Pane p) {
                p.getChildren().remove(lbl);
            }

            layout.getChildren().add(lbl);

            lbl.setLayoutX(originalX.get(i));
            lbl.setLayoutY(originalY.get(i));

            lbl.setOnMousePressed(this::pressedLabel);
            lbl.setOnMouseDragged(this::dragLabel);

            lbl.setOnMouseReleased(event -> {
                if (category1.getText().contains("Len"))
                    releaseLabelWordLength(event);
                else if (category1.getText().contains("Gen"))
                    releaseLabelConsole(event);
                else
                    releaseLabel(event);
            });
        }
        numOfCorrectAnswers = 0;
        numOfIncorrectAnswers = 0;
    }

    @FXML
    private void initialize(){
        if (draggableLabels.isEmpty()) {
            for (Node n : layout.getChildren()) {
                if (n instanceof Label lbl && lbl.getText().equals("ABCDEFG")) {
                    draggableLabels.add(lbl);
                    originalX.add(lbl.getLayoutX());
                    originalY.add(lbl.getLayoutY());
                }
            }
        }

        clearScoreboard();
        timedGame();

        incorrectSound = new AudioClip(getClass().getResource("/sounds/error.wav").toExternalForm());
        correctSound = new AudioClip(getClass().getResource("/sounds/success.wav").toExternalForm());
        tickSound = new AudioClip(getClass().getResource("/sounds/tick.wav").toExternalForm());
        gameoverSound = new AudioClip(getClass().getResource("/sounds/gameover.wav").toExternalForm());
        game = new AudioClip(getClass().getResource("/sounds/game.wav").toExternalForm());
        game2 = new AudioClip(getClass().getResource("/sounds/game2.wav").toExternalForm());
        game3 = new AudioClip(getClass().getResource("/sounds/game3.wav").toExternalForm());

    }

    public void musicController(){
        if (difficulty.equals("Easy"))
            game3.play();
        else if (difficulty.equals("Medium"))
            game2.play();
        else
            game.play();
    }

    public void clearScoreboard(){
        timer.setText("");
        score.setText("0");
        message.setText("");
    }

    public void timedGame(){
        timer.setText(String.valueOf(maxTimeInSeconds));
/*
        Runnable task = ()->{
            double currentTime = Double.parseDouble(timer.getText());
            currentTime*=1000;

            currentTime -= intervalInMS;
            timer.setText(String.valueOf(currentTime));
        };
        Platform.runLater(task);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(task, intervalInMS, intervalInMS, TimeUnit.MILLISECONDS);
*/
        EventHandler eh = new EventHandler<ActionEvent>() {
            private int lastSecond = maxTimeInSeconds;

            @Override
            public void handle(ActionEvent event) {
                currentTime = Double.parseDouble(timer.getText());
                currentTime *= millisecond;
                currentTime -= intervalInMS;
                currentTime /= millisecond;

                currentTime = Math.round(currentTime * 10) / 10.0;

                if (currentTime < 0) {
                    currentTime = 0.0;
                    try {
                        checkToSeeIfGameIsOver();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                int currentSecond = (int) currentTime;
                if (currentSecond < lastSecond) {
                    tickSound.play();
                    lastSecond = currentSecond;
                }

                /*
                if(currentTime % 4 == 0)
                    calculateBonusPoints();
                */

                //System.out.println(currentTime);
                timer.setText(String.valueOf(currentTime));
            }
        };
        timelineRunTimer = new Timeline(new KeyFrame(Duration.millis(intervalInMS), eh));
        timelineRunTimer.setCycleCount(maxTimeInSeconds * millisecond / intervalInMS );
        timelineRunTimer.play();
    }

    public void gameWordsLength(){
        category1.setText("Len < 4");
        category2.setText("Len 4-6");
        category3.setText("Len 7+");

        String[] arr = {"shake","elide","wrack","beele","podiatry","siamang","subjoin","trommel"};
        int counter = 0;

        for(Node item : layout.getChildren()){
            if(item instanceof Label){
                Label current = ((Label)item);
                if(current.getText().equals("ABCDEFG")){
                    current.setText(arr[counter++]);
                    current.setOnMousePressed(this::pressedLabel);
                    current.setOnMouseReleased(this::releaseLabelWordLength);
                    current.setOnMouseDragged(this::dragLabel);
                    numberOfRectanglesToAnswer++;
                }
            }
        }
/*
        Runnable task = ()->{
            try{
                TimeUnit.SECONDS.sleep(maxTimeInSeconds);
            }
            catch (InterruptedException e){
                message.setText("");
            }
        };
        Platform.runLater(task);
  */
        EventHandler gameover = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                layout.setDisable(true);
            }
        };
        timelineGameOver = new Timeline(new KeyFrame(Duration.seconds(maxTimeInSeconds), gameover));
        Platform.runLater(timelineGameOver::play);

    }

    public void gameNumbers(){
        category1.setText("# < 0");
        category2.setText("# < 50");
        category3.setText("# >= 50");

        SecureRandom secureRandom = new SecureRandom();
        IntStream ints = secureRandom.ints(8, -20, 150);

        int[] arr = ints.toArray();
        int counter = 0;

        for(Node item : layout.getChildren()){
            if(item instanceof Label){
                Label current = ((Label)item);
                if(current.getText().equals("ABCDEFG")){
                    current.setText(String.valueOf(arr[counter++]));
                    current.setOnMousePressed(this::pressedLabel);
                    current.setOnMouseReleased(this::releaseLabel);
                    current.setOnMouseDragged(this::dragLabel);
                    numberOfRectanglesToAnswer++;
                }
            }
        }
/*
        Runnable task = ()->{
            try{
                TimeUnit.SECONDS.sleep(maxTimeInSeconds);
            }
            catch (InterruptedException e){
                message.setText("");
            }
        };
        Platform.runLater(task);
  */
        EventHandler gameover = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                layout.setDisable(true);
            }
        };
        timelineGameOver = new Timeline(new KeyFrame(Duration.seconds(maxTimeInSeconds), gameover));
        Platform.runLater(timelineGameOver::play);
    }

    public void gameConsoleEras(){
        category1.setText("4th Gen");
        category2.setText("6th Gen");
        category3.setText("9th Gen");

        List<String> allEras = new ArrayList<>();
        allEras.addAll(Arrays.asList(fourthGen));
        allEras.addAll(Arrays.asList(sixthGen));
        allEras.addAll(Arrays.asList(ninethGen));

        int counter = 0;

        for(Node item : layout.getChildren()){
            if(item instanceof Label){
                Label current = ((Label)item);
                if(current.getText().equals("ABCDEFG")){
                    current.setText(String.valueOf(allEras.get(counter++)));
                    current.setOnMousePressed(this::pressedLabel);
                    current.setOnMouseReleased(this::releaseLabelConsole);
                    current.setOnMouseDragged(this::dragLabel);

                    current.setFont(new Font("System", 11));

                    switch (current.getText()) {
                        case "Sega Genesis":
                        case "Sega Dreamcast":
                            current.setTextFill(Color.DARKCYAN);
                            break;
                        case "TurboGrafx-16":
                            current.setTextFill(Color.DARKORANGE);
                            break;
                        case "Xbox Series X":
                            current.setTextFill(Color.DARKGREEN);
                            break;
                        case "Playstation 2":
                        case "Playstation 5":
                            current.setTextFill(Color.BLUE);
                            break;
                        default:
                            current.setTextFill(Color.RED);
                            break;
                    }

                    numberOfRectanglesToAnswer++;
                }
            }
        }

        EventHandler gameover = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                layout.setDisable(true);
            }
        };
        timelineGameOver = new Timeline(new KeyFrame(Duration.seconds(maxTimeInSeconds), gameover));
        Platform.runLater(timelineGameOver::play);
    }

    @FXML
    private void pressedLabel(MouseEvent event){
        Label current = (Label)event.getSource();
        orginalPositionX = current.getLayoutX();
        orginalPositionY = current.getLayoutY();
        // System.out.println("orig = " + orginalPositionX + ", " + orginalPositionY );

    }

    @FXML
    private void dragLabel(MouseEvent event){
        // System.out.println("Dragged to " + event.getSceneX() + ", " + event.getSceneY());
        Label current = (Label)event.getSource();
        current.setLayoutX(event.getSceneX());
        current.setLayoutY(event.getSceneY());
    }

    @FXML
    private void releaseLabel(MouseEvent event){
        Label current = (Label)event.getSource();
        //System.out.println("Result = " + labelWithinPane(pane1, current.getLayoutX(), current.getLayoutY()));

        if(labelWithinPane(pane1, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrect(1, current);
            addLabelToPane(pane1, current);
        }
        else if(labelWithinPane(pane2, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrect(2, current);
            addLabelToPane(pane2, current);
        }
        else if(labelWithinPane(pane3, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrect(3, current);
            addLabelToPane(pane3, current);
        }
        else{
            current.setLayoutX(orginalPositionX);
            current.setLayoutY(orginalPositionY);

        }

        orginalPositionX = 0;
        orginalPositionY = 0;
        try {
            checkToSeeIfGameIsOver();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void releaseLabelWordLength(MouseEvent event){
        Label current = (Label)event.getSource();
        //System.out.println("Result = " + labelWithinPane(pane1, current.getLayoutX(), current.getLayoutY()));

        if(labelWithinPane(pane1, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrectWordLength(1, current);
            addLabelToPane(pane1, current);
        }
        else if(labelWithinPane(pane2, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrectWordLength(2, current);
            addLabelToPane(pane2, current);
        }
        else if(labelWithinPane(pane3, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrectWordLength(3, current);
            addLabelToPane(pane3, current);
        }
        else{
            current.setLayoutX(orginalPositionX);
            current.setLayoutY(orginalPositionY);

        }

        orginalPositionX = 0;
        orginalPositionY = 0;
        try {
            checkToSeeIfGameIsOver();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void releaseLabelConsole(MouseEvent event){
        Label current = (Label)event.getSource();
        //System.out.println("Result = " + labelWithinPane(pane1, current.getLayoutX(), current.getLayoutY()));

        if(labelWithinPane(pane1, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrectConsole(1, current);
            addLabelToPane(pane1, current);
        }
        else if(labelWithinPane(pane2, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrectConsole(2, current);
            addLabelToPane(pane2, current);
        }
        else if(labelWithinPane(pane3, current.getLayoutX(), current.getLayoutY())){
            checkIfCorrectConsole(3, current);
            addLabelToPane(pane3, current);
        }
        else{
            current.setLayoutX(orginalPositionX);
            current.setLayoutY(orginalPositionY);

        }

        orginalPositionX = 0;
        orginalPositionY = 0;
        try {
            checkToSeeIfGameIsOver();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean labelWithinPane(Pane container, double x, double y){
        //System.out.println("X = " + x + " Container X = " + container.getLayoutX());
        return x >= container.getLayoutX() && x<= container.getLayoutX() + container.getWidth() && y >= container.getLayoutY() && y<= container.getLayoutY() + container.getHeight();
    }

    private void checkIfCorrect(int i, Label current) {
        int value = Integer.parseInt(current.getText());
        switch (i){
            case 1:
                if(value < 0){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Success!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Error");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
            case 2:
                if(value >= 0 && value < 50){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Success!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Error");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
            case 3:
                if(value >= 50){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Success!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Error");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
        }
    }

    private void checkIfCorrectWordLength(int i, Label current) {
        int value = current.getText().length();
        switch (i){
            case 1:
                if(value < 4){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Success!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Error");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
            case 2:
                if(value >= 4 && value < 6){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Success!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Error");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
            case 3:
                if(value >= 7){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Success!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Error");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
        }
    }

    private void checkIfCorrectConsole(int i, Label current) {
        String value = current.getText();
        List<String> fourthGeneration = new ArrayList<String>(Arrays.asList(fourthGen));
        List<String> sixthGeneration = new ArrayList<String>(Arrays.asList(sixthGen));
        List<String> ninethGeneration = new ArrayList<String>(Arrays.asList(ninethGen));

        switch (i){
            case 1:
                if(fourthGeneration.contains(value)){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Level Completed!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Hit!");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
            case 2:
                if (sixthGeneration.contains(value)){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Level Completed!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    incorrectSound.play();
                    splashMessage("Hit!");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
            case 3:
                if(ninethGeneration.contains(value)){
                    applyComboPoints(100);
                    correctSound.play();
                    splashMessage("Level Completed!");
                    numOfCorrectAnswers++;
                }
                else{
                    calculateGeneralPoints(-50);
                    incorrectSound.play();
                    splashMessage("Hit!");
                    numOfIncorrectAnswers++;
                    comboCounter = 0;
                    combo.setText(String.valueOf(comboCounter));
                }
                break;
        }
    }

    private void addLabelToPane(Pane pane, Label label){
        int numChildren = pane.getChildren().size();

        numChildren++;
        System.out.println(numChildren);
        double tx = 10;
        double ty = pane.getHeight() - 10 - (label.getHeight() * numChildren + 5);
        label.setLayoutX(tx);
        label.setLayoutY(ty );
//      System.out.println(tx + ", " + ty);

        pane.getChildren().add(label);
        label.setOnMouseReleased(null);
        label.setOnMousePressed(null);
        label.setOnMouseDragged(null);
    }

    private void splashMessage(String text){
        message.setText(text);
        /*
        Runnable task = ()->{
            try{
                TimeUnit.SECONDS.sleep(1);
                message.setText("");
            }
            catch (InterruptedException e){
            }
        };
        Platform.runLater(task);
        */

        EventHandler removemessage = (ActionEvent) -> message.setText("");

        timelineRemoveMessage = new Timeline(new KeyFrame(Duration.seconds(3), removemessage));
        Platform.runLater(timelineRemoveMessage::play);
    }

    private void calculateBonusPoints(int timeRemaining){
        long current = Long.parseLong(score.getText().isEmpty() ? "0" : score.getText());
        current += timeRemaining;
        score.setText(String.valueOf(current));
    }

    private void calculateGeneralPoints(int value) {
        long current = Long.parseLong(score.getText().isEmpty() ? "0" : score.getText());
        current += value;
        score.setText(String.valueOf(current));
    }

    private void applyComboPoints(int value) {
        comboCounter++;
        int basePoints = value;
        int bonus = (comboCounter - 1) * 5;
        int totalPoints = basePoints + bonus;
        calculateGeneralPoints(totalPoints);
        combo.setText(String.valueOf(comboCounter));
    }


    private void checkToSeeIfGameIsOver() throws IOException {
        System.out.println("****************");
        System.out.println(numOfCorrectAnswers);
        System.out.println(numOfIncorrectAnswers);
        System.out.println(numberOfRectanglesToAnswer);
        System.out.println("****************");

        System.out.println("Outside Function");
        if((numOfCorrectAnswers + numOfIncorrectAnswers == numberOfRectanglesToAnswer) || currentTime == 0) {
            game.stop();
            game2.stop();
            game3.stop();
            System.out.println("Inside Function");
            StringBuilder message = new StringBuilder("Game is over");
            message.append("\n");

            if (numOfCorrectAnswers == numberOfRectanglesToAnswer)
                message.append("You win!");
            else
                message.append("You lose!");

            // new Alert(Alert.AlertType.INFORMATION, message.toString(), ButtonType.OK).show();
            layout.setDisable(true);
            if (timelineGameOver != null) timelineGameOver.stop();
            if (timelineRemoveMessage != null) timelineRemoveMessage.stop();
            if (timelineRunTimer != null) timelineRunTimer.stop();

            Stage gameStage = (Stage) layout.getScene().getWindow();
            gameStage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(MyApplication.class.getResource("resultmenu.fxml"));

            Scene scene = new Scene(fxmlLoader.load());

            calculateBonusPoints((int) currentTime);

            ResultMenuController resultController = fxmlLoader.getController();
            resultController.setGameMode(selectedMode);
            resultController.setScore(score.getText());
            resultController.setCorrect(String.valueOf(numOfCorrectAnswers));
            resultController.setIncorrect(String.valueOf(numOfIncorrectAnswers));
            resultController.setTime(String.valueOf(currentTime));
            resultController.setDifficulty(String.valueOf(difficulty));

            Stage stage = new Stage();
            stage.setTitle("Results");
            stage.setScene(scene);
            stage.show();
        }
    }
}
