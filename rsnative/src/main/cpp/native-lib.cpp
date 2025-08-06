#include <jni.h>
#include <string>
#include <librealsense2/rs.hpp>

static rs2::pipeline pipeline;
static bool is_streaming = false;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_hello(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("Hello from native code");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_startPreview(
        JNIEnv*, jobject) {
    try {
        if (!is_streaming) {
            pipeline.start();
            is_streaming = true;
        }
        return JNI_TRUE;
    } catch (const rs2::error&) {
        is_streaming = false;
        return JNI_FALSE;
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_stopPreview(
        JNIEnv*, jobject) {
    if (is_streaming) {
        pipeline.stop();
        is_streaming = false;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_getRgbFrame(
        JNIEnv* env,
        jobject) {
    if (!is_streaming) return nullptr;
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
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_getDepthFrame(
        JNIEnv* env,
        jobject) {
    if (!is_streaming) return nullptr;
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
}
