package leltek.viewer;

import android.app.Application;
import android.content.Context;

/**
 * Created by rajesh on 4/10/17.
 */

public class ApplicationInitializer extends Application {
    static ApplicationInitializer applicationInitilizer;


    @Override
    public void onCreate() {
        super.onCreate();
        applicationInitilizer = this;
    }

    public static ApplicationInitializer getInstance() {
        return applicationInitilizer==null?new ApplicationInitializer():applicationInitilizer;
    }

    public Context getAppContext() {
        return applicationInitilizer.getApplicationContext();
    }
}
