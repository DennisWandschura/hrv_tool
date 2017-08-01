//
// Created by tanne on 29.07.2017.
//

#ifndef HRVAPP_HRV_H
#define HRVAPP_HRV_H

#include <stdint.h>

struct HRV
{
    float avgRR; // ms
    float sdnn; // standard deviation of RR intervals
    float rmssd; // root mean square of successive differences
    //float lnRmssd;
    float sdsd; // standard deviation of successive differences

    int nn50; // number of successive pairs with more than 50ms difference
    int nn20; // number of successive pairs with more than 20ms difference
    float pnn50; // nn50 divided by count
    float pnn20 ; // nn20 divided by count

    float pVLF;
    float pLF;
    float pHF;

    float nLF;
    float nHF;
    float lfhf;

    int isFirstOfDay;
    int sleepQuality;
    int mentalHealth;
    int physicalHealth;
};

extern void timeDomain(const float* rr, int n, HRV* hrv);

#endif //HRVAPP_HRV_H
