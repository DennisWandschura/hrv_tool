//
// Created by tanne on 30.07.2017.
//
#include "HRV.h"
#include <math.h>
#include <vector>

float avg(const float* v, int n)
{
    float result = 0.0;
    for (int i = 0; i < n; ++i)
    {
        result += v[i];
    }

    return result / (float)n;
}

void difference(const float* src, const int n, float* dst)
{
    int dst_n = n - 1;
    for (int i = 0; i < dst_n; ++i)
    {
        dst[i] = src[i + 1] - src[i];
    }
}

float standard_deviation(const float* v, const int n)
{
    auto average = avg(v, n);

    float variance = 0.0;
    for (int i = 0; i < n; ++i)
    {
        auto diff = v[i] - average;
        diff = diff * diff;

        variance += diff;
    }

    variance = variance / n;

    return ::sqrt(variance);
}

void rmssd(const float* rr, const int n, HRV* hrv)
{
    const int diff_n = n - 1;
    std::vector<float> diff(diff_n);
    difference(rr, n, diff.data());

    hrv->nn50 = 0;
    hrv->nn20 = 0;

    float result = 0.0f;
    for (int i = 0; i < diff_n; ++i)
    {
        auto tmp = fabs(diff[i]);
        if (tmp > 0.05f)
        {
            ++hrv->nn50;
        }

        if (tmp > 0.02f)
        {
            ++hrv->nn20;
        }

        result += (diff[i] * diff[i]);
    }

    result /= (float)diff_n;

    hrv->avgRR = avg(rr, n);
    hrv->sdnn = standard_deviation(rr, n);
    hrv->rmssd = sqrt(result);
    hrv->sdsd = standard_deviation(diff.data(), diff_n);
    hrv->pnn50 = hrv->nn50 / (float)(diff_n);
    hrv->pnn20 = hrv->nn20 / (float)(diff_n);
}

void timeDomain(const float* rr, int n, HRV* hrv)
{
    rmssd(rr, n, hrv);
}