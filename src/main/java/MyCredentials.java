import com.amazonaws.auth.AWSCredentials;

public class MyCredentials implements AWSCredentials {
    private static MyCredentials credentials;
    private String accessKeyId;
    private String secretKey;

    public static MyCredentials getInstance(String accessKeyId, String secretKey){
        if (credentials == null){
            credentials = new MyCredentials(accessKeyId,secretKey);
        }
        return credentials;
    }

    private MyCredentials(String accessKeyId, String secretKey) {
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
    }

    @Override
    public String getAWSAccessKeyId() {
        return accessKeyId;
    }

    @Override
    public String getAWSSecretKey() {
        return secretKey;
    }
}
