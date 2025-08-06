#pragma once
#include <cstdint>
#define RS2_STREAM_DEPTH 0
#define RS2_STREAM_COLOR 1
#define RS2_FORMAT_Z16 0
#define RS2_FORMAT_RGB8 1
namespace rs2 {
struct config {
    void enable_stream(int, int, int, int, int) {}
    void enable_stream(int, int, int, int, int, int) {}
    void enable_device_from_file(const char*, bool) {}
    void enable_record_to_file(const char*) {}
};
struct video_frame {
    int get_width() const { return 0; }
    int get_height() const { return 0; }
    int get_bytes_per_pixel() const { return 0; }
    const void* get_data() const { return nullptr; }
};
struct depth_frame : video_frame {};
struct frameset {
    video_frame get_color_frame() const { return video_frame{}; }
    depth_frame get_depth_frame() const { return depth_frame{}; }
};
struct pipeline {
    pipeline() {}
    template<typename... Args>
    void start(Args&&...) {}
    frameset wait_for_frames() { return frameset{}; }
    void stop() {}
};
class error: public std::exception {
    const char* what() const noexcept override { return "stub"; }
};
}
