package bdd_test2;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SecureMQTTClient {
    public static void main(String[] args) {
        String broker = "tcp://eu1.cloud.thethings.network:1883";
        String clientId = "JavaSecureMqttClient";
        String topic = "v3/smartpoubelle@ttn/devices/eui-70b3d57ed0065e91/up";
        String topic2 = "v3/smartpoubelle@ttn/devices/eui-70b3d57ed0067e21/up";
        String username = "smartpoubelle@ttn";
        String password = "NNSXS.MD24XGMYWPLIMISYFU3UQULONXU43646F4S4OVQ.VA6M4YL3GNADQ2LWXWZWKOYXYXYONRGILOBVPZV2RJ6W2ZUEX6IA";
        
        // Remplacez les informations de la base de données par les vôtres
        String jdbcUrl = "jdbc:mysql://172.30.2.206:3306/smart_poubelle";
        String jdbcUsername = "user";
        String jdbcPassword = "pwduser";

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient client = new MqttClient(broker, clientId, persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());

            System.out.println("Connexion au broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connecté");
/*
            System.out.println("Abonnement au topic: " + topic);
            client.subscribe(topic);
            System.out.println("Abonnement au topic: " + topic2);
            client.subscribe(topic2);
*/
            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword)) {
            	
            	String verifSQL ="Select id from Conteneurs where actif = 1";
            	    PreparedStatement pstmt = conn.prepareStatement(verifSQL);
            		ResultSet res = pstmt.executeQuery(verifSQL);
                    while(res.next()) {
                    String topicVar = "v3/smartpoubelle@ttn/devices/"+res.getString("id")+"/up";
                    System.out.println("Abonnement au topic: " + res.getString("id"));
                    client.subscribe(topicVar);
                    }
                
            	
            	
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            
            
            
            
            
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Connexion perdue");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    System.out.println("Message reçu sur le topic: " + topic + ", Message: " + payload);

                    try {
                        JSONObject racine = new JSONObject(payload);
                        System.out.println("racine:" + racine.toString());

                        String deviceId = racine.getJSONObject("end_device_ids").getString("device_id");
                        if (deviceId == null) {
                            System.out.println("device_id est null");
                            return;
                        }

                        JSONObject uplinkMessage = racine.getJSONObject("uplink_message");
                        /*System.out.println("uplink_message: " + uplinkMessage.toString());*/

                        if (!uplinkMessage.isNull("decoded_payload")) {
                            JSONObject decodedPayload = uplinkMessage.getJSONObject("decoded_payload");

                            float latitude = (float) decodedPayload.getJSONObject("gps_2").getDouble("latitude");
                            float longitude = (float) decodedPayload.getJSONObject("gps_2").getDouble("longitude");
                            float niveauRemplissage = (float) decodedPayload.getDouble("analog_out_1");

                            /*if (niveauRemplissage >= 20) niveauRemplissage = 20;
                            float pourcentageRemplissage = 100 - 5 * niveauRemplissage;*/
                            
                            System.out.println("ID de l'appareil: " + deviceId);
                            System.out.println("Latitude: " + latitude);
                            System.out.println("Longitude: " + longitude);
                            System.out.println("Niveau de remplissage: " + niveauRemplissage);

                            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword)) {
                            	int IdOK = 0;
                            	String verifSQL ="Select count(*) as NB from Conteneurs where ID = '"+deviceId+"'";
                            	try (PreparedStatement pstmt = conn.prepareStatement(verifSQL)) {
                                    //pstmt.setString(1, deviceId);
                            		ResultSet res = pstmt.executeQuery(verifSQL);
                                    res.next();
                                    System.out.println("Nombre reçu: " + res.getInt("NB"));
                                    IdOK = res.getInt("NB");
                                }
                            	if (IdOK == 1) {
                                String insertSQL = "UPDATE Conteneurs set latitude = ?, longitude = ?, taux_de_remplissage = ?, Maj = NOW() WHERE ID =?";
                                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                                	
                                    pstmt.setFloat(1, latitude);
                                    pstmt.setFloat(2, longitude);
                                    pstmt.setFloat(3, niveauRemplissage);
                                    pstmt.setString(4, deviceId);
                                    System.out.println(pstmt);
                                    pstmt.executeUpdate();
                                }
                            	}
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Non utilisé dans cet exemple
                }
            });

            // Maintenir le client actif pour écouter les messages
            while (true) {
                Thread.sleep(1000);
            }

        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }

	/*public static ContainerData[] getContainerData() {
		// TODO Auto-generated method stub
		return null;
	}*/
}