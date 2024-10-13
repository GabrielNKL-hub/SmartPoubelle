package bdd_test2;

import java.net.*; //prof
import java.io.*; //prof
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SecureTcpClient{

    public static void main(String[] args) {
        String serverHost = "172.30.3.69";
        int serverPort = 12345;  // Remplacez par le port approprié
        String trustStorePath = "path/to/truststore.jks";  // Remplacez par le chemin de votre truststore
        String trustStorePassword = "your_truststore_password";  // Remplacez par le mot de passe de votre truststore

        try {
            // Charger le truststore pour une communication sécurisée
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            //SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverHost, serverPort);
            Socket sslSocket = new Socket(serverHost,serverPort); //Prof
            // Configurer les flux d'entrée et de sortie
            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            // Envoyer la demande de poids du camion
            out.println("GET_WEIGHT");

            // Recevoir la réponse du serveur
            String response = in.readLine();
            System.out.println("Poids du camion : " + response);
          
         // Enregistrer les données dans la base de données
               saveDataToDatabase(response);

            // Fermer les flux et la connexion
            out.close();
            in.close();
            sslSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void saveDataToDatabase(String data) {
        // Remplacez les informations de la base de données par les vôtres
        String jdbcUrl = "jdbc:mysql://172.30.2.206:3306/smart_poubelle";
        String username = "user";
        String password = "pwduser";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Utilisez une requête préparée pour éviter les attaques par injection SQL
        	 String sql = "INSERT INTO Camion (Plaque, Poids_vide) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            	// Générer un identifiant unique pour chaque enregistrement
                int uniqueID = UUID.randomUUID().hashCode();
                preparedStatement.setInt(1, uniqueID); // ID 
                
             // Convertir la chaîne de caractères en float
                float poidsVide = Float.parseFloat(data);
                preparedStatement.setFloat(2, poidsVide); // Poids_depart
                preparedStatement.executeUpdate();
                System.out.println("Data saved to database");
            } catch (NumberFormatException e) {
                System.err.println("Invalid format for weight: " + data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static double getTruckWeight() {
		// TODO Auto-generated method stub
		return 0;
	}

}






















