//
// Created by tanne on 06.08.2017.
//

#include "Data.h"
#include "DateTime.h"

void RR_Entry::loadFromFile_DateTime(FILE* in)
{
    DateTime tmp;
    fread(&tmp, 1, sizeof(DateTime), in);
    key.year = tmp.year;
    key.month = tmp.month;
    key.day = tmp.day;
    key.hour = tmp.hour;
    key.minute = tmp.minute;

    fread(&n, 1, sizeof(int), in);

    rr_values = std::unique_ptr<float[]>(new float[n]);
    fread(rr_values.get(), 1, sizeof(float) * n, in);
}

void Data::saveToFile(const char* file) const
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

    fwrite((const void*)sortedEntries.data(), 1, header.entryCount * sizeof(Entry1), outFile);

    for(auto &it : rr_values)
    {
        it.writeToFile(outFile);
    }

    fwrite((const void*)firstOfDay.data(), 1, header.firstOfDayCount * sizeof(FirstOfDay), outFile);

    fclose(outFile);
}

struct Entry
{
    DateTime key;
    HRV value;

    friend bool operator<(const Entry &lhs, const Entry &rhs)
    {
        return lhs.key < rhs.key;
    }
};

int Data::loadFromFile_Header1(Header1 header, FILE* inFile)
{
    std::vector<Entry> sortedEntriesTmp;
    sortedEntriesTmp.resize(header.entryCount);
    fread(sortedEntriesTmp.data(), 1, sizeof(Entry) * header.entryCount, inFile);

    sortedEntries.reserve(header.entryCount);
    for(auto &it : sortedEntriesTmp)
    {
        sortedEntries.push_back({ it.key, it. value});
    }

    rr_values.reserve(header.rrCount);
    for(int i = 0; i < header.rrCount; ++i)
    {
        RR_Entry entry;
        entry.loadFromFile_DateTime(inFile);
        rr_values.push_back(std::move(entry));
    }

    firstOfDay.resize(header.firstOfDayCount);
    fread(firstOfDay.data(), 1, sizeof(FirstOfDay) * header.firstOfDayCount, inFile);

    return header.entryCount;
}

int Data::loadFromFile_Header2(Header2 header, FILE* inFile)
{
    sortedEntries.resize(header.entryCount);
    fread(sortedEntries.data(), 1, sizeof(Entry1) * header.entryCount, inFile);

    rr_values.reserve(header.rrCount);
    for(int i = 0; i < header.rrCount; ++i)
    {
        RR_Entry entry;
        entry.loadFromFile_DateTime1(inFile);
        rr_values.push_back(std::move(entry));
    }

    firstOfDay.resize(header.firstOfDayCount);
    fread(firstOfDay.data(), 1, sizeof(FirstOfDay) * header.firstOfDayCount, inFile);

    return header.entryCount;
}

int Data::loadFromFile(const char* file)
{
    FILE* inFile = fopen(file, "rb");
    if(inFile == nullptr)
        return 0;

    int result = 0;

    Header header;
    fread(&header, 1, sizeof(Header), inFile);

    rewind(inFile);

    if(header.size == sizeof(Header1) &&
       header.magic == Header1::MAGIC)
    {
        Header1 header1;
        fread(&header1, 1, sizeof(header1), inFile);

        result = loadFromFile_Header1(header1, inFile);
    }
    else if(header.size == sizeof(Header2) &&
       header.magic == Header2::MAGIC)
    {
        Header2 header1;
        fread(&header1, 1, sizeof(header1), inFile);

        result = loadFromFile_Header2(header1, inFile);
    }


    fclose(inFile);

    return result;
}