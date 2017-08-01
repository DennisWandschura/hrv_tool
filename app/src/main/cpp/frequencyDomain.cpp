//
// Created by tanne on 28.07.2017.
//
#include "frequencyDomain.h"
#include <vector>
#include <float.h>
#include <math.h>
#include <algorithm>

/************************************************************************************
Function    : void detrend_IP(T *y, T *x, int m)
Description : Remove the linear trend of the input floating point data. Note that this
will initialize a work buffer inside the function. So if you are calling
this many, many times, create your work buffer in the calling scope and call
detrend(T *y, T*x, int m) instead to avoid initializing memory over and over
again.
Inputs      : y - Floating point input data
m - Input data length
Outputs     : y - Data with linear trend removed
Copyright   : DSO National Laboratories
History     : 01/02/2008, TCK, Adapted from HYC code
01/12/2008, TCK, Added in return value
25/01/2016, Pier, Changed into template type, removed need for work buffer
*************************************************************************************/
template<typename T>
void detrend_IP(T *y, int m)
{
    T xmean, ymean;
    int i;
    T temp;
    T Sxy;
    T Sxx;

    T grad;
    T yint;

    auto x = make_array<T>(m);
    //std::unique_ptr<T[]> x(new T[m]);

    /********************************
    Set the X axis Liner Values
    *********************************/
    for (i = 0; i < m; i++)
        x[i] = (float)i;

    /********************************
    Calculate the mean of x and y
    *********************************/
    xmean = 0;
    ymean = 0;
    for (i = 0; i < m; i++)
    {
        xmean += x[i];
        ymean += y[i];
    }
    xmean /= m;
    ymean /= m;

    /********************************
    Calculate Covariance
    *********************************/
    temp = 0;
    for (i = 0; i < m; i++)
        temp += x[i] * y[i];
    Sxy = temp / m - xmean * ymean;

    temp = 0;
    for (i = 0; i < m; i++)
        temp += x[i] * x[i];
    Sxx = temp / m - xmean * xmean;

    /********************************
    Calculate Gradient and Y intercept
    *********************************/
    grad = Sxy / Sxx;
    yint = -grad * xmean + ymean;

    /********************************
    Removing Linear Trend
    *********************************/
    for (i = 0; i < m; i++)
        y[i] = y[i] - (grad * i + yint);

}

float mean(const ArrayF &a)
{
    float m = 0.f;
    for (int i = 0; i < a.n; ++i)
        m += a.v[i];

    return m / (float)a.n;
}

float avg(const ArrayF& v)
{
    float result = 0.f;
    for (int i = 0; i < v.n; ++i)
    {
        result += v[i];
    }

    return result / (float)v.n;
}

float min(const ArrayF& v)
{
    float m = FLT_MAX;
    for (int i = 0; i < v.n; ++i)
    {
        m = std::min(m, v[i]);
    }

    return m;
}

float max(const ArrayF& v)
{
    float m = FLT_MIN;
    for (int i = 0; i < v.n; ++i)
    {
        m = std::max(m, v[i]);
    }

    return m;
}

float max(const float* v, int n, int* idx)
{
    float m = FLT_MIN;
    for (int i = 0; i < n; ++i)
    {
        if (v[i] > m)
        {
            m = v[i];
            *idx = i;
        }
    }

    return m;
}

float max(const ArrayF& v, int* idx)
{
    return max(v.v, v.n, idx);
}

float max(const std::vector<float>& v, int* idx)
{
    return max(v.data(), (int)v.size(), idx);
}

namespace lomb_ml
{
    ArrayF diff(const ArrayF& src)
    {
        auto result = make_array<float>(src.n - 1);
        for (int i = 0; i < result.n; ++i)
        {
            result[i] = src[i + 1] - src[i];
        }

        return result;
    }

