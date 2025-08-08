find_path(
    realsense2_INCLUDE_DIR
    librealsense2/rs.hpp
    HINTS ${CMAKE_CURRENT_LIST_DIR}/realsense2/include
)

find_library(
    realsense2_LIBRARY
    NAMES realsense2 librealsense2
    HINTS ${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}
)

add_library(realsense2::realsense2 SHARED IMPORTED)
set_target_properties(realsense2::realsense2 PROPERTIES
    IMPORTED_LOCATION "${realsense2_LIBRARY}"
    INTERFACE_INCLUDE_DIRECTORIES "${realsense2_INCLUDE_DIR}"
)
