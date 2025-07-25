package work.art;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;


import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class SController {

    @FXML private Button EndPage;
    @FXML private ComboBox<String> FilterBox;
    @FXML private Button FirstPage;
    @FXML private Button LastPage;
    @FXML private FlowPane ListContainer;
    @FXML private Button NextPage;
    @FXML private Button Page1;
    @FXML private Button Page2;
    @FXML private Button Page3;
    @FXML private Button Page4;
    @FXML private Button Page5;
    @FXML private TextField SearchShort;

    private Button[] butpages;
    public int thisPage = 1;
    private int PlaceOnPage = 18;
    private int totalPage, totalElems;

    public static Reduction onRedact;

    @FXML
    private void initialize(){
        butpages = new Button[]{Page1, Page2, Page3, Page4, Page5};
        FilterBox.getItems().addAll(DB.getCategory());
        SearchShort.textProperty().addListener((
                (observableValue, oldV, newV) ->
        {
            //Если сделать без проверки то он зациклится в рекурсию на SetText
            String filteredText = MainFunctions.CheckOnEng(newV);
            if (!newV.equals(filteredText))
                SearchShort.setText(filteredText);
            thisPage = 1;
            UpdateUI();
        }));
        UpdateUI();

    }

    public void UpdateUI(){
        totalElems = DB.getTotalCount(FilterBox.getValue(), SearchShort.getText());
        totalPage = (int) Math.ceil((double) totalElems/PlaceOnPage);
        if(totalPage == 0) totalPage = 1;
        // Скрываем все кнопки страниц
        for (Button button : butpages) {
            button.setVisible(false);
        }
        LoadReduce(DB.getShorts(FilterBox.getValue(),SearchShort.getText(), thisPage, PlaceOnPage));

        // Определяем диапазон отображаемых страниц (5 кнопок)
        int startPage = Math.max(1, thisPage - 2);
        int endPage = Math.min(totalElems, thisPage + 2);

        // Корректируем диапазон для крайних случаев
        if (totalPage <= 5) {
            startPage = 1;
            endPage = totalPage;
        } else if (thisPage <= 3) {
            startPage = 1;
            endPage = 5;
        } else if (thisPage >= totalPage - 2) {
            startPage = totalPage - 4;
            endPage = totalPage;
        }

        // Обновляем кнопки страниц
        int buttonIndex = 0;
        for (int i = startPage; i <= endPage; i++) {
            if (buttonIndex < butpages.length) {
                butpages[buttonIndex].setText(String.valueOf(i));
                butpages[buttonIndex].setVisible(true);
                butpages[buttonIndex].setDisable(i == thisPage);
                buttonIndex++;
            }
        }

        // Обновляем состояние кнопок навигации
        FirstPage.setDisable(thisPage == 1);
        LastPage.setDisable(thisPage == 1);
        NextPage.setDisable(thisPage == totalElems);
        EndPage.setDisable(thisPage == totalElems);


    }

    @FXML
    void AddShort(MouseEvent event){
        MainFunctions.ShowDialog(
                SController.class,
                "createwind.fxml",
                "Добавить сокращение",
                ((Node)event.getSource()).getScene().getWindow());
        UpdateUI();
    }

    @FXML
    void ChooseFilter(ActionEvent event) {
        thisPage = 1;
        UpdateUI();;
    }



    @FXML
    void ClearFilters(MouseEvent event) {
        SearchShort.setText("");
        FilterBox.getSelectionModel().select(null);
    }

    public void LoadReduce(List<Reduction> red){
        ListContainer.getChildren().clear();

        if(red.isEmpty())
            return;

        for (Reduction shor : red){
            try {
                //Загружаем карточкив
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ReducCard.fxml"));
                VBox card = loader.load();
                CardController controller = loader.getController();
                controller.setReduction(shor);
                controller.setParent(this);
                ListContainer.getChildren().add(card);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void ToEndPage(ActionEvent event) {
        thisPage = totalPage;
        UpdateUI();
    }

    @FXML
    void ToFirstPage(ActionEvent event) {
        thisPage = 1;
        UpdateUI();
    }

    @FXML
    void ToLastPage(ActionEvent event) {
        // Переход на предыдущую страницу
        if (thisPage > 1) {
            thisPage--;
            UpdateUI();
        }
    }

    @FXML
    void ToNextPage(ActionEvent event) {
        // Переход на следующую страницу
        if (thisPage < totalPage) {
            thisPage++;
            UpdateUI();
        }
    }

    @FXML
    void ToPage(ActionEvent event) {
        // Переход на конкретную страницу
        Button clickedButton = (Button) event.getSource();
        thisPage = Integer.parseInt(clickedButton.getText());
        UpdateUI();
    }

}