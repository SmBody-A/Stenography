package work.art;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;

public class CardController {

    @FXML
    private Text CategName;

    @FXML
    private ImageView ImageRed;

    @FXML
    private Text NameRed;

    private Reduction reduction;

    private SController parent;

    //Родитель для обновления списка
    public void setParent(SController controller){
        this.parent = controller;
    }

    public void setReduction(Reduction red) {
        this.reduction = red;
        updateUI();
    }

    private void updateUI() {
        try {
            // Если загружать как ресурс, то при добавлении нового, он картинку не выводит
            String filePath = "src/main/resources" + reduction.ImgPath;
            File file = new File(filePath);
            if (file.exists()) {
                Image fileImage = new Image(file.toURI().toString());
                ImageRed.setImage(fileImage);
            }
        } catch (Exception e) {
            ImageRed.setImage(new Image(getClass().getResourceAsStream("/icons/Разметка.png")));
        }
        NameRed.setText(reduction.name);
        CategName.setText(reduction.category);
    }

    @FXML
    void DelReduction(ActionEvent event) {
        DB.deleteShort(reduction.ID, reduction.ImgPath);
        Platform.runLater(()->{
            parent.UpdateUI();
        });
    }

    @FXML
    void RedReduction(ActionEvent event) {
        SController.onRedact = reduction;
        MainFunctions.ShowDialog(
                SController.class,
                "createwind.fxml",
                "Добавить сокращение",
                ((Node)event.getSource()).getScene().getWindow());
        Platform.runLater(()->{
            parent.UpdateUI();
        });
    }
}
