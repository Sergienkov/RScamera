cmake_minimum_required(VERSION 3.22.1)
project("rsnative")

set(CMAKE_CXX_STANDARD 14)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}")

add_library(rsnative-lib SHARED native-lib.cpp)

find_library(log-lib log)
find_package(realsense2 REQUIRED)

target_link_libraries(
    rsnative-lib
    realsense2::realsense2
    ${log-lib}
)
