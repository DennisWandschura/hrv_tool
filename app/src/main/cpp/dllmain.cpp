//
// Created by tanne on 28.07.2017.
//
#include <jni.h>
#include "frequencyDomain.h"
#include "Data.h"

Data* DATA{nullptr};

extern "C" JNIEXPORT void JNICALL Java_denwan_hrv_Native_initialize(JNIEnv *env, jobject obj)
{
    if(DATA == nullptr)
        DATA = new Data();
}

extern "C" JNIEXPORT void JNICALL Java_denwan_hrv_Native_shutdown(JNIEnv *env, jobject obj)
{
    if(DATA != nullptr)
    {
        delete(DATA);
        DATA = nullptr;
    }
}

extern "C" JNIEXPORT void JNICALL Java_denwan_hrv_Native_saveData(JNIEnv *env, jobject obj, jstring file)
{
    if(DATA)
    {
        auto n = env->GetStringUTFLength(file);
        auto str = new char[n + 1];
        env->GetStringUTFRegion(file, 0, n, str);
        str[n] = 0;

        DATA->saveToFile(str);

        delete[]str;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_loadData(JNIEnv *env, jobject obj, jstring file)
{
    if(DATA)
    {
        auto n = env->GetStringUTFLength(file);
        auto str = new char[n + 1];
        env->GetStringUTFRegion(file, 0, n, str);
        str[n] = 0;

        int loadCount = DATA->loadFromFile(str);

        delete[]str;

        return loadCount;
    }

    return -1;
}

HRV getHRVData(const float* rr, int n)
{
    auto times = make_array<float>(n);
    times[0] = 0.f;
    for (int i = 1; i < n; ++i)
    {
        times[i] = times[i - 1] + rr[i - 1];
    }

    HRV data;
    memset(&data, 0, sizeof(data));
    timeDomain(rr, n, &data);
    freqDomainHRV(ArrayF((float*)rr, n), times, 1024, 2.0, &data);

    return data;
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_getFirstOfToday(JNIEnv *env, jobject obj, int year, int month, int day)
{
    if(DATA)
    {
        return DATA->getFirstOfToday(year, month, day);
    }

    return -1;
}

extern "C" JNIEXPORT jint JNICALL  Java_denwan_hrv_Native_getEntryCount(JNIEnv *env, jobject obj)
{
    if(DATA) {
        return (int)DATA->sortedEntries.size();
    }
    return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_createNewEntry(JNIEnv *env, jobject obj, jint year, jint month, jint day, jint hour, jint minute, jfloatArray rr_values, jboolean isFirstOfDay)
{
    if(DATA)
    {
        DateTime1 key;
        key.year = year;
        key.month = month;
        key.day = day;

        key.hour = hour;
        key.minute = minute;

        int n = env->GetArrayLength(rr_values);
        std::unique_ptr<float[]> rr(new float[n]);
        env->GetFloatArrayRegion(rr_values, 0, n, rr.get());

        auto hrv = getHRVData(rr.get(), n);
        hrv.isFirstOfDay = isFirstOfDay ? 1 : 0;

        return DATA->addEntry(key, hrv, std::move(rr), n);
    }

    return -2;
}

extern "C" JNIEXPORT void JNICALL Java_denwan_hrv_Native_updateIndices(JNIEnv *env, jobject obj)
{
    DATA->updateIndices();
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getAverageRmssd(JNIEnv *env, jobject obj, jint start_year, jint start_month, jint start_day, jint end_year, jint end_month, jint end_day)
{
    Date1 start, end;
    start.year = start_year;
    start.month = start_month;
    start.day = start_day;

    end.year = end_year;
    end.month = end_month;
    end.day = end_day;

    float avg = 0.0f;
    int count = 0;
    for(auto &it : DATA->sortedEntries)
    {
        if(start <= it.key && it.key <= end)
        {
            avg += it.value.rmssd;
            ++count;
        }
    }

    avg = avg / (float)std::max(count, 1);

    return avg;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getAverageRmssd1(JNIEnv *env, jobject obj, jint year, jint month, jint day)
{
    Date1 current;
    current.year = year;
    current.month = month;
    current.day = day;

    float avg = 0.0f;
    int count = 0;
    for(auto &it : DATA->sortedEntries)
    {
        if(current == it.key)
        {
            avg += it.value.rmssd;
            ++count;
        }
    }

    avg = avg / (float)std::max(count, 1);

    return avg;
}

extern "C" JNIEXPORT jobject JNICALL Java_denwan_hrv_Native_getDateTime(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        jclass clazz = env->FindClass ("denwan/hrv/DateTime");
        if(clazz)
        {
            auto dateTime = DATA->getDateTime(idx);

            jobject obj = env->AllocObject(clazz);
            jfieldID f_year = env->GetFieldID (clazz, "year", "I");
            env->SetIntField(obj, f_year, dateTime->year);

            jfieldID f_month = env->GetFieldID (clazz, "month", "I");
            env->SetIntField(obj, f_month, dateTime->month);

            jfieldID f_day = env->GetFieldID (clazz, "day", "I");
            env->SetIntField(obj, f_day, dateTime->day);

            jfieldID f_hour = env->GetFieldID (clazz, "hour", "I");
            env->SetIntField(obj, f_hour, dateTime->hour);

            jfieldID f_minute = env->GetFieldID (clazz, "minute", "I");
            env->SetIntField(obj, f_minute, dateTime->minute);

            return obj;
        }
    }

    return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_getIndex(int year, int month, int day, int hour, int minute)
{
    if(DATA) {
        DateTime1 key;
        key.year = year;
        key.month = month;
        key.day = day;

        key.hour = hour;
        key.minute = minute;

        return DATA->getIndex(key);
    }

    return -1;
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_isFirstOfDay(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->isFirstOfDay;
    }

    return -1;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getAvgRR(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->avgRR;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getSDNN(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->sdnn;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getRMSSD(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->rmssd;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getSDSD(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->sdsd;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getPNN50(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->pnn50;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getPNN20(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->pnn20;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getVLF(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->pVLF;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getLF(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->pLF;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jfloat JNICALL Java_denwan_hrv_Native_getHF(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->pHF;
    }

    return -1.0f;
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_getSleep(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->sleepQuality;
    }

    return 0;

}

extern "C" JNIEXPORT void JNICALL Java_denwan_hrv_Native_setSleep(JNIEnv *env, jobject obj, jint idx, jint value)
{
    if(DATA)
    {
        DATA->getHRV(idx)->sleepQuality = value;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_getMental(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->mentalHealth;
    }

    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_denwan_hrv_Native_setMental(JNIEnv *env, jobject obj, jint idx, jint value)
{
    if(DATA)
    {
        DATA->getHRV(idx)->mentalHealth = value;
    }
}

extern "C" JNIEXPORT jint JNICALL Java_denwan_hrv_Native_getPhysical(JNIEnv *env, jobject obj, jint idx)
{
    if(DATA)
    {
        return DATA->getHRV(idx)->physicalHealth;
    }

    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_denwan_hrv_Native_setPhysical(JNIEnv *env, jobject obj, jint idx, jint value)
{
    if(DATA)
    {
        DATA->getHRV(idx)->physicalHealth = value;
    }
}