//
// Created by tanne on 28.07.2017.
//
#include <jni.h>
#include "frequencyDomain.h"
#include <unordered_map>
#include "DateTime.h"
#include <vector>
#include <memory>
#include "city.h"2
#include <algorithm>

template<>
struct std::hash<DateTime>
{
    std::size_t operator()(const DateTime& v) const
    {
        return CityHash32((const char*)&v, sizeof(DateTime));
    }
};

struct Data
{
    struct RR_Entry
    {
        std::unique_ptr<float[]> rr_values;
        DateTime key;
        int n;

        void writeToFile(FILE* outFile) const
        {
            fwrite(&key, 1, sizeof(DateTime), outFile);
            fwrite(&n, 1, sizeof(int), outFile);
            fwrite(rr_values.get(), 1, sizeof(float) * n, outFile);
        }

        uint8_t* writeToBuffer(uint8_t* dst) const
        {
            ::memcpy(dst, &key, sizeof(DateTime));
            dst += sizeof(DateTime);

            ::memcpy(dst, &n, sizeof(int));
            dst += sizeof(int);

            ::memcpy(dst, rr_values.get(), sizeof(float) * n);
            dst += (sizeof(float) * n);

            return dst;
        }

        void loadFromFile(FILE* in)
        {
            fread(&key, 1, sizeof(DateTime), in);
            fread(&n, 1, sizeof(int), in);

            rr_values = std::unique_ptr<float[]>(new float[n]);
            fread(rr_values.get(), 1, sizeof(float) * n, in);
        }

        const uint8_t* loadFromBuffer(const uint8_t* src)
        {
            ::memcpy(&key, src, sizeof(DateTime));
            src += sizeof(DateTime);

            ::memcpy(&n, src, sizeof(int));
            src += sizeof(int);

            rr_values = std::unique_ptr<float[]>(new float[n]);
            ::memcpy(rr_values.get(), src, sizeof(float) * n);
            src += (sizeof(float) * n);

            return src;
        }
    };

    struct Entry
    {
        DateTime key;
        HRV value;

        friend bool operator<(const Entry &lhs, const Entry &rhs)
        {
            return lhs.key < rhs.key;
        }
    };

    struct FirstOfDay
    {
        int year, month, day;
        int idx;

        friend bool operator==(const FirstOfDay &lhs, const FirstOfDay &rhs)
        {
            return ((lhs.year << 16) | (lhs.month << 8) | lhs.day) == ((rhs.year << 16) | (rhs.month << 8) | rhs.day);
        }

        friend bool operator<(const FirstOfDay &lhs, const FirstOfDay &rhs)
        {
            return ((lhs.year << 16) | (lhs.month << 8) | lhs.day) < ((rhs.year << 16) | (rhs.month << 8) | rhs.day);
        }
    };

    std::vector<Entry> sortedEntries;
    std::vector<RR_Entry> rr_values;
    std::vector<FirstOfDay> firstOfDay;

    Data(): sortedEntries(), rr_values(), firstOfDay(){}

    int getFirstOfToday(int year, int month, int day)
    {
        auto it = std::find_if(firstOfDay.begin(), firstOfDay.end(), [year, month, day](const FirstOfDay &lhs) {
            return (lhs.year == year && lhs.month == month && lhs.day == day);
        });

        return (it == firstOfDay.end()) ? -1 : it->idx;
    }

    // returns index or -1 if indices were changed
    int addEntry(const DateTime &key, const HRV &data, std::unique_ptr<float[]>&& rr, int n)
    {
        int idx = (int)sortedEntries.size();
        sortedEntries.push_back({key, data});

        if(idx > 1 &&
                sortedEntries[idx - 1].key >= key)
        {
            std::sort(sortedEntries.begin(), sortedEntries.end());
            idx = -1;
        }

        RR_Entry rr_entry;
        rr_entry.key = key;
        rr_entry.n = n;
        rr_entry.rr_values = std::move(rr);
        rr_values.push_back(std::move(rr_entry));

        if(data.isFirstOfDay >= 0)
        {
            firstOfDay.push_back(FirstOfDay{key.year, key.month, key.day, idx});
        }

        return idx;
    }

    void updateIndices()
    {
        std::sort(firstOfDay.begin(), firstOfDay.end());

        firstOfDay.erase(std::unique(firstOfDay.begin(), firstOfDay.end()), firstOfDay.end());

        for(auto &it : firstOfDay)
        {
            auto iter = std::lower_bound(sortedEntries.begin(), sortedEntries.end(), it, [](const Entry& lhs, const FirstOfDay &rhs) {
                Date tmp;
                tmp.year = rhs.year;
                tmp.month = rhs.month;
                tmp.day = rhs.day;

                return lhs.key < tmp;
            });

            auto idx = iter - sortedEntries.begin();
            it.idx = idx;
        }
    }

    const HRV* getHRV(int idx) const
    {
        return &sortedEntries[idx].value;
    }

    HRV* getHRV(int idx)
    {
        return &sortedEntries[idx].value;
    }

    const DateTime* getDateTime(int idx) const
    {
        return &sortedEntries[idx].key;
    }

    int getIndex(DateTime key) const {
        auto it = std::lower_bound(sortedEntries.begin(), sortedEntries.end(), key, [](const Entry &lhs, const DateTime &rhs)
        {
            return lhs.key<rhs;
        });

        return (it != sortedEntries.end() && it->key == key) ? (it - sortedEntries.begin()) : -1;
    }

    struct Header
    {
        int size;
        int magic;
    };

    struct Header1 : Header{
        enum : int {MAGIC = 0x133700};

        int entryCount;
        int rrCount;
        int firstOfDayCount;
    };

    typedef Header1 MyHeader;

    void saveToFile(const char* file) const
    {
        FILE* outFile = fopen(file, "wb");
        if(outFile == nullptr)
            return;

        MyHeader header;
        header.size = sizeof(MyHeader);
        header.magic = MyHeader::MAGIC;
        header.entryCount =( int)sortedEntries.size();
        header.rrCount = (int)rr_values.size();
        header.firstOfDayCount = (int)firstOfDay.size();

        fwrite(&header, 1, sizeof(MyHeader), outFile);

        fwrite((const void*)sortedEntries.data(), 1, header.entryCount * sizeof(Entry), outFile);

        for(auto &it : rr_values)
        {
            it.writeToFile(outFile);
        }

        fwrite((const void*)firstOfDay.data(), 1, header.firstOfDayCount * sizeof(FirstOfDay), outFile);

        fclose(outFile);
    }

    int loadFromFile(const char* file)
    {
        FILE* inFile = fopen(file, "rb");
        if(inFile == nullptr)
            return 0;

        MyHeader header;
        fread(&header, 1, sizeof(MyHeader), inFile);
        if(header.size != sizeof(MyHeader) ||
                header.magic != MyHeader::MAGIC)
        {
            fclose(inFile);
            return 0;
        }

        sortedEntries.resize(header.entryCount);
        fread(sortedEntries.data(), 1, sizeof(Entry) * header.entryCount, inFile);

        rr_values.reserve(header.rrCount);
        for(int i = 0; i < header.rrCount; ++i)
        {
            RR_Entry entry;
            entry.loadFromFile(inFile);
            rr_values.push_back(std::move(entry));
        }

        firstOfDay.resize(header.firstOfDayCount);
        fread(firstOfDay.data(), 1, sizeof(FirstOfDay) * header.firstOfDayCount, inFile);

        fclose(inFile);

        return header.entryCount;
    }
};

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
        DateTime key;
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
    Date start, end;
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
        DateTime key;
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