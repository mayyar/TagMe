package labelingStudy.nctu.minuku_2.service;

import android.app.job.JobParameters;
import android.app.job.JobService;

import labelingStudy.nctu.minuku.logger.Log;

public class JobSchedulerService extends JobService {

    private static final String TAG = "JobSchedulerService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "OnStartJob");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "OnStopJob");

        return false;
    }
}
