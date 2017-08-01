//
// Created by tanne on 28.07.2017.
//

#ifndef HRVAPP_FREQUENCYDOMAIN_H
#define HRVAPP_FREQUENCYDOMAIN_H

#include "Array.h"
#include "HRV.h"

extern void freqDomainHRV(const ArrayF& ibi, const ArrayF& t, int nfft, float fs, HRV* out);

#endif //HRVAPP_FREQUENCYDOMAIN_H
