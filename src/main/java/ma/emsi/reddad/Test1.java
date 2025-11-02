package ma.emsi.reddad;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

public class Test1 {

    // Méthode principale
    public static void main(String[] args) {
        Test1 programme = new Test1();
        programme.executer();
    }

    // Méthode pour exécuter la logique principale
    private void executer() {
        ChatModel modele = creerModele();
        poserQuestion(modele);
    }

    // Création et configuration du modèle Gemini
    private ChatModel creerModele() {
        String cleApi = System.getenv("GEMINI_KEY");

        if (cleApi == null || cleApi.isEmpty()) {
            throw new IllegalStateException("La clé API GEMINI_KEY n’est pas définie dans les variables d’environnement.");
        }

        return GoogleAiGeminiChatModel.builder()
                .apiKey(cleApi)
                .modelName("gemini-2.5-flash")
                .temperature(0.9)
                .build();
    }

    // Envoi d’une question et affichage de la réponse
    private void poserQuestion(ChatModel modele) {
        String question = "Quelle est la capitale du Senegal?";
        String reponse = modele.chat(question);

        afficherResultat(question, reponse);
    }

    // Méthode d’affichage
    private void afficherResultat(String question, String reponse) {
        System.out.println("User : " + question);
        System.out.println("LLM: " + reponse);
    }
}
