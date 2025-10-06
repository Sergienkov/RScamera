set(_realsense_header_root "${CMAKE_CURRENT_LIST_DIR}/realsense2/include")
set(_realsense_header "${_realsense_header_root}/librealsense2/rs.hpp")

if(EXISTS "${_realsense_header}")
    set(realsense2_INCLUDE_DIR "${_realsense_header_root}")
else()
    message(FATAL_ERROR "librealsense2 headers were not found at ${_realsense_header_root}")
endif()

set(_realsense_library_dir "${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}")
set(_realsense_library "${_realsense_library_dir}/librealsense2.so")

set(_realsense_library_size 0)
if(EXISTS "${_realsense_library}")
    file(SIZE "${_realsense_library}" _realsense_library_size)
endif()

if(_realsense_library_size GREATER 0)
    add_library(realsense2::realsense2 SHARED IMPORTED)
    set_target_properties(realsense2::realsense2 PROPERTIES
        IMPORTED_LOCATION "${_realsense_library}"
        INTERFACE_INCLUDE_DIRECTORIES "${realsense2_INCLUDE_DIR}"
    )
    set(realsense2_FOUND TRUE)
else()
    add_library(realsense2::realsense2 INTERFACE IMPORTED)
    set_target_properties(realsense2::realsense2 PROPERTIES
        INTERFACE_INCLUDE_DIRECTORIES "${realsense2_INCLUDE_DIR}"
        INTERFACE_COMPILE_DEFINITIONS "RSCAMERA_STUB_REALSENSE=1"
    )
    set(realsense2_FOUND TRUE)
    message(STATUS "librealsense2 binary not packaged for ${ANDROID_ABI}; building with stub implementation")
endif()
