package ma.emsi.reddad;

import dev.langchain4j.agent.tool.Tool;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MeteoTool {
    @Tool("Donne moi la météo actuelle d'une ville en utilisant wttr.in")
    public String meteo(String ville) {
        try {
            String url = "https://wttr.in/" + ville + "?format=3";
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                for (String line; (line = in.readLine()) != null; ) sb.append(line);
                return sb.toString();
            }
        } catch (Exception e) {
            return "Impossible de récupérer la météo pour " + ville + " (" + e.getMessage() + ")";
        }
    }
}
