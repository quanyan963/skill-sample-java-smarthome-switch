import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

import static bean.Constants.ACCESS_KEY_ID;
import static bean.Constants.SECRET_KEY;

public class MyCredentialsProvider implements AWSCredentialsProvider {
    @Override
    public AWSCredentials getCredentials() {
        return MyCredentials.getInstance(ACCESS_KEY_ID,SECRET_KEY);
    }

    @Override
    public void refresh() {

    }
}
