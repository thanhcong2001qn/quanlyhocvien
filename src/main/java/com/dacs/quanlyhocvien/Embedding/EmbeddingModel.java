package com.dacs.quanlyhocvien.Embedding;

import java.util.List;

public interface EmbeddingModel {
    List<Float> getEmbedding(String text) throws Exception;
}
