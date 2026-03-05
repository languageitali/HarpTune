cmake_minimum_required(VERSION 3.22.1)

project("harptune-native")

# 1. Oboe via Prefab (Funciona correctamente)
find_package(oboe REQUIRED CONFIG)

# 2. Configuración manual de TensorFlow Lite
# NOTA: Debes colocar los headers de TFLite en src/main/cpp/include
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

# Definir la librería importada de TFLite
add_library(lib_tflite SHARED IMPORTED)
set_target_properties(lib_tflite PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libtensorflowlite.so)

# 3. Definición de la librería del proyecto
add_library(harptune-native SHARED
        native-lib.cpp
        AudioEngine.cpp
        FastYin.cpp)

# 4. Estándares y Optimización de Baja Latencia
target_compile_features(harptune-native PRIVATE cxx_std_20)

target_compile_options(harptune-native PRIVATE
        -O3
        -ffast-math
        -fno-rtti
        -fno-exceptions
        -Wall)

# 5. Enlace de targets
find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(harptune-native
        oboe::oboe
        lib_tflite # Enlace manual definido arriba
        ${log-lib}
        ${android-lib})