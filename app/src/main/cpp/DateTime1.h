//
// Created by tanne on 06.08.2017.
//

#ifndef HRVAPP_DATETIME1_H
#define HRVAPP_DATETIME1_H

#include "DateTime.h"

struct Date1
{
    union
    {
        uint32_t hash;
        struct
        {
            uint8_t day;
            uint8_t month;
            uint16_t year;
        };
    };

    constexpr uint32_t getHash() const
    {
        return this->hash;
    }
};

inline bool operator<(const Date1 &lhs, const Date1 &rhs)
{
    return lhs.getHash() < rhs.getHash();
}

inline bool operator<=(const Date1 &lhs, const Date1 &rhs)
{
    return lhs.getHash() <= rhs.getHash();
}

inline bool operator>(const Date1 &lhs, const Date1 &rhs)
{
    return lhs.getHash() > rhs.getHash();
}

inline bool operator>=(const Date1 &lhs, const Date1 &rhs)
{
    return lhs.getHash() >= rhs.getHash();
}

inline bool operator==(const Date1 &lhs, const Date1 &rhs)
{
    return lhs.getHash() == rhs.getHash();
}

struct DateTime1
{
    union
    {
        uint64_t hash;
        struct
        {
            uint32_t low;
            uint32_t high;
        };
        struct
        {
            // low
            uint16_t minute;
            uint16_t hour;

            // high
            uint8_t day;
            uint8_t month;
            uint16_t year;
        };
    };

    constexpr uint64_t getHash() const
    {
        return this->hash;
    }

    DateTime1():hash(0){}
    DateTime1(const DateTime &rhs):minute(rhs.minute), hour(rhs.hour), day(rhs.day), month(rhs.month), year(rhs.year) {}
};

inline bool operator<(const DateTime1 &lhs, const DateTime1 &rhs)
{
    return lhs.getHash() < rhs.getHash();
}

inline bool operator<=(const DateTime1 &lhs, const DateTime1 &rhs)
{
    return lhs.getHash() <= rhs.getHash();
}

inline bool operator>(const DateTime1 &lhs, const DateTime1 &rhs)
{
    return lhs.getHash() > rhs.getHash();
}

inline bool operator>=(const DateTime1 &lhs, const DateTime1 &rhs)
{
    return lhs.getHash() > rhs.getHash();
}

inline bool operator==(const DateTime1 &lhs, const DateTime1 &rhs)
{
    return lhs.getHash() == rhs.getHash();
}



inline bool operator<(const DateTime1 &lhs, const Date1 &rhs)
{
    return lhs.high < rhs.getHash();
}

inline bool operator==(const DateTime1 &lhs, const Date1 &rhs)
{
    return lhs.high == rhs.getHash();
}

inline bool operator==(const Date1 &lhs, const DateTime1 &rhs)
{
    return rhs.high == lhs.getHash();
}

inline bool operator<=(const Date1 &lhs, const DateTime1 &rhs)
{
    return lhs.getHash() <= rhs.high;
}

inline bool operator<=(const DateTime1 &lhs, const Date1 &rhs)
{
    return lhs.high <= rhs.getHash();
}

#endif //HRVAPP_DATETIME1_H
