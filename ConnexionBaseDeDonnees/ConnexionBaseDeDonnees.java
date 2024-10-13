package bdd_test2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ConnexionBaseDeDonnees {
	
	public static void main(String[] args) {
        // Informations de connexion à la base de données
        String url = "jdbc:mysql://172.30.2.206:3306/smart_poubelle";

        String utilisateur = "user";
        String motDePasse = "pwduser";

        try {
            // Charger le pilote JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir une connexion à la base de données
            Connection connection = DriverManager.getConnection(url, utilisateur, motDePasse);

            
            // Créer une déclaration (Statement) pour exécuter des requêtes SQL
            Statement statement = connection.createStatement();

            // Exemple de requête SQL pour Conteneurs
            String requeteConteneurs = "SELECT * FROM Conteneurs";
			ResultSet resultSetConteneurs = statement.executeQuery(requeteConteneurs);
            
            // Parcourir les résultats de Conteneurs
            while (resultSetConteneurs.next()) {
                String idConteneurs = resultSetConteneurs.getString("Id");
                Float latitudeConteneurs = resultSetConteneurs.getFloat("latitude");
                Float longitudeConteneurs = resultSetConteneurs.getFloat("longitude");
                int taux_de_remplissageConteneurs = resultSetConteneurs.getInt("Taux_de_remplissage");
                // Traiter les résultats de Conteneurs...
                System.out.println("Conteneurs - ID : " + idConteneurs + ", Latitude : " + latitudeConteneurs + ", Longitude : " + longitudeConteneurs + ", Taux de remplissage : " + taux_de_remplissageConteneurs);
            }
            

            // Fermer les ressources
            resultSetConteneurs.close();
            statement.close();
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

}
