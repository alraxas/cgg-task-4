package com.cgvsu.render_engine.rendering;

public enum RenderMode {
    SOLID,                  // Только сплошная заливка
    WIREFRAME,              // Только каркас
    TEXTURED,               // Только текстура
    LIT_SOLID,              // Освещение + сплошная заливка
    LIT_TEXTURED,           // Освещение + текстура
    WIREFRAME_LIT_SOLID,    // Каркас + освещение + сплошная заливка
    WIREFRAME_TEXTURED,     // Каркас + текстура
    ALL                     // Все включено (каркас + текстура + освещение)
}
