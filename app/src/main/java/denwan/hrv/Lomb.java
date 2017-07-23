package denwan.hrv;

/**
 * Created by tanne on 23.07.2017.
 */

public class Lomb {

    static final int MACC = 4;

    //#define MOD(a,b)	while (a >= b) a -= b


    private static class avevar_out
    {
        public double ave, var;

        avevar_out(double a, double v){ave=a;var=v;}
    }

    static avevar_out avevar(double data[], int n)
    {
        int j;
        double s, ep;

        double ave;
        double var;

        for (ave = 0.0, j = 1; j <= n; j++)
            ave += data[j];

        ave /= n;
        var = ep = 0.0;
        for (j = 1; j <= n; j++)
        {
            s = data[j] - ave;
            ep += s;
	        var += s*s;
        }

        var = (var - ep*ep/n) / (double)(n-1);
        //pwr = var;
        return new avevar_out(ave, var);
    }

    static void spread(double y, double yy[], int n, double x, int m)
    {
        int ihi, ilo, ix, j, nden;
        //static int nfac[11] = { 0, 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880 };
        int nfac[] = { 0, 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880 };
        double fac;

       // if (m > 10)
            //error("factorial table too small");
        ix = (int)x;
        if (x == (float)ix)
            yy[ix] += y;
        else
            {
            ilo = Math.min(Math.max((int)(x - 0.5*m + 1.0), 1), n - m + 1);
            ihi = ilo + m - 1;
            nden = nfac[m];
            fac = x - ilo;
            for (j = ilo + 1; j <= ihi; j++) fac *= (x - j);
            yy[ihi] += y*fac/(nden*(x - ihi));
            for (j = ihi-1; j >= ilo; j--) {
                nden = (nden/(j + 1 - ilo))*(j - ihi);
                yy[j] += y*fac/(nden*(x - j));
            }
        }
    }

    static double SIGN(double a, double b)
    {
        return  (b > 0.0 ? Math.abs(a) : -Math.abs(a));
    }

    static private double SQR(double a)
    {
        double sqrarg = a;
        return  (sqrarg == 0.0 ? 0.0 : sqrarg * sqrarg);
    }

    static class FasperOut
    {
        long nout[];
    }

    /*static FasperOut fasper(double x[], double y[], int n, double ofac, double hifac, double wk1[], double wk2[], int nwk)
    {
        //void avevar(), realft(), spread(), error();
        int j, k, ndim, nfreq, nfreqt;
        double ave, ck, ckk, cterm, cwt, den, df, effm, expy, fac, fndim, hc2wt,
                hs2wt, hypo, pmax, sterm, swt, var, xdif, xmax, xmin;

        int nout = (int)(0.5 * ofac * hifac * n);
        nfreqt = (int)(ofac * hifac * n * MACC);
        nfreq = 64;

        while (nfreq < nfreqt)
            nfreq <<= 1;

        ndim = nfreq << 1;
        //if (ndim > nwk)
           // error("workspaces too small\n");

        avevar_out avg = avevar(y, n);
        ave=avg.ave;
        var = avg.var;
        xmax = xmin = x[1];

        for (j = 2; j <= n; j++)
        {
            if (x[j] < xmin) xmin = x[j];
            if (x[j] > xmax) xmax = x[j];
        }

        xdif = xmax - xmin;

        for (j = 1; j <= ndim; j++)
            wk1[j] = wk2[j] = 0.0;

        fac = ndim/(xdif*ofac);
        fndim = ndim;

        for (j = 1; j <= n; j++)
        {
            ck = (x[j] - xmin)*fac;

            //MOD(ck, fndim);
            while (ck >= fndim) ck -= fndim;

            ckk = 2.0*(ck++);
            //MOD(ckk, fndim);
            while (ckk >= fndim) ckk -= fndim;

            ++ckk;
            spread(y[j] - ave, wk1, ndim, ck, MACC);
            spread(1.0, wk2, ndim, ckk, MACC);
        }
        realft(wk1, ndim, 1);
        realft(wk2, ndim, 1);
        df = 1.0/(xdif*ofac);
        pmax = -1.0;
        int jmax;
        for (k = 3, j = 1; j <= nout; j++, k += 2)
        {
            hypo = Math.sqrt(wk2[k]*wk2[k] + wk2[k+1]*wk2[k+1]);
            hc2wt = 0.5*wk2[k]/hypo;
            hs2wt = 0.5*wk2[k+1]/hypo;
            cwt = Math.sqrt(0.5+hc2wt);
            swt = SIGN(Math.sqrt(0.5-hc2wt), hs2wt);

            den = 0.5*n + hc2wt*wk2[k] + hs2wt*wk2[k+1];
            cterm = SQR(cwt*wk1[k] + swt*wk1[k+1])/den;
            sterm = SQR(cwt*wk1[k+1] - swt*wk1[k])/(n - den);
            wk1[j] = j*df;
            wk2[j] = (cterm+sterm)/(2.0*var);

            if (wk2[j] > pmax)
                pmax = wk2[jmax = j];
        }
        expy = Math.exp(-pmax);
        effm = 2.0*nout/ofac;
        double prob = effm*expy;
        if (prob > 0.01)
            prob = 1.0 - Math.pow(1.0 - expy, effm);
    }*

    static void test(double x[], double y[])
    {
       /* Compute the Lomb periodogram. */
       // fasper(x-1, y-1, n, 4.0, 2.0, wk1-1, wk2-1, 64*nmax, &nout, &jmax, &prob);

    /* Write the results.  Output only up to Nyquist frequency, so that the
       results are directly comparable to those obtained using conventional
       methods.  The normalization is by half the number of output samples; the
       sum of the outputs is (approximately) the mean square of the inputs.

       Note that the Nyquist frequency is not well-defined for an irregularly
       sampled series.  Here we use half of the mean sampling frequency, but
       the Lomb periodogram can return (less reliable) estimates of frequency
       content for frequencies up to half of the maximum sampling frequency in
       the input.  */

        /*maxout = nout/2;
        if (sflag) {        // smoothed

            pwr /= 4;

            if (aflag)      //smoothed amplitudes
                for (n = 0; n < maxout; n += 4)
                    printf("%g\t%g\n", wk1[n],
                            sqrt((wk2[n]+wk2[n+1]+wk2[n+2]+wk2[n+3])/(nout/(8.0*pwr))));

            else            // smoothed powers
                for (n = 0; n < maxout; n += 4)
                    printf("%g\t%g\n", wk1[n],
                            (wk2[n]+wk2[n+1]+wk2[n+2]+wk2[n+3])/(nout/(8.0*pwr)));

        }
        else {    	        // oversampled

            if (aflag)      //amplitudes
                for (n = 0; n < maxout; n++)
                    printf("%g\t%g\n", wk1[n], sqrt(wk2[n]/(nout/(2.0*pwr))));

            else            // powers
                for (n = 0; n < maxout; n++)
                    printf("%g\t%g\n", wk1[n], wk2[n]/(nout/(2.0*pwr)));

        }
    }*/
}
