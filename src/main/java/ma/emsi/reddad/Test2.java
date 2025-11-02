package ma.emsi.reddad;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

public class Test2 {

    public static void main(String[] args) {
        Test2 programme = new Test2();
        programme.executer();
    }

    // Méthode principale qui orchestre les étapes
    private void executer() {
        ChatModel modele = creerModele();
        String texte = "Bonjour, je suis boxeur et j’aime me bagarre.";

        Prompt prompt = creerPrompt(texte);
        String traduction = obtenirTraduction(modele, prompt);

        afficherResultat(texte, traduction);
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

    // Création du prompt à partir d’un texte
    private Prompt creerPrompt(String texte) {
        PromptTemplate template = PromptTemplate.from(
                "Traduis le texte en anglais : {{texte}}"
        );

        Map<String, Object> variables = Map.of("texte", texte);
        return template.apply(variables);
    }

    // Envoi du prompt au modèle et récupération de la réponse
    private String obtenirTraduction(ChatModel modele, Prompt prompt) {
        return modele.chat(prompt.text());
    }

    // Affichage des résultats
    private void afficherResultat(String original, String traduction) {
        System.out.println("Texte original : " + original);
        System.out.println("Traduction : " + traduction);
    }
}
