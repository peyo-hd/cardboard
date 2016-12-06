#include <jni.h>

static JavaVM *gVM;

static struct {
    jmethodID notifyRiftEventAcc;
    jmethodID notifyRiftEventGyro;
} gRiftManagerClassInfo;

static void checkAndClearExceptionFromCallback(JNIEnv* env, const char* methodName __unused) {
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }
}

static JNIEnv *sEnv;
static jobject sRiftManagerObj;

extern "C" {

void onRiftEventAcc(long timestamp, float x, float y, float z) {
    sEnv->CallVoidMethod(sRiftManagerObj,
            gRiftManagerClassInfo.notifyRiftEventAcc,
            timestamp, x, y, z);
    checkAndClearExceptionFromCallback(sEnv, "notifyRiftEventAcc");
}

void onRiftEventGyro(long timestamp, float x, float y, float z) {
    sEnv->CallVoidMethod(sRiftManagerObj,
            gRiftManagerClassInfo.notifyRiftEventGyro,
            timestamp, x, y, z);
    checkAndClearExceptionFromCallback(sEnv, "notifyRiftEventGyro");
}

#include <cardboard-openhmd.h>

}

// ----------------------------------------------------------------------------

static void nativeOpen(JNIEnv*, jobject) {
    rift_open(onRiftEventAcc, onRiftEventGyro);
}

static void nativeUpdate(JNIEnv* env, jobject riftMangerObj) {
	sEnv = env;
	sRiftManagerObj = riftMangerObj;
	rift_update();
}

static void nativeClose(JNIEnv*, jobject) {
    rift_close();
}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
    {"nativeOpen",   "()V", (void*)nativeOpen},
    {"nativeUpdate", "()V", (void*)nativeUpdate},
    {"nativeClose",  "()V", (void*)nativeClose},
};

jint JNI_OnLoad(JavaVM* vm, void*)
{
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
	gVM = vm;

    jclass clazz = env->FindClass("com/google/vr/sdk/base/sensors/RiftManager");
    env->RegisterNatives(clazz, gMethods, sizeof(gMethods)/sizeof(gMethods[0]));

    gRiftManagerClassInfo.notifyRiftEventAcc = env->GetMethodID(
            clazz, "notifyRiftEventAcc", "(JFFF)V");
    gRiftManagerClassInfo.notifyRiftEventGyro = env->GetMethodID(
            clazz, "notifyRiftEventGyro", "(JFFF)V");

    return JNI_VERSION_1_6;
}
