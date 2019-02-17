import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.json.*;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException{

        String parsed_results;

        while (true) {
            parsed_results = parse(callOWM("Rochester"));

            //prints out results in form of "temperature,weather" where temperature is in kelvin and weather is
            //1 if there is precipitation and 0 otherwise.
            System.out.println("\n*****    Parsed Result    *****\n" + parsed_results);

            sendTo(parsed_results);

            //because we don't care about precision or efficiency we put the thread to sleep for 30 seconds
            Thread.sleep(30*1000);
        }
    }

    /**
     * Sends the results to the microcontroller through a byte array that represents our 4 digits.
     * @param data the information to be sent to the microcontroller.
     */
    private static void sendTo(String data) throws IOException{
        byte[] message = data.getBytes();
        Socket socket = new Socket(InetAddress.getByName("ESP8266"), 80);

        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        output.write(message);
    }

    /**
     * Takes a JSON Object from openweathermap, extracts the useful data, and formats a String with how the data will be
     * sent to receiver.
     * @param raw the JSON Object that contains the weather data that will be parsed
     * @return a String in form of "temperature,weather" where temperature is in kelvin and weather is
     *         1 if there is precipitation and 0 otherwise. There is no comma when the data is sent.
     */
    private static String parse(JSONObject raw){

        int temp = (raw.getJSONObject("main").getInt("temp"));

        String raw_weather = raw.getJSONArray("weather").getJSONObject(0).getString("main");

        int weather;
        if(raw_weather.equals("Snow") || raw_weather.equals("Rain") || raw_weather.equals("Extreme"))
            weather = 1;
        else
            weather = 0;

        return "" + temp + weather;
    }

    /**
     * Asks the openweathermap api the weather at the city given and returns the JSON Object the server gives.
     * @param city the city openweathermap checks the weather at
     * @return the JSON Object found at the openweathermap URL response
     */
    private static JSONObject callOWM(String city) throws IOException {

        String APPID = "5ad60f1b59786cce16e7ab189518a4b2"; //API ID that our team received from OWM
        String httpsURL = "http://api.openweathermap.org/data/2.5/weather?q="+city+",us&APPID="+APPID; //request to OWM
        URL url; //request to OWM
        HttpURLConnection conn = null; //Connection to OWM

        url = new URL(httpsURL);
        conn = (HttpURLConnection) url.openConnection();


        if (conn != null) {

            //br reads the content on the page
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            //all the content is one line so just read it once
            String input = br.readLine();
            //close the reader
            br.close();

            System.out.println("***** Initial JSON Object *****\n" + input);
            return new JSONObject(input);

        }
        return new JSONObject("Empty page");
    }
}
