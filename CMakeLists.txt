
cmake_minimum_required(VERSION 3.21)

project(CinemaSurroundPlayer VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_AUTOMOC ON)

find_package(Qt6 6.5 REQUIRED COMPONENTS
    Core
    Widgets
    Multimedia
    MultimediaWidgets
)

qt_standard_project_setup()

qt_add_executable(CinemaSurroundPlayer
    src/main.cpp
    src/MainWindow.h
    src/MainWindow.cpp
)

target_link_libraries(CinemaSurroundPlayer PRIVATE
    Qt6::Core
    Qt6::Widgets
    Qt6::Multimedia
    Qt6::MultimediaWidgets
)

if(WIN32)
    set_target_properties(CinemaSurroundPlayer PROPERTIES
        WIN32_EXECUTABLE TRUE
    )
endif()