    float sd(const ArrayF &v)
    {
        auto average = avg(v);

        float variance = 0.f;
        for (int i = 0; i < v.n; ++i)
        {
            auto diff = v[i] - average;
            diff = diff * diff;

            variance += diff;
        }

        variance = variance / v.n;

        return sqrtf(variance);
    }

    ArrayF zeros(int n)
    {
        auto a = make_array<float>(n);
        memset(a.v, 0, n * sizeof(float));
        return a;
    }

    int size(const ArrayF &v)
    {
        return v.n;
    }

    inline float sin(float x)
    {
        return ::sinf(x);
    }

    ArrayF sin(const ArrayF &v)
    {
        ArrayF r(v);
        for (int i = 0; i < r.n; ++i)
        {
            r[i] = ::sinf(r[i]);
        }

        return r;
    }

    inline float cos(float x)
    {
        return ::cosf(x);
    }

    ArrayF cos(const ArrayF &v) {
        ArrayF r(v);
        for (int i = 0; i < r.n; ++i) {
            r[i] = cos(r[i]);
        }

        return r;
    }

    float sum(const ArrayF &v)
    {
        float s = 0.f;
        for (int i = 0; i < v.n; ++i)
        {
            s += v[i];
        }
        return s;
    }

    float pow(float x, float y)
    {
        return ::powf(x, y);
    }

    ArrayF pow(const ArrayF &v, float f)
    {
        ArrayF t = v;
        for (int i = 0; i < t.n; ++i)
            t[i] = pow(t[i], f);

        return t;
    }

    void lomb2(const ArrayF& y, const ArrayF& t, const ArrayF &f, bool flagNorm, ArrayF* Pn)
    {
        //float upper_freq = 0.5f / min(diff(t));


        //% subtract mean, compute variance, initialize Pn

        auto meanY = mean(y);
        ArrayF z = y;
        for (int i = 0; i < z.n; ++i)
            z[i] -= meanY;
        float var = sd(y);//std(y) ^ 2;
        var = var * var;

        int N = f.n;//length(f);
        *Pn = zeros(size(f));

        const float pi = 3.14159265358979323846f;

        //%	now do main loop for all frequencies
        for (int i = 0; i < N; ++i)
        {
            auto w = 2 * pi * f[i];
            if (w > 0)
            {
                auto twt = 2.0f * w * t;
                //Array twt(t.n);
                //for (int k = 0; k < twt.n; ++k)
                //	twt[k] = 2.f * w * t[k];

                auto tau = atan2f(sum(sin(twt)), sum(cos(twt))) / 2 / w;

                auto wtmt = w * (t - tau);
                (*Pn)[i] = (pow(sum(z * cos(wtmt)), 2)) / sum(pow(cos(wtmt), 2)) + (pow(sum(z * sin(wtmt)), 2)) / sum(pow(sin(wtmt), 2));
            }
            else
            {
                (*Pn)[i] = pow(sum(z * t), 2) / sum(pow(t, 2));
            }
        }

        if (flagNorm) //%normalize by variance
        {
            //(*Pn) = (*Pn) / (2.0 * var);
            for(int i = 0; i < Pn->n; ++i)
                (*Pn)[i] = (*Pn)[i] / (2.0f * var);
        }
        else // return denormalized spectrum(see T.Thong)
        {
            // (*Pn) = (*Pn) / (float)y.n;
            for(int i = 0; i < Pn->n; ++i)
                (*Pn)[i] = (*Pn)[i] / (float)y.n;
        }
    }

    float floor(float v)
    {
        return ::floorf(v);
    }

    ArrayF floor(ArrayF &&rhs)
    {
        for (int i = 0; i < rhs.n; ++i)
        {
            rhs[i] = floor(rhs[i]);
        }

        return rhs;
    }

    void linspace(float a, float b, int n, ArrayF* dst)
    {
        auto diff = b - a;
        auto stepSize = diff / (float)n;

        float step = 0.0f;
        *dst = make_array<float>(n);
        for (int i = 0; i < n; ++i)
        {
            (*dst)[i] = a + step;
            step += stepSize;
        }
    }

