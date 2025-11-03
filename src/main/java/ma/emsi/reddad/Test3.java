package ma.emsi.reddad;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Test3 {

    // ==== Constantes de config ====
    private static final String ENV_KEY = "GEMINI_KEY";
    private static final String MODEL_NAME = "text-embedding-004";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    public static void main(String[] args) {
        new Runner().run();
    }

    // ==== Orchestrateur ====
    private static class Runner {
        void run() {
            // 1) Clé API
            String apiKey = System.getenv(ENV_KEY);
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("La variable d'environnement '" + ENV_KEY + "' est absente ou vide.");
            }

            // 2) Modèle
            EmbeddingModel model = buildModel(apiKey);

            // 3) Phrases
            String phrase1 = "J'adore la boxe";
            String phrase2 = "L'intelligence artificielle est vraiment incroyable";
            String phrase3 = "Les planetes tournes autour du soleil a l'inverse des aiguilles d'une montre";
            String phrase4 = "l'Antarctic est le desert le plus grand au monde";

            // 4) Embeddings
            List<Embedding> embeddings = EmbeddingUtils.embedAll(
                    model,
                    phrase1, phrase2, phrase3, phrase4
            );

            Embedding emb1 = embeddings.get(0);
            Embedding emb2 = embeddings.get(1);
            Embedding emb3 = embeddings.get(2);
            Embedding emb4 = embeddings.get(3);

            // 5) Similarités cosinus
            double sim12 = MathUtils.cosineSimilarity(emb1.vector(), emb2.vector());
            double sim34 = MathUtils.cosineSimilarity(emb3.vector(), emb4.vector());

            // 6) Résultats
            System.out.println("Similarité phrase1 / phrase2 (proches) : " + sim12);
            System.out.println("Similarité phrase3 / phrase4 (éloignées) : " + sim34);
        }
    }

    // ==== Construction du modèle ====
    private static EmbeddingModel buildModel(String apiKey) {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(Objects.requireNonNull(apiKey))
                .modelName(MODEL_NAME)
                .timeout(TIMEOUT)
                .build();
    }

    // ==== Utilitaires ====
    private static class EmbeddingUtils {
        static List<Embedding> embedAll(EmbeddingModel model, String... texts) {
            return Arrays.stream(texts)
                    .map(t -> model.embed(t).content())
                    .toList();
        }
    }

    private static class MathUtils {
        static double cosineSimilarity(float[] v1, float[] v2) {
            double dot = 0, norm1 = 0, norm2 = 0;
            int len = Math.min(v1.length, v2.length);
            for (int i = 0; i < len; i++) {
                dot += v1[i] * v2[i];
                norm1 += v1[i] * v1[i];
                norm2 += v2[i] * v2[i];
            }
            return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
        }
    }
}

