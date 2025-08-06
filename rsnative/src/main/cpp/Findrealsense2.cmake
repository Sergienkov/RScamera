find_path(realsense2_INCLUDE_DIR librealsense2/rs.hpp
          HINTS ${CMAKE_CURRENT_LIST_DIR}/realsense2/include)

add_library(realsense2::realsense2 INTERFACE)
set_target_properties(realsense2::realsense2 PROPERTIES
    INTERFACE_INCLUDE_DIRECTORIES "${realsense2_INCLUDE_DIR}"
)
