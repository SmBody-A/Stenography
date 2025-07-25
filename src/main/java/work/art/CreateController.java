package work.art;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateController  {

    private List<WritableImage> history = new ArrayList<>();
    private int drawnow = -1;
    WritableImage image;
    Image bgImg = new Image(getClass().getResource("/icons/Разметка.png").toExternalForm());

    @FXML
    private ComboBox<String> CategList;

    @FXML
    private Canvas PaintPlace;

    @FXML
    private TextArea Name;

    //Контекст для рисования
    private GraphicsContext gc;
    private boolean isDrawing = false;

    @FXML
    public void initialize() {
        List<String> categories = DB.getCategory();
        categories.removeFirst();
        CategList.getItems().addAll(categories);
        //Получаем контекст
        gc = PaintPlace.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2.5);

        //Редактируется или создается новый?
        if (SController.onRedact == null)
            clearBG();
        else{
            // Путь к изображению относительно папки проекта
            String imagePath = "src/main/resources" + SController.onRedact.ImgPath;
            File imageFile = new File(imagePath);

            // Проверяем существование файла
            if (imageFile.exists()) {
                // Загружаем через URI
                Image image = new Image(imageFile.toURI().toString());
                gc.drawImage(image, 0, 0, PaintPlace.getWidth(), PaintPlace.getHeight());
            } else
                clearBG();

            Name.setText(SController.onRedact.name);
            CategList.setValue(SController.onRedact.category);
        }

        Name.textProperty().addListener(
                (observableValue, oldV, newV) -> {
            String filter = MainFunctions.CheckOnEng(newV);
            if(!newV.equals(filter))
                Name.setText(filter);
        });

        // Чтобы Canvas мог получать фокус и обрабатывать клавиши
        PaintPlace.setFocusTraversable(true);
    }

    @FXML
    void clearImg(ActionEvent event) {
        clearBG();
    }

    @FXML
    void BackPaint(ActionEvent event) {
        Undo();
    }

    @FXML
    void RollBack(KeyEvent event) {
        if(event.isControlDown() && event.getCode() == KeyCode.Z)
            Undo();
    }

    private void Undo(){
        //Ограниченние по пустой истории
        if (drawnow>0){
            drawnow--;
            image = history.get(drawnow);
            //Очистка Canvas
            gc.clearRect(0,0, PaintPlace.getWidth(), PaintPlace.getHeight());
            gc.drawImage(image,0,0);
        }
    }

    @FXML
    void SaveShort(ActionEvent event) {
        if(CategList.getValue() != null && !Name.getText().trim().isEmpty()) {
            try {
                image = PaintPlace.snapshot(null, null);

                BufferedImage bf = new BufferedImage(
                        (int) image.getWidth(),
                        (int) image.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );

                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++)
                        bf.setRGB(x, y, image.getPixelReader().getArgb(x, y));
                }

                String fileName = "short";
                if (SController.onRedact == null){
                    fileName += DB.GetLast() + ".png";
                    DB.InserData(Name.getText().trim(), "/Images/" + fileName,
                            DB.GetCategory(CategList.getValue()));
                }
                else{
                    fileName += SController.onRedact.ID + ".png";
                    DB.UpdateShort(SController.onRedact.ID, Name.getText().trim(), DB.GetCategory(CategList.getValue()));
                    SController.onRedact = null;
                }

                String savePath = "src/main/resources/Images/" + fileName;
                ImageIO.write(bf, "png", new File(savePath));


                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Сообщение");
                alert.setHeaderText("Сохранено!");
                alert.setContentText("Файл сохранен.");
                alert.showAndWait();

                Stage stage = (Stage) Name.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Не удалось сохранить изображение");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Предупреждение!");
            alert.setContentText("Не выбрана категория или\nотсутствует название.");
            alert.showAndWait();
        }
    }
    @FXML
    private void clearBG(){
        gc.drawImage(bgImg, 0, 0, PaintPlace.getWidth(), PaintPlace.getHeight());
    }

    @FXML
    void setMouseDragged(MouseEvent event) {
        if (isDrawing){
              // Подготовка к следующему сегменту
            // Рисуем линию до новой позиции
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
            // Начинаем новый путь с текущей позиции
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
        }
    }

    @FXML
    void setMousePressed(MouseEvent event) {
        //Устанавливаем фокус
        PaintPlace.requestFocus();
        isDrawing = true;
        //Новый путь
        gc.beginPath();
        gc.moveTo(event.getX(),  event.getY());
    }

    @FXML
    void setMouseReleased(MouseEvent event) {
        isDrawing = false;

        //Удаляем все состояния после текущего
        if(drawnow < history.size()-1)
            history = history.subList(0, drawnow+1);

        //Сохраняем текущий Canvas в историю
        image = new WritableImage((int) PaintPlace.getWidth(),
                (int) PaintPlace.getHeight());
        PaintPlace.snapshot(null, image);
        history.add(image);
        drawnow = history.size()-1;
    }
}
