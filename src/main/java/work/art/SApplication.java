package work.art;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/work/art/shorts-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/icons/pen.png"))
        );
        stage.setResizable(false);
        stage.setTitle("Стенография");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}