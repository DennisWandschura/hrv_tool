//
// Created by tanne on 29.07.2017.
//

#ifndef HRVAPP_DATETIME_H
#define HRVAPP_DATETIME_H

#include <stdint.h>

struct Date
{
    union
    {
        uint32_t hash;
        struct
        {
            uint16_t year;
            unsigned char month, day;
        };
    };

    constexpr uint32_t getHash() const
    {
        return (year << 16) | (month << 8) | day;
    }
};

inline bool operator<(const Date &lhs, const Date &rhs)
{
    return lhs.getHash() < rhs.getHash();
}

inline bool operator<=(const Date &lhs, const Date &rhs)
{
    return lhs.getHash() <= rhs.getHash();
}

inline bool operator>(const Date &lhs, const Date &rhs)
{
    return lhs.getHash() > rhs.getHash();
}

inline bool operator>=(const Date &lhs, const Date &rhs)
{
    return lhs.getHash() >= rhs.getHash();
}

struct DateTime : public Date
{
    union
    {
        uint16_t hash;
        struct
        {
            unsigned char hour, minute;
        };
    };

    constexpr uint64_t getHash() const
    {
       return (static_cast<uint64_t>(Date::getHash()) << 32) | ((hour << 8) | minute);
    }
};

inline bool operator<(const DateTime &lhs, const DateTime &rhs)
{
   return lhs.getHash() < rhs.getHash();
}

inline bool operator==(const DateTime &lhs, const DateTime &rhs)
{
    return lhs.getHash() == rhs.getHash();
}

#endif //HRVAPP_DATETIME_H
