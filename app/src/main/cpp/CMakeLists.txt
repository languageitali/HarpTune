cmake_minimum_required(VERSION 3.22.1)

project("harptune-native")

# 1. Localizar Oboe mediante la infraestructura Prefab de Android
find_package(oboe REQUIRED CONFIG)

# 2. Definición de la librería nativa
add_library(harptune-native SHARED
        native-lib.cpp)

# 3. Configuración de estándares y optimizaciones de compilación (2026)
target_compile_features(harptune-native PRIVATE cxx_std_23)

target_compile_options(harptune-native PRIVATE
        -O3
        -ffast-math
        -fno-rtti
        -fno-exceptions
        -Wall
        -Werror)

# 4. Inclusión de librerías del sistema y dependencias de Oboe
find_library(log-lib log)
find_library(android-lib android)

# 5. Enlace de targets
# oboe::oboe inyecta automáticamente las cabeceras necesarias
target_link_libraries(harptune-native
        oboe::oboe
        ${log-lib}
        ${android-lib})