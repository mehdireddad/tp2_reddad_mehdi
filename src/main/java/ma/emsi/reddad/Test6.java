package ma.emsi.reddad;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

import java.util.Scanner;

public class Test6 {

    public static void main(String[] args) {

        // 1) LLM Gemini
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GEMINI_KEY"))
                .modelName("gemini-2.5-flash")
                .temperature(0.1)
                .build();

        // 2) Assistant avec OUTIL + mémoire de chat
        AssistantMeteo assistant = AiServices.builder(AssistantMeteo.class)
                .chatModel(model)
                .tools(new MeteoTool())                             // <-- outil météo branché
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))  // <-- contexte multi-tours
                .build();

        // 3) Boucle Scanner (tu poses toi-même les questions)
        conversationAvec(assistant);
    }

    private static void conversationAvec(AssistantMeteo assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.println("Posez votre question (tapez 'fin' pour quitter) : ");
                String question = scanner.nextLine();
                if (question == null || question.isBlank()) continue;
                if ("fin".equalsIgnoreCase(question.trim())) {
                    System.out.println("Assistant : À la prochaine !");
                    break;
                }
                System.out.println("==================================================");
                String reponse = assistant.chat(question);
                System.out.println("Assistant : " + reponse);
            }
        }
    }
}
