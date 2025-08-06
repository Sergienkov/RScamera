#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_realsensecapture_rsnative_NativeBridge_hello(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("Hello from native code");
}
