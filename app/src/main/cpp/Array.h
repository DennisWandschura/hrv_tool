//
// Created by tanne on 28.07.2017.
//

#ifndef HRVAPP_ARRAY_H
#define HRVAPP_ARRAY_H

#include <malloc.h>
#include <cstring>

template<typename T>
struct Array
{
    typedef T value_type;

    value_type* v;
    int n;
    int owns;

    Array() :v(0), n(0), owns(0) {}
    //explicit Array(int _n) :v((value_type*)malloc(sizeof(value_type) * _n)), n(_n), owns(1) {}

    Array(value_type* _v, int _n) :v(_v), n(_n), owns(0) {}

    Array(const Array &rhs)
            :v((value_type*)malloc(sizeof(value_type) * rhs.n)), n(rhs.n), owns(1)
    {
        memcpy(v, rhs.v, sizeof(value_type) * n);
    }

    Array(Array &&rhs) :v(rhs.v), n(rhs.n), owns(rhs.owns)
    {
        rhs.v = 0;
        rhs.n = rhs.owns = 0;
    }

    ~Array()
    {
        if (owns != 0)
        {
            free(v);
            owns = 0;
        }
    }

    Array& operator=(const Array& rhs)
    {
        if (this != &rhs)
        {
            if (owns != 0)
            {
                free(v);
            }

            n = rhs.n;
            v = (value_type*)malloc(sizeof(value_type) * n);
            owns = 1;
            memcpy(v, rhs.v, sizeof(value_type) * n);
        }

        return *this;
    }

    Array& operator=(Array&& rhs)
    {
        if (this != &rhs)
        {
            v = rhs.v;
            n = rhs.n;
            owns = rhs.owns;

            rhs.v = 0;
            rhs.n = 0;
            rhs.owns = 0;
        }

        return *this;
    }

    const value_type& back() const { return *(v + n - 1); }

    value_type& operator[](int i) { return v[i]; }
    const value_type& operator[](int i) const { return v[i]; }

    value_type* begin() { return v; }
    value_type* end() { return v + n; };

    void zero()
    {
        memset(v, 0, sizeof(value_type) * n);
    }

    void assign(const value_type* src, int n, int offset)
    {
        memcpy(this->v + offset, src, sizeof(value_type) * n);
    }

    void assign(const Array &other, int offset)
    {
        memcpy(this->v + offset, other.v, sizeof(value_type) * other.n);
    }
};

template<typename T>
Array<T> make_array(int count)
{
    Array<T> result;

    result.v = (T*)malloc(sizeof(T) * count);
    result.n = count;
    result.owns = 1;

    return result;
}

template<typename T>
Array<T> make_array(const Array<T>& src, int from, int to)
{
    auto count = to - from;
    auto result = make_array<T>(count);
    memcpy(result.v, src.v + from, count * sizeof(T));

    return result;
}

template<typename T>
inline Array<T> operator+(const Array<T>& a, const Array<T>& b)
{
    auto r = make_array(a.n);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] = a[i] + b[i];
    }
    return r;
}

template<typename T>
inline Array<T> operator-(const Array<T>& a, const Array<T>& b)
{
    auto r = make_array<T>(a.n);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] = a[i] - b[i];
    }
    return r;
}

template<typename T>
inline Array<T> operator*(const Array<T>& a, const Array<T>& b)
{
    auto r = make_array<T>(a.n);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] = a[i] * b[i];
    }
    return r;
}

template<typename T>
inline Array<T> operator+(const Array<T>& a, T b)
{
    auto r = make_array<T>(a.n);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] = a[i] + b;
    }
    return r;
}

template<typename T>
inline Array<T> operator*(const Array<T>& a, T b)
{
    auto r = make_array<T>(a.n);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] = a[i] * b;
    }
    return r;
}

template<typename T>
inline Array<T> operator*(T a, const Array<T>&b)
{
    Array<T> r(b);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] *= a;
    }
    return r;
}

template<typename T>
inline Array<T> operator/(const Array<T>& a, T b)
{
    auto r = make_array<T>(a.n);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] = a[i] / b;
    }
    return r;
}

template<typename T>
inline Array<T> operator-(const Array<T>& a, T b)
{
    auto r = make_array<T>(a.n);
    for (int i = 0; i < r.n; ++i)
    {
        r[i] = a[i] - b;
    }
    return r;
}

typedef Array<float> ArrayF;

#endif //HRVAPP_ARRAY_H
