package ma.emsi.reddad;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.Objects;
import java.util.Scanner;

public class Test5 {

    // === Configuration centralisée ===
    private static final String ENV_API_KEY          = "GEMINI_KEY";
    private static final String MODEL_NAME           = "gemini-2.5-flash";
    private static final double MODEL_TEMPERATURE    = 0.1;
    private static final String DOC_PATH             = "ml.pdf";
    private static final int    CHAT_MEMORY_MESSAGES = 20;

    // === Contrat de l'assistant ===
    interface Assistant {
        String chat(String userMessage);
    }

    // === Point d'entrée ===
    public static void main(String[] args) {
        try {
            ChatModel model           = buildModel();
            Document document         = loadDocument(DOC_PATH);
            EmbeddingStore<TextSegment> store = buildAndFillStore(document);
            Assistant assistant       = buildAssistant(model, store, CHAT_MEMORY_MESSAGES);
            runConsoleLoop(assistant);
        } catch (Exception e) {
            System.err.println("Erreur fatale : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Construction du modèle ---
    private static ChatModel buildModel() {
        String apiKey = System.getenv(ENV_API_KEY);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable d'environnement " + ENV_API_KEY + " est absente ou vide."
            );
        }

        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MODEL_NAME)
                .temperature(MODEL_TEMPERATURE)
                .build();
    }

    // --- Chargement du document ---
    private static Document loadDocument(String path) {
        Objects.requireNonNull(path, "Le chemin du document ne peut pas être null.");
        return FileSystemDocumentLoader.loadDocument(path);
    }

    // --- Vector store + ingestion ---
    private static EmbeddingStore<TextSegment> buildAndFillStore(Document document) {
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        // Conserve l’API utilisée dans ton code d’origine
        EmbeddingStoreIngestor.ingest(document, store);
        return store;
    }

    // --- Assemblage de l'assistant avec RAG + mémoire ---
    private static Assistant buildAssistant(ChatModel model,
                                            EmbeddingStore<TextSegment> store,
                                            int memoryMaxMessages) {
        return AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(memoryMaxMessages))
                .contentRetriever(EmbeddingStoreContentRetriever.from(store))
                .build();
    }

    // --- Boucle REPL console ---
    private static void runConsoleLoop(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.println("Posez votre question : ");
                String question = scanner.nextLine();

                if (question == null || question.isBlank()) {
                    continue;
                }
                if ("fin".equalsIgnoreCase(question.trim())) {
                    System.out.println("Assistant : Au revoir !");
                    break;
                }

                System.out.println("==================================================");
                String reponse = safeChat(assistant, question);
                System.out.println("Assistant : " + reponse);
            }
        }
    }

    // --- Garde-fou pour éviter de casser la boucle en cas d'erreur modèle ---
    private static String safeChat(Assistant assistant, String question) {
        try {
            return assistant.chat(question);
        } catch (Exception e) {
            return "Désolé, une erreur est survenue : " + e.getMessage();
        }
    }
}

