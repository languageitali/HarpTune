cmake_minimum_required(VERSION 3.22.1)

project("harptune-native")

# 1. Localizar dependencias mediante Prefab
# Oboe usa su propio nombre de paquete
find_package(oboe REQUIRED CONFIG)

# TensorFlow Lite Prefab usualmente exporta como 'tensorflowlite' o 'tensorflow-lite'
# Intentamos primero con 'tensorflowlite' que es el estándar moderno
find_package(tensorflowlite REQUIRED CONFIG)

# 2. Definición de la librería nativa
add_library(harptune-native SHARED
        native-lib.cpp
        AudioEngine.cpp
        FastYin.cpp)

# 3. Configuración de estándares y optimizaciones
target_compile_features(harptune-native PRIVATE cxx_std_20)

target_compile_options(harptune-native PRIVATE
        -O3
        -ffast-math
        -fno-rtti
        -fno-exceptions
        -Wall)

# 4. Inclusión de librerías del sistema
find_library(log-lib log)
find_library(android-lib android)

# 5. Enlace de targets
target_link_libraries(harptune-native
        oboe::oboe
        tensorflow::tensorflowlite
        ${log-lib}
        ${android-lib})
