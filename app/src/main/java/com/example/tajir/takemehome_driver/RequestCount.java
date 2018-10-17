package com.example.tajir.takemehome_driver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RequestCount extends Service {
    public RequestCount() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
