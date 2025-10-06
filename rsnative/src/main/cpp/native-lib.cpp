#include <jni.h>
#include <string>
#include <chrono>
#include <cstdio>
#include <cstring>
#include <librealsense2/rs.hpp>
#include <vector>
#include <algorithm>
#include <fstream>
#include <cmath>

#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "stb_image_write.h"

static bool is_streaming = false;

#ifndef RSCAMERA_STUB_REALSENSE
static rs2::pipeline pipeline;
#else
namespace {
constexpr int kColorWidth = 640;
constexpr int kColorHeight = 480;
constexpr int kDepthWidth = 848;
constexpr int kDepthHeight = 480;
constexpr int kColorChannels = 3;

struct StubFrames {
    std::vector<uint8_t> color;
    std::vector<uint16_t> depth;
};

static int stub_frame_counter = 0;

void reset_stub_counter() {
    stub_frame_counter = 0;
}

StubFrames generate_stub_frames() {
    StubFrames frames;
    frames.color.resize(kColorWidth * kColorHeight * kColorChannels);
    frames.depth.resize(kDepthWidth * kDepthHeight);

    const int frame = stub_frame_counter++;
    for (int y = 0; y < kColorHeight; ++y) {
        for (int x = 0; x < kColorWidth; ++x) {
            size_t idx = (y * kColorWidth + x) * kColorChannels;
            uint8_t r = static_cast<uint8_t>((x + frame * 5) % 256);
            uint8_t g = static_cast<uint8_t>((y + frame * 3) % 256);
            uint8_t b = static_cast<uint8_t>(((x + y) / 2 + frame * 7) % 256);
            frames.color[idx] = r;
            frames.color[idx + 1] = g;
            frames.color[idx + 2] = b;
        }
    }

    for (int y = 0; y < kDepthHeight; ++y) {
        for (int x = 0; x < kDepthWidth; ++x) {
            size_t idx = static_cast<size_t>(y) * kDepthWidth + x;
            frames.depth[idx] = static_cast<uint16_t>(((x + frame * 11) % 400) * 160);
        }
    }

    return frames;
}

bool write_stub_bag(const std::string& path) {
    std::ofstream bag(path, std::ios::binary | std::ios::trunc);
    if (!bag.good()) {
        return false;
    }
    bag << "STUB";
    return bag.good();
}

bool write_stub_jpeg(const std::string& path, const std::vector<uint8_t>& pixels) {
    return stbi_write_jpg(path.c_str(), kColorWidth, kColorHeight, kColorChannels,
                          pixels.data(), 90) != 0;
}
}  // namespace
#endif

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_hello(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("Hello from native code");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_startPreview(
        JNIEnv*, jobject) {
#ifndef RSCAMERA_STUB_REALSENSE
    try {
        if (!is_streaming) {
            rs2::config cfg;
            cfg.enable_stream(RS2_STREAM_DEPTH, 848, 480, RS2_FORMAT_Z16, 60);
            cfg.enable_stream(RS2_STREAM_COLOR, 640, 480, RS2_FORMAT_RGB8, 30);
            pipeline.start(cfg);
            is_streaming = true;
        }
        return JNI_TRUE;
    } catch (const rs2::error&) {
        is_streaming = false;
        return JNI_FALSE;
    }
#else
    if (!is_streaming) {
        reset_stub_counter();
        is_streaming = true;
    }
    return JNI_TRUE;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_stopPreview(
        JNIEnv*, jobject) {
    if (!is_streaming) {
        return;
    }
#ifndef RSCAMERA_STUB_REALSENSE
    pipeline.stop();
#endif
    is_streaming = false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_startStreaming(
        JNIEnv* env, jobject thiz) {
    return Java_com_example_realsensecapture_rsnative_NativeBridge_startPreview(env, thiz);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_stopStreaming(
        JNIEnv* env, jobject thiz) {
    Java_com_example_realsensecapture_rsnative_NativeBridge_stopPreview(env, thiz);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_startPlayback(
        JNIEnv* env, jobject, jstring bagPath) {
    const char* pathChars = env->GetStringUTFChars(bagPath, nullptr);
    std::string path(pathChars ? pathChars : "");
    env->ReleaseStringUTFChars(bagPath, pathChars);
#ifndef RSCAMERA_STUB_REALSENSE
    try {
        if (!is_streaming) {
            rs2::config cfg;
            cfg.enable_device_from_file(path.c_str(), false);
            pipeline.start(cfg);
            is_streaming = true;
        }
        return JNI_TRUE;
    } catch (const rs2::error&) {
        is_streaming = false;
        return JNI_FALSE;
    }
#else
    std::ifstream bag(path, std::ios::binary);
    if (!bag.good()) {
        return JNI_FALSE;
    }
    reset_stub_counter();
    is_streaming = true;
    return JNI_TRUE;
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_stopPlayback(
        JNIEnv*, jobject) {
    if (!is_streaming) {
        return;
    }
#ifndef RSCAMERA_STUB_REALSENSE
    pipeline.stop();
#endif
    is_streaming = false;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_getRgbFrame(
        JNIEnv* env,
        jobject) {
    if (!is_streaming) return nullptr;
#ifndef RSCAMERA_STUB_REALSENSE
    try {
        rs2::frameset frames = pipeline.wait_for_frames();
        rs2::video_frame color = frames.get_color_frame();
        int size = color.get_width() * color.get_height() * color.get_bytes_per_pixel();
        jbyteArray result = env->NewByteArray(size);
        env->SetByteArrayRegion(result, 0, size,
                                reinterpret_cast<const jbyte*>(color.get_data()));
        return result;
    } catch (const rs2::error&) {
        return nullptr;
    }
#else
    StubFrames frames = generate_stub_frames();
    const jsize size = static_cast<jsize>(frames.color.size());
    jbyteArray result = env->NewByteArray(size);
    if (!result) {
        return nullptr;
    }
    env->SetByteArrayRegion(result, 0, size,
                            reinterpret_cast<const jbyte*>(frames.color.data()));
    return result;
#endif
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_getDepthFrame(
        JNIEnv* env,
        jobject) {
    if (!is_streaming) return nullptr;
#ifndef RSCAMERA_STUB_REALSENSE
    try {
        rs2::frameset frames = pipeline.wait_for_frames();
        rs2::depth_frame depth = frames.get_depth_frame();
        int size = depth.get_width() * depth.get_height() * depth.get_bytes_per_pixel();
        jbyteArray result = env->NewByteArray(size);
        env->SetByteArrayRegion(result, 0, size,
                                reinterpret_cast<const jbyte*>(depth.get_data()));
        return result;
    } catch (const rs2::error&) {
        return nullptr;
    }
#else
    StubFrames frames = generate_stub_frames();
    const jsize size = static_cast<jsize>(frames.depth.size() * sizeof(uint16_t));
    jbyteArray result = env->NewByteArray(size);
    if (!result) {
        return nullptr;
    }
    env->SetByteArrayRegion(result, 0, size,
                            reinterpret_cast<const jbyte*>(frames.depth.data()));
    return result;
#endif
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_getCombinedFrame(
        JNIEnv* env, jobject) {
    if (!is_streaming) return nullptr;
#ifndef RSCAMERA_STUB_REALSENSE
    try {
        rs2::frameset frames = pipeline.wait_for_frames();
        rs2::align align_to_color(RS2_STREAM_COLOR);
        frames = align_to_color.process(frames);
        rs2::video_frame color = frames.get_color_frame();
        rs2::depth_frame depth = frames.get_depth_frame();

        int width = color.get_width();
        int height = color.get_height();
        int dwidth = depth.get_width();
        int combined_width = width + dwidth;
        std::vector<uint8_t> buffer(combined_width * height * 3);

        const uint8_t* rgb = reinterpret_cast<const uint8_t*>(color.get_data());
        const uint16_t* d = reinterpret_cast<const uint16_t*>(depth.get_data());

        for (int y = 0; y < height; ++y) {
            std::memcpy(buffer.data() + (y * combined_width) * 3,
                        rgb + (y * width) * 3,
                        width * 3);
            for (int x = 0; x < dwidth; ++x) {
                uint16_t depth_val = d[y * dwidth + x];
                uint8_t g = std::min(depth_val / 32, (uint16_t)255);
                size_t idx = (y * combined_width + width + x) * 3;
                buffer[idx] = g;
                buffer[idx + 1] = g;
                buffer[idx + 2] = g;
            }
        }

        jbyteArray result = env->NewByteArray(buffer.size());
        env->SetByteArrayRegion(result, 0, buffer.size(),
                                reinterpret_cast<jbyte*>(buffer.data()));
        return result;
    } catch (const rs2::error&) {
        return nullptr;
    }
#else
    StubFrames frames = generate_stub_frames();
    const int combined_width = kColorWidth + kDepthWidth;
    std::vector<uint8_t> buffer(static_cast<size_t>(combined_width) * kColorHeight * kColorChannels);

    for (int y = 0; y < kColorHeight; ++y) {
        std::memcpy(buffer.data() + (static_cast<size_t>(y) * combined_width) * kColorChannels,
                    frames.color.data() + (static_cast<size_t>(y) * kColorWidth) * kColorChannels,
                    static_cast<size_t>(kColorWidth) * kColorChannels);
        for (int x = 0; x < kDepthWidth; ++x) {
            uint16_t depth_val = frames.depth[static_cast<size_t>(y) * kDepthWidth + x];
            uint8_t g = static_cast<uint8_t>(std::min<uint16_t>(depth_val / 32, 255));
            size_t idx = (static_cast<size_t>(y) * combined_width + kColorWidth + x) * kColorChannels;
            buffer[idx] = g;
            buffer[idx + 1] = g;
            buffer[idx + 2] = g;
        }
    }

    jbyteArray result = env->NewByteArray(static_cast<jsize>(buffer.size()));
    if (!result) {
        return nullptr;
    }
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(buffer.size()),
                            reinterpret_cast<const jbyte*>(buffer.data()));
    return result;
#endif
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_recordBurst(
        JNIEnv* env,
        jobject,
        jstring dirPath) {
    const char* pathChars = env->GetStringUTFChars(dirPath, nullptr);
    std::string directory(pathChars ? pathChars : "");
    env->ReleaseStringUTFChars(dirPath, pathChars);

#ifndef RSCAMERA_STUB_REALSENSE
    try {
        rs2::config cfg;
        std::string bagPath = directory + "/depth_0.1s.bag";
        cfg.enable_stream(RS2_STREAM_DEPTH, 848, 480, RS2_FORMAT_Z16, 60);
        cfg.enable_stream(RS2_STREAM_COLOR, 640, 480, RS2_FORMAT_RGB8, 30);
        cfg.enable_record_to_file(bagPath);
        rs2::pipeline p;
        p.start(cfg);

        auto start = std::chrono::steady_clock::now();
        int rgbCount = 0;
        while (std::chrono::duration_cast<std::chrono::milliseconds>(
                   std::chrono::steady_clock::now() - start)
                   .count() < 100) {
            rs2::frameset frames = p.wait_for_frames();
            rs2::video_frame color = frames.get_color_frame();
            int width = color.get_width();
            int height = color.get_height();
            int channels = color.get_bytes_per_pixel();
            const unsigned char* data =
                    reinterpret_cast<const unsigned char*>(color.get_data());
            char filename[512];
            std::snprintf(filename, sizeof(filename), "%s/rgb_%03d.jpg",
                          directory.c_str(), rgbCount);
            stbi_write_jpg(filename, width, height, channels, data, 90);
            rgbCount++;
        }
        p.stop();

        if (rgbCount < 3) {
            for (int i = 0; i < rgbCount; ++i) {
                char file[512];
                std::snprintf(file, sizeof(file), "%s/rgb_%03d.jpg",
                              directory.c_str(), i);
                std::remove(file);
            }
            std::remove(bagPath.c_str());
            return JNI_FALSE;
        }
        return JNI_TRUE;
    } catch (const rs2::error&) {
        return JNI_FALSE;
    }
#else
    const std::string bagPath = directory + "/depth_0.1s.bag";
    if (!write_stub_bag(bagPath)) {
        return JNI_FALSE;
    }

    std::vector<std::string> writtenFiles;
    writtenFiles.reserve(5);
    const int targetFrames = 5;
    bool ok = true;
    for (int i = 0; i < targetFrames; ++i) {
        StubFrames frames = generate_stub_frames();
        char filename[512];
        std::snprintf(filename, sizeof(filename), "%s/rgb_%03d.jpg",
                      directory.c_str(), i);
        if (!write_stub_jpeg(filename, frames.color)) {
            ok = false;
            break;
        }
        writtenFiles.emplace_back(filename);
    }

    if (!ok || static_cast<int>(writtenFiles.size()) < 3) {
        for (const auto& file : writtenFiles) {
            std::remove(file.c_str());
        }
        std::remove(bagPath.c_str());
        return JNI_FALSE;
    }

    return JNI_TRUE;
#endif
}