    void calcLomb(const ArrayF&t, const ArrayF&y, int nfft, float maxF,
                  ArrayF* F, ArrayF* PSD)
    {
        //%Calculate PSD
        float deltaF = maxF / nfft;

        linspace(0.0, maxF - deltaF, nfft, F);
       lomb2(y, t, *F, false, PSD); //%calc lomb psd
    }
}

ArrayF sign(const ArrayF& v)
{
    ArrayF t(v);
    for (int i = 0; i < t.n; ++i)
    {
        t[i] = (t[i] == 0.f) ? 0.0f : (t[i] > 0.f ? 1 : -1);
    }

    return t;
}

Array<bool> logical(const ArrayF &v)
{
    auto r = make_array<bool>(v.n);
    for (int i = 0;i < v.n; ++i)
        r[i] = (v[i] == 0.0f) ? false : true;

    return r;
}

void zipeaks(const ArrayF &y, std::vector<float>* pks, std::vector<int>* locs)
{
    /*function[pks locs] = zipeaks(y)
    % zippeaks: finds local maxima of input signal y
    %Usage : peak = zipeaks(y);
    %Returns 2x(number of maxima) array
    %pks = value at maximum
    %locs = index value for maximum
    %
    %Reference:  2009, George Zipfel(Mathworks File Exchange #24797)*/

    //% check dimentions
    if (y.n == 0)
    {
        //Warning('Empty input array')
        //pks = []; locs = [];
        return;
    }

    //[rows cols] = size(y);
    int rows = y.n;
    int cols = 1;
    if (cols == 1 && rows > 1) //% all data in 1st col
        ;//y = y';
    else if (cols == 1 && rows == 1)
    {
        //Warning('Short input array')
        //	pks = []; locs = [];
        return;
    }

    //%Find locations of local maxima
    //%yD = 1 at maxima, yD = 0 otherwise, end point maxima excluded
    //N = length(y) - 2;
    //yD=[0 (sign(sign(y(2:N+1)-y(3:N+2))-sign(y(1:N)-y(2:N+1))-.1)+1) 0];
    int N = y.n - 2;
    auto tmp_yD = (sign(sign(make_array(y, 1, N + 1) - make_array(y, 2, N + 2)) - sign(make_array(y, 0, N) - make_array(y, 1, N + 1)) - 0.1f) + 1.f);
    Array<float> yD = make_array<float>(tmp_yD.n + 2);
    yD.zero();
    yD.assign(tmp_yD, 1);
    //%Indices of maxima and corresponding values of y
    auto Y = logical(yD);

    //I = 1:length(Y);
    //Array<int> I(Y.n);
//	for (int i = 0; i < Y.n; ++i)
    //	I[i] = i;

    //pks = y(Y);
    //locs = I(Y);
    for (int i = 0; i < Y.n; ++i)
    {
        if (Y[i])
        {
            locs->push_back(i);

            pks->push_back(y[i]);
        }
    }

    return;
}

float getPeak(const ArrayF& F, const ArrayF& PSD, const std::vector<int>& iF)
{
    ArrayF tmpF = make_array<float>(iF.size());
    int k = 0;
    for (auto &it : iF)
    {
        tmpF[k++] = F[it];
    }

    ArrayF tmppsd= make_array<float>(iF.size());
    k = 0;
    for (auto &it : iF)
    {
        tmppsd[k++] = PSD[it];
    }

    std::vector<float> pks;
    std::vector<int> ipks;
    zipeaks(tmppsd, &pks, &ipks);

    float peak = 0.f;
    if (!pks.empty())
    {
        //[tmpMax i] = max(pks);
        int i;
        float tmpMax = max(pks, &i);

        peak = tmpF[ipks[i]];
    }
    else
    {
        //[tmpMax i] = max(tmppsd);
        int i;
        float tmpMax = max(tmppsd, &i);

        peak = tmpF[i];
    }
    return peak;
}

