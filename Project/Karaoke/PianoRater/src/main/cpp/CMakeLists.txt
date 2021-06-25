cmake_minimum_required(VERSION 3.4.1)

set(PROJECT_NAME "PianoRater")
project(${PROJECT_NAME})
FILE(GLOB SRCS "*.cpp" "*.c")
FILE(GLOB HEADERS "*.h" "*.hpp")
add_definitions (-Wno-format-security)
add_library(${PROJECT_NAME} SHARED ${SRCS} ${HEADERS})
target_include_directories(${PROJECT_NAME} PUBLIC "./")


# build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -Ofast")



target_link_libraries(PianoRater
        android
        log)
