cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_STANDARD 99)

file(GLOB_RECURSE sources_ggwave "../../native/ggwave/*.cpp" "../../native/test-cpp.cpp")

add_library(ggwave SHARED ${sources} ${sources_ggwave})

target_link_libraries(ggwave -llog)