package ma.emsi.reddad;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.Objects;

public class Test4 {

    // === Contrat du service ===
    interface Assistant {
        String chat(String userMessage);
    }

    // === Config centralisée ===
    private static final class Config {
        static final String ENV_KEY = "GEMINI_KEY";
        static final String DEFAULT_MODEL = "gemini-2.5-flash";
        static final String DEFAULT_DOC = "infos.txt";
        static final String DEFAULT_QUESTION = "Comment s'appel le chat de pierre?";
        static final double TEMPERATURE = 0.1;
        static final int MEMORY_WINDOW = 10;

        private Config() {}
    }

    public static void main(String[] args) {
        // 1) Paramètres (avec valeurs par défaut)
        String docPath = (args != null && args.length > 0 && !args[0].isBlank())
                ? args[0] : Config.DEFAULT_DOC;

        String question = (args != null && args.length > 1 && !args[1].isBlank())
                ? args[1] : Config.DEFAULT_QUESTION;

        // 2) Orchestration
        String apiKey = requireApiKey(Config.ENV_KEY);
        ChatModel chatModel = buildChatModel(apiKey, Config.DEFAULT_MODEL, Config.TEMPERATURE);
        EmbeddingModel embeddingModel = buildEmbeddingModel(apiKey);
        EmbeddingStore<TextSegment> store = buildEmbeddingStoreFromFile(docPath, embeddingModel);
        Assistant assistant = buildAssistant(chatModel, embeddingModel, store, Config.MEMORY_WINDOW);

        // 3) Exécution
        String reponse = assistant.chat(question);
        System.out.println(reponse);
    }

    // === Étapes de construction ===

    private static String requireApiKey(String envVar) {
        String key = System.getenv(envVar);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("La variable d'environnement '" + envVar + "' est absente ou vide.");
        }
        return key;
    }

    private static ChatModel buildChatModel(String apiKey, String modelName, double temperature) {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(Objects.requireNonNull(apiKey))
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    private static EmbeddingModel buildEmbeddingModel(String apiKey) {
        // Embeddings Google pour éviter les dépendances natives DJL/ONNX
        return GoogleAiEmbeddingModel.builder()
                .apiKey(Objects.requireNonNull(apiKey))
                .modelName("text-embedding-004")
                .build();
    }

    private static EmbeddingStore<TextSegment> buildEmbeddingStoreFromFile(String filePath,
                                                                           EmbeddingModel embeddingModel) {
        Document doc = FileSystemDocumentLoader.loadDocument(filePath);
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

        // On fixe explicitement l'embeddingModel dans l'ingestor
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build();

        ingestor.ingest(doc);
        return store;
    }

    private static Assistant buildAssistant(ChatModel chatModel,
                                            EmbeddingModel embeddingModel,
                                            EmbeddingStore<TextSegment> store,
                                            int memoryWindow) {
        // ✅ IMPORTANT : fournir embeddingModel au ContentRetriever
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .maxResults(5)        // optionnel
                .minScore(0.2)        // optionnel
                .build();

        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(memoryWindow))
                .contentRetriever(retriever)
                .build();
    }
}
