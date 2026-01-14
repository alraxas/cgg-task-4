package com.cgvsu.render_engine.rendering;

public enum RenderMode {
    WIREFRAME, // Только каркас
    SOLID, // Только заливка
    TEXTURED, // Только текстура
    LIT, // Только освещение
    WIREFRAME_SOLID, // Каркас + заливка
    TEXTURED_LIT, // Текстура + освещение
    ALL // Все режимы (текстура + освещение + каркас)
}
