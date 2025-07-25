package work.art;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {
    //resources/stensimbols.db - путь к БД относительно папки проекта
    private static final String dblink;

    static {
        // Явная загрузка драйвера SQLite
        String dbPath = Path.of(System.getProperty("user.dir"), "Database", "stensimbols.db").toString();
        dblink = "jdbc:sqlite:" + dbPath;
    }
    //Соединение с БД
    public static Connection connect(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dblink);
        }
        catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return conn;
    }
    //В методы передавать сразу запрос
    //Или сделать отдельный метод для формирования запроса


    //Пример метода для вставки данных
    public static void InserData(String Name, String ImagePath, int CatgID){
        String sql ="INSERT INTO Сокращения(Наименование, Изображение, Категория) Values(?,?,?)";
        //PreparedStatement — безопасный способ вставки данных
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            //На места знаков вопросов подставляем значения
            pstmt.setString(1,Name);
            pstmt.setString(2,ImagePath);
            pstmt.setInt(3, CatgID);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Список категорий
    public static List<String> getCategory(){
        /*
        В круглых скобках - ресурсы, которые нужно закрыть.
        После выхода из блока try ресурсы закрываются автоматически в обратном порядке.
        Во избежания утечек памяти*/
        List<String> categories = new ArrayList<>();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("Select Наименование from Категория")) {

            while (rs.next())
                categories.add(rs.getString(1));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return categories;
    }



    // Метод для получения общего количества элементов
    public static int getTotalCount(String categ, String Name) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM Сокращения");
        List<String> params = new ArrayList<>();

        if (categ != null && !categ.isEmpty() && !categ.equals("Отсутствует")) {
            countSql.append(" WHERE Категория = (SELECT Код FROM Категория WHERE Наименование = ?)");
            params.add(categ);
        }

        if (Name != null && !Name.isEmpty()) {
            countSql.append(params.isEmpty() ? " WHERE " : " AND ")
                    .append("LOWER(Наименование) LIKE LOWER(?)");
            params.add("%" + Name + "%");
        }

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(countSql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка выполнения запроса count: " + e.getMessage());
        }
        return 0;
    }

    // Метод для получения данных с пагинацией
    public static List<Reduction> getShorts(String categ, String Name, int page, int itemsPerPage) {
        List<Reduction> allShort = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Сокращения");
        List<String> params = new ArrayList<>();

        if (categ != null && !categ.isEmpty() && !categ.equals("Отсутствует")) {
            sql.append(" WHERE Категория = (SELECT Код FROM Категория WHERE Наименование = ?)");
            params.add(categ);
        }

        if (Name != null && !Name.isEmpty()) {
            sql.append(params.isEmpty() ? " WHERE " : " AND ")
                    .append("LOWER(Наименование) LIKE LOWER(?)");
            params.add("%" + Name + "%");
        }

        sql.append(" LIMIT ? OFFSET ?");
        int offset = (page - 1) * itemsPerPage;
        params.add(String.valueOf(itemsPerPage));
        params.add(String.valueOf(offset));

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reduction rd = new Reduction();
                    rd.ID = rs.getInt(1);
                    rd.name = rs.getString(2);
                    rd.ImgPath = rs.getString(3);
                    rd.category = GetCategoryName(rs.getInt(4));
                    allShort.add(rd);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка выполнения запроса: " + e.getMessage());
        }

        return allShort;
    }



    //Номер последнего сокращения
    public static int GetLast(){
        int id = 1;
        try (Connection conn = connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("Select MAX(Код) from Сокращения")
                ){
             if (rs.next())
                 id = rs.getInt(1) + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public static int GetCategory(String Name){
        int id = 1;
        String sql = "Select Код from Категория Where Наименование = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, Name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public static String GetCategoryName(int id){
        String name = "Отсутствует";
        String sql = "Select Наименование from Категория Where Код = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                name = rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public static void deleteShort(int id, String imgPath) {
        String deleteSql = "DELETE FROM Сокращения WHERE Код = ?";
        try (Connection conn = connect();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            // Удаляем запись
            deleteStmt.setInt(1, id);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка удаления сокращения: " + e.getMessage());
        }
        try {
            Path way = Paths.get("src/main/resources"+imgPath);
            Files.delete(way);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void UpdateShort(int id, String name, int categ) {
        String sql = "UPDATE Сокращения SET Наименование = ?, Категория = ? WHERE Код = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Устанавливаем параметры
            pstmt.setString(1, name);
            pstmt.setInt(2, categ);
            pstmt.setInt(3, id);

            // Выполняем обновление
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("Ни одна запись не была обновлена. Проверьте ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении сокращения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
