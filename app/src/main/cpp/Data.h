//
// Created by tanne on 06.08.2017.
//

#ifndef HRVAPP_DATA_H
#define HRVAPP_DATA_H

#include "DateTime1.h"
#include <memory>
#include "HRV.h"
#include <vector>
#include <algorithm>

typedef Date1 MyDate;
typedef DateTime1 MyDateTime;

struct RR_Entry
{
    std::unique_ptr<float[]> rr_values;
    DateTime1 key;
    int n;

    void writeToFile(FILE* outFile) const
    {
        fwrite(&key, 1, sizeof(DateTime1), outFile);
        fwrite(&n, 1, sizeof(int), outFile);
        fwrite(rr_values.get(), 1, sizeof(float) * n, outFile);
    }

    void loadFromFile_DateTime(FILE* in);

    void loadFromFile_DateTime1(FILE* in)
    {
        fread(&key, 1, sizeof(DateTime1), in);
        fread(&n, 1, sizeof(int), in);

        rr_values = std::unique_ptr<float[]>(new float[n]);
        fread(rr_values.get(), 1, sizeof(float) * n, in);
    }
};

struct Entry1
{
    DateTime1 key;
    HRV value;

    friend bool operator<(const Entry1 &lhs, const Entry1 &rhs)
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

struct Data
{
    std::vector<Entry1> sortedEntries;
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
    int addEntry(const DateTime1 &key, const HRV &data, std::unique_ptr<float[]>&& rr, int n)
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
            auto iter = std::lower_bound(sortedEntries.begin(), sortedEntries.end(), it, [](const Entry1& lhs, const FirstOfDay &rhs) {
                Date1 tmp;
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

    const DateTime1* getDateTime(int idx) const
    {
        return &sortedEntries[idx].key;
    }

    int getIndex(DateTime1 key) const {
        auto it = std::lower_bound(sortedEntries.begin(), sortedEntries.end(), key, [](const Entry1 &lhs, const DateTime1 &rhs)
        {
            return lhs.key < rhs;
        });

        return (it != sortedEntries.end() && it->key == key) ? (it - sortedEntries.begin()) : -1;
    }

    struct Header
    {
        int size;
        int magic;
    };

    struct Header1 : Header
    {
        enum : int {MAGIC = 0x133700};

        int entryCount;
        int rrCount;
        int firstOfDayCount;
    };

    struct Header2 : Header
    {
        enum : int {MAGIC = 0x133701};

        int entryCount;
        int rrCount;
        int firstOfDayCount;
    };

    typedef Header2 MyHeader;

    void saveToFile(const char* file) const;

    int loadFromFile_Header1(Header1 header, FILE* inFile);
    int loadFromFile_Header2(Header2 header, FILE* inFile);
    int loadFromFile(const char* file);
};

#endif //HRVAPP_DATA_H
