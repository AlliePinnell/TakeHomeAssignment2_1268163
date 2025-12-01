package ca.georgiancollege.pinnell_takehomeassignment2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import javax.xml.transform.Result;
import java.io.IOException;

public class MainMenuController {
    @FXML private ToggleGroup difficultyGroup;
    @FXML private ToggleGroup modeGroup;

    @FXML public RadioButton easyRadio;
    @FXML public RadioButton mediumRadio;
    @FXML public RadioButton hardRadio;

    @FXML public RadioButton mode1Radio;
    @FXML public RadioButton mode2Radio;
    @FXML public RadioButton mode3Radio;

    @FXML
    Button startButton;

    private AudioClip gameoverSound;
    private AudioClip menu;

    @FXML
    private void initialize() {
        difficultyGroup = new ToggleGroup();
        easyRadio.setToggleGroup(difficultyGroup);
        mediumRadio.setToggleGroup(difficultyGroup);
        hardRadio.setToggleGroup(difficultyGroup);

        modeGroup = new ToggleGroup();
        mode1Radio.setToggleGroup(modeGroup);
        mode2Radio.setToggleGroup(modeGroup);
        mode3Radio.setToggleGroup(modeGroup);

        easyRadio.setSelected(true);
        mode1Radio.setSelected(true);

        gameoverSound = new AudioClip(getClass().getResource("/sounds/gameover.wav").toExternalForm());
        menu = new AudioClip(getClass().getResource("/sounds/menu.wav").toExternalForm());
        menu.play();
    }

    public void startGame(ActionEvent event) throws IOException {
        String selectedDiff = "";
        String selectedMode = "";

        menu.stop();
        gameoverSound.play();
        FXMLLoader fxmlLoader = new FXMLLoader(MyApplication.class.getResource("drag-and-drop-game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();

        Stage mainMenuStage = (Stage) startButton.getScene().getWindow();
        mainMenuStage.close();

        DragAndDropController gameController = fxmlLoader.getController();

        if (easyRadio.isSelected()) {
            gameController.maxTimeInSeconds = 120;
            selectedDiff = "Easy";
        } else if (mediumRadio.isSelected()) {
            gameController.maxTimeInSeconds = 60;
            selectedDiff = "Medium";
        } else if (hardRadio.isSelected()) {
            gameController.maxTimeInSeconds = 40;
            selectedDiff = "Hard";
        }

        gameController.setDifficulty(selectedDiff);

        if(mode1Radio.isSelected()){
            selectedMode = "Numbers";
            gameController.gameNumbers();
        }
        else if(mode2Radio.isSelected()){
            selectedMode = "Words";
            gameController.gameWordsLength();
        }
        else if(mode3Radio.isSelected()){
            selectedMode = "Console Eras";
            gameController.gameConsoleEras();
        }

        gameController.clearScoreboard();
        gameController.timedGame();

        gameController.setSelectedMode(selectedMode);

        stage.setTitle(selectedMode + " - " + selectedDiff);
        stage.setScene(scene);
        stage.show();
    }

}
