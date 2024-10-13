package bdd_test2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseWriter {

    public static void main(String[] args) {
        // Remplacez les informations de connexion par les vôtres
        String url = "jdbc:mysql://172.30.2.206:3306/smart_poubelle";
        String username = "user";
        String password = "pwduser";

        // Les données à insérer
        int id = 5; // Définir l'ID comme une chaîne de caractères explicite
        float latitude = 19;
        float longitude = 20;
        float Taux_de_remplissage = 50;

        // Requête SQL pour insérer les données dans la table "Conteneurs"
        String sql = "INSERT INTO Conteneurs (ID, latitude, longitude, Taux_de_remplissage) VALUES (?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            // Remplir les paramètres de la requête avec les valeurs des données
            preparedStatement.setInt(1, id);
            preparedStatement.setFloat(2, latitude);
            preparedStatement.setFloat(3, longitude);
            preparedStatement.setFloat(4, Taux_de_remplissage);

            // Exécuter la requête
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Nombre de lignes affectées : " + rowsAffected);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
