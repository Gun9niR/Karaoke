
#
# Copyright (C) The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.4.1)


set(PROJECT_NAME "WORLD")
project(${PROJECT_NAME})
FILE(GLOB SRCS "*.cpp" "*.c" "tools/*.cpp" "tools/*.c")
FILE(GLOB HEADERS "world/*.h" "world/*.hpp" "tools/*.h" "tools/*.hpp")
add_definitions (-Wno-format-security)
add_library(${PROJECT_NAME} SHARED ${SRCS} ${HEADERS})
target_include_directories(${PROJECT_NAME} PUBLIC "./")


# build application's shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")



target_link_libraries(WORLD
                      android
                      log)
