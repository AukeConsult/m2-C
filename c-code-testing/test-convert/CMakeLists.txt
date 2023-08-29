cmake_minimum_required(VERSION 3.26)
project(test_app  
    VERSION 0.1
    DESCRIPTION "An example project with CMake"
    LANGUAGES CXX
)

set(CMAKE_CXX_STANDARD 20)

add_library(sys lib/system.cpp lib/system.h)

add_executable(test_app main.cpp
        tests/test_all.cpp
        util/byteUtil.h
        util/pack.h
        util/testclasses.h
        util/byteUtil.cpp
        messages/messageAttribute.hpp
        messages/messageHeader.hpp   
 )

target_link_libraries(test_app PRIVATE sys)