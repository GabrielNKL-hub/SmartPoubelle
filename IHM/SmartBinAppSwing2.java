package bdd_test2;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONObject;

public class SmartBinAppSwing2 {

	private JFrame frame;
	private JLabel tauxRemplissageLabel;
    private DefaultCategoryDataset poidsDataset;
    private DefaultCategoryDataset[] remplissageDatasets = new DefaultCategoryDataset[3];
    private final int limiteCapacite = 80;
    private int quantité;

    private JLabel niveauRemplissageConteneur1;
    private JLabel niveauRemplissageConteneur2;
    private JLabel niveauRemplissageConteneur3;

    private String conteneur1Id = "eui-70b3d57ed0065e91";
    private String conteneur2Id = "eui-70b3d57ed0067e21";
    private String conteneur3Id = "eui-70b3d57ed0069e31";
	private JLabel conteneur1Label;
	private JLabel conteneur2Label;
	private JLabel conteneur3Label;
	
	JProgressBar pb1;
	JProgressBar pb2;
	JProgressBar pb3;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SmartBinAppSwing2 window = new SmartBinAppSwing2();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SmartBinAppSwing2() {
		initialize();
		initialiseMqtt();
	}
	//********initialise matt
	public void initialiseMqtt() {
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
                System.out.println("fin abonnement");
            	
            	
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
                            pb1.setValue((int)niveauRemplissage);
                            pb2.setValue((int)niveauRemplissage);
                            pb3.setValue((int)niveauRemplissage);

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
           /* while (true) {
                Thread.sleep(1000);
            }*/

        } catch (MqttException  e) {
            e.printStackTrace();
        }
        
        
        
	}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("SmartBinAppSwing");
		frame.setBounds(100, 100, 1200, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(756, 545);
		
		JPanel panel = new JPanel();

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);
        
        JLabel lblNewLabel_3 = new JLabel("Taux de remplissage");
        lblNewLabel_3.setBounds(114, 33, 105, 14);
        panel.add(lblNewLabel_3);
        
        JLabel lblNewLabel_4 = new JLabel("Taux de remplissage");
        lblNewLabel_4.setBounds(309, 33, 105, 14);
        panel.add(lblNewLabel_4);
        
        JLabel lblNewLabel_5 = new JLabel("Taux de remplissage");
        lblNewLabel_5.setBounds(499, 33, 105, 14);
        panel.add(lblNewLabel_5);
        
        JPanel panel_1 = new JPanel();
        panel_1.setBounds(39, 68, 226, 259);
        panel.add(panel_1);
        
        JLabel lblNewLabel = new JLabel("eui-70b3d57ed0065e91");
        panel_1.add(lblNewLabel);
        
        pb1 = new JProgressBar();
        pb1.setValue(40);
        panel_1.add(pb1);
        
        JPanel panel_2 = new JPanel();
        panel_2.setBounds(274, 68, 168, 259);
        panel.add(panel_2);
        
        JLabel lblNewLabel_1 = new JLabel("eui-70b3d57ed0067e21");
        panel_2.add(lblNewLabel_1);
        
        pb2 = new JProgressBar();
        pb2.setValue(40);
        panel_2.add(pb2);
        
        JPanel panel_3 = new JPanel();
        panel_3.setBounds(474, 68, 174, 259);
        panel.add(panel_3);
        
        JLabel lblNewLabel_2 = new JLabel("eui-70b3d57ed0069e31");
        panel_3.add(lblNewLabel_2);
        
        pb3 = new JProgressBar();
        pb3.setValue(40);
        panel_3.add(pb3);
        
        frame.setVisible(true);
	}
}
