package work.art;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFunctions {

    //Открыть новое, закрыть старое
    public void Show(Class<?> controller, String WinName, String Title, Window Parent){
        Title = (Title == null) ? "Окно" : Title;
        try {
            FXMLLoader loader = new FXMLLoader(controller.getResource(WinName));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Создаем новое окно
            Stage newStage = new Stage();
            newStage.setTitle(Title);
            newStage.setScene(scene);

            // Закрываем текущее окно
            Stage currentStage = (Stage)Parent;
            currentStage.close();

            // Показываем новое окно
            newStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Открыть окно с блокировкой предыдущего
    public static void ShowDialog(Class<?> controller, String WinName, String Title, Window Parent){
        Title = (Title == null) ? "Окно" : Title;
        try {
            FXMLLoader loader = new FXMLLoader(controller.getResource(WinName));
            Parent root = loader.load();
            Stage createWind = new Stage();
            createWind.setTitle(Title);
            createWind.setScene(new Scene(root));
            createWind.getIcons().add(
                    new Image(controller.getResourceAsStream("/icons/pen.png"))
            );
            createWind.setResizable(false);
            //Блокируем родительское окно
            createWind.initModality(Modality.APPLICATION_MODAL);
            //Указание родительского окна
            createWind.initOwner(Parent);

            createWind.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //ShowDialog(getClass().getResource(CreateController.class,"createwind.fxml"),"Добавить сокращение",
    // ((Node)event.getSource()).getScene().getWindow());

    //Ограничение англ букв
    public static String CheckOnEng(String str){
        Pattern ptn = Pattern.compile("[a-zA-Z]");
        Matcher mat = ptn.matcher(str);
        return mat.replaceAll("");
    }
}
