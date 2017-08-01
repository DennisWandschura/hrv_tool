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
};

inline bool operator<(const Date &lhs, const Date &rhs)
{
    return lhs.hash < rhs.hash;
}

inline bool operator<=(const Date &lhs, const Date &rhs)
{
    return lhs.hash <= rhs.hash;
}

inline bool operator>(const Date &lhs, const Date &rhs)
{
    return lhs.hash > rhs.hash;
}

inline bool operator>=(const Date &lhs, const Date &rhs)
{
    return lhs.hash >= rhs.hash;
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

    uint64_t getHash() const
    {
        return ((uint64_t)Date::hash | (static_cast<uint64_t>(DateTime::hash) << 32));
    }
};

inline bool operator<(const DateTime &lhs, const DateTime &rhs)
{
   if(*static_cast<const Date*>(&lhs) < *static_cast<const Date*>(&rhs))
   {
       return true;
   }
    else
   {
       return (lhs.hour < rhs.hour) ||(lhs.hour == rhs.hour && lhs.minute < rhs.minute);
   }
}

#endif //HRVAPP_DATETIME_H
