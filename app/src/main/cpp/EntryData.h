//
// Created by tanne on 23.06.2017.
//

#ifndef HRVAPP_ENTRYDATA_H
#define HRVAPP_ENTRYDATA_H

#include <jni.h>

typedef unsigned char u8;

struct DateTime
{
    int day, month, year;
    int hour, minute;
};

struct HrvData
{
    float sdnn; // standard deviation of RR intervals
    float rmssd; // root mean square of successive differences
    float sdsd; // standard deviation of successive differences
    int nn50; // number of successive pairs with more than 50ms difference
    int nn20; // number of successive pairs with more than 20ms difference
    float pnn50; // nn50 divided by count
    float pnn20; // nn20 divided by count
};

struct EntryData
{
    DateTime dateTime;

    int sleepQuality;
    int mentalHealth;
    int physicalHealth;

    HrvData hrvData;

    int valueCount;
    float* values; // RR interval values
};

extern void writeEntriesToStream(u8* dst, const EntryData* data, int count);

#endif //HRVAPP_ENTRYDATA_H
