package denwan.activity;

import android.os.AsyncTask;

import com.sweetzpot.stravazpot.activity.api.ActivityAPI;
import com.sweetzpot.stravazpot.activity.model.*;
import com.sweetzpot.stravazpot.activity.model.Activity;
import com.sweetzpot.stravazpot.athlete.model.Athlete;
import com.sweetzpot.stravazpot.authenticaton.api.AuthenticationAPI;
import com.sweetzpot.stravazpot.authenticaton.model.AppCredentials;
import com.sweetzpot.stravazpot.authenticaton.model.LoginResult;
import com.sweetzpot.stravazpot.authenticaton.model.Token;
import com.sweetzpot.stravazpot.common.api.AuthenticationConfig;
import com.sweetzpot.stravazpot.common.api.StravaConfig;
import com.sweetzpot.stravazpot.common.model.Time;

import java.util.List;

/**
 * Created by tanne on 26.07.2017.
 */

public class Strava {
    public interface  OnQuerySuccess
    {
        public void run();
    }

    public final static int CLIENT_ID = 18821;
    final static String CLIENT_SECRET = "a2e4499e5e88812965c6994b97655773efb07eb7";

    public LoginResult loginResult = null;
    String resultStr = new String();
    ActivityAPI activityAPI = null;
    StravaConfig config = null;
    Token token = null;
    Athlete athlete = null;
    public List<com.sweetzpot.stravazpot.activity.model.Activity> m_activities24h = null;
    public int lastQueryTime = 0;
    OnQuerySuccess m_onQuerySuccess = null;

    void onSuccess(LoginResult result)
    {
        token = result.getToken();
        athlete = result.getAthlete();

        config = StravaConfig.withToken(token).debug().build();
        activityAPI = new ActivityAPI(config);



        new QueryActivities().execute();
    }

    class QueryActivities extends AsyncTask<Void, Void, List<Activity>>
    {
        @Override
        protected List<com.sweetzpot.stravazpot.activity.model.Activity> doInBackground(Void... params) {

            List<com.sweetzpot.stravazpot.activity.model.Activity> activities;
            if(lastQueryTime == 0) {
                //long secondsNow = System.currentTimeMillis() / 1000l;

                activities = activityAPI.listMyActivities()
                        // .before(Time.seconds((int)secondsNow))
                        .after(Time.seconds(0))
                        // .after(Time.seconds((int)secondsNow))
                        // .inPage(1)
                        //.perPage(1)
                        .execute();

                if(activities.size() > 0)
                {
                    // lastQueryTime = (int)activities.get(0).getStartDate().getTime();
                    lastQueryTime = (int)activities.get(activities.size() - 1).getStartDate().getTime();
                }
            }
            else
            {
                activities = activityAPI.listMyActivities()
                        .after(Time.seconds((int)lastQueryTime))
                        // .inPage(1)
                        //.perPage(1)
                        .execute();

                if(activities.size() > 0)
                {
                    lastQueryTime = (int)activities.get(activities.size() - 1).getStartDate().getTime();
                }
            }

            return activities;
        }

        protected void onPostExecute(List<com.sweetzpot.stravazpot.activity.model.Activity> result) {
            m_activities24h = result;

            m_onQuerySuccess.run();
        }
    }


    class StravaLogin extends AsyncTask<String, Void, LoginResult>
    {
        @Override
        protected LoginResult doInBackground(String... params) {
            try {
                AuthenticationConfig config = AuthenticationConfig.create().debug().build();
                AuthenticationAPI api = new AuthenticationAPI(config);
                loginResult = api.getTokenForApp(AppCredentials.with(CLIENT_ID, CLIENT_SECRET))
                        .withCode(params[0])
                        .execute();
            }
            catch(Exception e)
            {
                return null;
            }

            return loginResult;
        }

        protected void onPostExecute(LoginResult result) {
            if(result != null) {
                onSuccess(result);
            }
        }
    }

    public void getStuff(String CODE, int lastQueryTime, OnQuerySuccess callback)
    {
        this.lastQueryTime = lastQueryTime;
        this.m_onQuerySuccess = callback;

        new StravaLogin().execute(CODE);
    }
}
