#include "EntryData.h"
#include <cstring>
#include <stddef.h>
//
// Created by tanne on 23.06.2017.
//

#define COPY(MEMBER) ::memcpy(&dst[offsetof(EntryData, MEMBER)], &src->MEMBER, sizeof(src->MEMBER)); writtenBytes += sizeof(src->MEMBER);

void writeEntryData(u8* dst, const EntryData* src, int* writtenBytesOut)
{
    int writtenBytes = 0;
    COPY(dateTime);

    COPY(sleepQuality);
    COPY(mentalHealth);
    COPY(physicalHealth);

    COPY(hrvData);

    COPY(valueCount);

    const int valuesSizeBytes = sizeof(src->values) * src->valueCount;
    ::memcpy(&dst[offsetof(EntryData, values)], &src->values,valuesSizeBytes );
    writtenBytes += valuesSizeBytes;

    *writtenBytesOut = writtenBytes;
}

void writeEntriesToStream(u8* dst, const EntryData* data, int count)
{
    int writtenBytes;
    for(int i = 0; i < count; ++i)
    {
        writeEntryData(dst, data, &writtenBytes);
        dst += writtenBytes;
    }
}