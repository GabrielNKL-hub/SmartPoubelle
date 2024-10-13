package bdd_test2;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSslUtil {
	 public void connect() {
		 //parametres p = parametres.recupere();
	        MqttConnectOptions conOpt = new MqttConnectOptions();
	        conOpt.setCleanSession(true);
	        String port = "1883";
	        String protocole = "tcp";
	        //frmTestDeConfiguration.setTitle("Test de configuration du serveur TTN : mode sécurisé via TLS inactif");

	        /*if (p.isTls()) { // Si TLS est activé
	            port = "1883"; // Port par défaut pour MQTT TLS
	            protocole = "ssl"; // Protocole SSL

	            try {
	                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
	                sslContext.init(null, null, new SecureRandom());
	                SSLSocketFactory socketFactory = sslContext.getSocketFactory();
	                conOpt.setSocketFactory(socketFactory);
	            } catch (NoSuchAlgorithmException | KeyManagementException e) {
	                e.printStackTrace();
	            }
	        }*/

	        try {
	        	MqttClient client = new MqttClient("tcp://eu1.cloud.thethings.network:1883", "ihmtest", new MemoryPersistence());
	            client.setCallback((MqttCallback) this);
	            client.connect(conOpt);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	

}