float trapz(const ArrayF &x, const ArrayF &y)
{
    float sum = 0.f;
    int i = 0;

    for (i = 0; i < x.n - 1; ++i)
    {
        sum += ((x[i + 1] - x[i]) * (y[i + 1] + y[i]));
    }

    return sum * .5f;
}

ArrayF makeArray(const ArrayF &src, const std::vector<int> &indices)
{
    ArrayF dst= make_array<float>(indices.size());
    for (int i = 0; i < (int)indices.size(); ++i)
    {
        dst[i] = src[indices[i]];
    }

    return dst;
}

void calcAreas(const ArrayF& F, ArrayF& PSD, const float(&VLF)[2], const float(&LF)[2], const float(&HF)[2], bool flagNorm, HRV* out)
{
    //%normalize PSD if needed
    if (flagNorm)
        PSD = PSD / max(PSD);

    //% find the indexes corresponding to the VLF, LF, and HF bands
    //iVLF = (F >= VLF(1)) & (F <= VLF(2));
    //iLF = (F >= LF(1)) & (F <= LF(2));
    //iHF = (F >= HF(1)) & (F <= HF(2));

    std::vector<int> iVLF, iLF, iHF;
    for (int i = 0; i < F.n; ++i)
    {
        if (F[i] >= VLF[0] && F[i] <= VLF[1])
            iVLF.push_back(i);

        if (F[i] >= LF[0] && F[i] <= LF[1])
            iLF.push_back(i);

        if (F[i] >= HF[0] && F[i] <= HF[1])
            iHF.push_back(i);
    }

    //%Find peaks
    //%VLF Peak
    auto peakVLF = getPeak(F, PSD, iVLF);

    //%LF Peak
    auto peakLF = getPeak(F, PSD, iLF);

    //%HF Peak
    auto peakHF = getPeak(F, PSD, iHF);

    //% calculate raw areas(power under curve), within the freq bands(ms ^ 2)
    auto aVLF = trapz(makeArray(F, iVLF), makeArray(PSD, iVLF));
    auto aLF = trapz(makeArray(F, iLF), makeArray(PSD, iLF));
    auto aHF = trapz(makeArray(F, iHF), makeArray(PSD, iHF));
    auto aTotal = aVLF + aLF + aHF;

    //%calculate areas relative to the total area(%)
    out->pVLF = (aVLF / aTotal) * 100.f;
    out->pLF = (aLF / aTotal) * 100.f;
    out->pHF = (aHF / aTotal) * 100.f;

    //%calculate normalized areas(relative to HF + LF, n.u.)
    out->nLF = aLF / (aLF + aHF);
    out->nHF = aHF / (aLF + aHF);

    //%calculate LF / HF ratio
    out->lfhf = aLF / aHF;

    //printf("peakVLF %f\npeakLF %f\npeakHF %f\npVLF %f\npLF %f\npHF %f\nnLF %f\nnHF %f\nlfhf %f\n\n", peakVLF, peakLF, peakHF, pVLF, pLF, pHF, nLF, nHF, lfhf);

    return;
}

void freqDomainHRV(const ArrayF& ibi, const ArrayF& t, int nfft, float fs, HRV* out)
{
    auto y = ibi;
    for(int i = 0; i < y.n; ++i)
        y[i] = y[i] * 1000.0f;
    //y = y * 1000.0; //convert ibi to ms

    float maxF = fs / 2.0f;

    //%prepare y
    //y = detrend(y, 'linear');
    detrend_IP(y.v, y.n);
    y = y - mean(y);

    ArrayF PSD, F;
    lomb_ml::calcLomb(t, y, nfft, maxF, &F, &PSD);

    const float VLF[2] = { 0.0f, 0.04f };
    const float LF[2] = { 0.04f, 0.15f };
    const float HF[2] = { 0.15f, 0.4f };

    calcAreas(F, PSD, VLF, LF, HF, true, out);
}