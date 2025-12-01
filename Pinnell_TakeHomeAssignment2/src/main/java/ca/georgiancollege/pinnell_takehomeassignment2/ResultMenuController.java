package ca.georgiancollege.pinnell_takehomeassignment2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ResultMenuController {

    @FXML
    private Text gamemodeText, scoreText, correctText, incorrectText, timeleftText, difficultyText;
    @FXML
    Button backtoMenu, again;

    private String gameMode;
    private String score;
    private String correct;
    private String incorrect;
    private String time;
    private String difficulty;
    private AudioClip win;

    public void setGameMode(String mode) {
        this.gameMode = mode;

        if (gamemodeText != null) {
            gamemodeText.setText(mode);
        }
    }

    public void setScore(String score) {
        this.score = score;

        if (scoreText != null) {
            scoreText.setText(score);
        }
    }

    public void setCorrect(String correct) {
        this.correct = correct;

        if (correctText != null) {
            correctText.setText(correct);
        }
    }

    public void setIncorrect(String incorrect) {
        this.incorrect = incorrect;

        if (incorrectText != null) {
            incorrectText.setText(incorrect);
        }
    }

    public void setTime(String time) {
        this.time = time;

        if (timeleftText != null) {
            timeleftText.setText(time);
        }
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;

        if (difficultyText != null) {
            difficultyText.setText(difficulty);
        }
    }

    @FXML
    private void openMainMenu(ActionEvent event) {
        try {
            win.stop();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainmenu.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle("Main Menu");
            newStage.setScene(new Scene(root));
            newStage.show();

            Stage gameStage = (Stage) backtoMenu.getScene().getWindow();
            gameStage.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void playAgain(ActionEvent event) {
        try {
            win.stop();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("drag-and-drop-game-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();

            Stage gameStage = (Stage) again.getScene().getWindow();
            gameStage.close();

            DragAndDropController gameController = fxmlLoader.getController();

            if (Objects.equals(difficulty, "Easy")) {
                gameController.maxTimeInSeconds = 120;
            } else if (Objects.equals(difficulty, "Medium")) {
                gameController.maxTimeInSeconds = 60;
            } else if (Objects.equals(difficulty, "Hard")) {
                gameController.maxTimeInSeconds = 40;
            }
            gameController.setDifficulty(difficulty);

            if (Objects.equals(gameMode, "Numbers")) {
                gameController.gameNumbers();
            } else if (Objects.equals(gameMode, "Words")) {
                gameController.gameWordsLength();
            } else if (Objects.equals(gameMode, "Console Eras")) {
                gameController.gameConsoleEras();
            }
            gameController.setSelectedMode(gameMode);

            gameController.clearScoreboard();
            gameController.timedGame();

            stage.setTitle(gameMode + " - " + difficulty);
            stage.setScene(scene);
            stage.show();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        win = new AudioClip(getClass().getResource("/sounds/win.wav").toExternalForm());
        win.play();
    }
}
