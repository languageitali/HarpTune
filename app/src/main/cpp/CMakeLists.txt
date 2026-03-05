cmake_minimum_required(VERSION 3.22.1)

project("harptune-native")

# 1. Localizar Oboe mediante la infraestructura Prefab de Android
find_package(oboe REQUIRED CONFIG)

# 2. Definición de la librería nativa
add_library(harptune-native SHARED
        native-lib.cpp
        AudioEngine.cpp
        FastYin.cpp)

# 3. Configuración de estándares y optimizaciones de compilación
# Usamos C++20 para mejor compatibilidad con herramientas de análisis de Android
target_compile_features(harptune-native PRIVATE cxx_std_20)

target_compile_options(harptune-native PRIVATE
        -O3
        -ffast-math
        -fno-rtti
        -fno-exceptions
        -Wall)

# 4. Inclusión de librerías del sistema y dependencias de Oboe
find_library(log-lib log)
find_library(android-lib android)

# 5. Enlace de targets
target_link_libraries(harptune-native
        oboe::oboe
        ${log-lib}
        ${android-lib})
