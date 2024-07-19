package sharepoint;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class SharePointDownloader {

    private static final String TENANT_ID = "<your-tenant-id>";
    private static final String CLIENT_ID = "<your-client-id>";
    private static final String CLIENT_SECRET = "<your-client-secret>";
    private static final String SITE_URL = "<your-site-url>";
    private static final String DOCUMENT_LIBRARY = "<your-document-library>";
    private static final String FILE_PATH = "<your-file-path>";

    public static void main(String[] args) {
        try {
            String accessToken = getAccessToken();
            downloadFile(accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getAccessToken() throws IOException {
        String authUrl = "https://accounts.accesscontrol.windows.net/" + TENANT_ID + "/tokens/OAuth/2";
        String resource = "https://<your-tenant-name>.sharepoint.com";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(authUrl);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        StringEntity params = new StringEntity(
                "grant_type=client_credentials" +
                        "&client_id=" + CLIENT_ID + "@" + TENANT_ID +
                        "&client_secret=" + CLIENT_SECRET +
                        "&resource=" + resource,
                StandardCharsets.UTF_8);

        httpPost.setEntity(params);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            JSONObject json = new JSONObject(result);
            return json.getString("access_token");
        }
    }

    private static void downloadFile(String accessToken) throws IOException {
        String fileUrl = SITE_URL + "/_api/web/GetFileByServerRelativeUrl('/" + DOCUMENT_LIBRARY + "/" + FILE_PATH + "')/$value";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(fileUrl);
        httpGet.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try (InputStream inputStream = entity.getContent();
                     FileOutputStream fileOutputStream = new FileOutputStream("<local-path-to-save-file>")) {
                    int bytesRead;
                    byte[] buffer = new byte[8192];
                    while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }
}