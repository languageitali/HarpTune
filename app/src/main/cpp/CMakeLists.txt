cmake_minimum_required(VERSION 3.22.1)
project("harptune_engine")

# Cargar Oboe desde Prefab
find_package(oboe REQUIRED CONFIG)

# Definir la librería compartida y sus fuentes
add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp
        AudioEngine.cpp
        FastYin.cpp
)

# Enlazar dependencias nativas (Oboe y Android Log)
target_link_libraries(${CMAKE_PROJECT_NAME}
        oboe::oboe
        log
)