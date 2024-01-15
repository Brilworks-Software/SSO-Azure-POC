package com.sso.azure.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.*;
import com.sso.azure.DTO.UserIdToken;
import com.sso.azure.DTO.UserDetail;
import com.sso.azure.DTO.UserDto;
import com.sso.azure.Repository.UserRepository;
import com.sso.azure.dao.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final String SELECTED_FIELDS = "displayName,mail,userPrincipalName";
    private static ObjectMapper objectMapper = new ObjectMapper();
    @Value("${azure.client.id}")
    private String azureClientId;
    @Value("${azure.client.secret}")
    private String azureClientSecret;
    @Value("${azure.authority.url}")
    private String azureAuthorityURL;
    @Value("${azure.scope}")
    private String azureScope;
    @Value("${azure.graph.url}")
    private String azureGraphUrl;

    public UserDto fetchProfileUsingIdTokenFromAzureAndSaveToDB(UserIdToken userIdToken) throws Exception {
        UserDetail userDetail = FetchUserProfileUsingIdToken(userIdToken);
        if(userDetail!=null) {
            User user = userRepository.findByEmail(userDetail.getUserPrincipalName());
            if (user == null) {
                user = new User();
                user.setEmail(userDetail.getUserPrincipalName());
                user.setName(userDetail.getDisplayName());
                user = userRepository.save(user);
            }
            return new UserDto(user.getId(), user.getName(), user.getEmail());
        }
        return null;
    }

    public UserDetail FetchUserProfileUsingIdToken(UserIdToken userIdToken) throws Exception {
        String accessToken = getAccessTokenFromIdToken(userIdToken.getIdToken());
        String apiUrl = azureGraphUrl + "?$select=" + SELECTED_FIELDS;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(apiUrl));

        ResponseEntity<String> responseEntity = new RestTemplate().exchange(requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String userProfile = responseEntity.getBody();
            if(StringUtils.hasText(userProfile)) {
                return StringToObject(userProfile, UserDetail.class);
            }
            return null;
        } else {
            System.err.println("Error accessing Microsoft Graph API. Status code: " + responseEntity.getStatusCode());
            return null;
        }


    }

    public static <T> T StringToObject(String jsonString, Class<T> valueType){
        try {
            if(StringUtils.hasText(jsonString)){
                return objectMapper.readValue(jsonString, valueType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private IAuthenticationResult acquireTokenUsingIdTokenMSAL(String idToken, String scope)
            throws MalformedURLException, ServiceUnavailableException, InterruptedException, ExecutionException, UnsupportedEncodingException {

        ConfidentialClientApplication clientApplication = ConfidentialClientApplication.builder(
                        azureClientId,
                        ClientCredentialFactory.createFromSecret(azureClientSecret))
                .authority(azureAuthorityURL)
                .build();

        if (scope == null) {
            URLEncoder.encode("openid offline_access profile", "UTF-8");
        } else {
            URLEncoder.encode("openid offline_access profile" + " " + scope, "UTF-8");
        }

        Set<String> scopes = new HashSet<>(Arrays.asList(StringUtils.split(scope, " ")));
        UserAssertion assertion = new UserAssertion(idToken);

        OnBehalfOfParameters params = OnBehalfOfParameters.builder(scopes, assertion).build();
        CompletableFuture<IAuthenticationResult> future = clientApplication.acquireToken(params);

        IAuthenticationResult result = future.get();

        if (result == null) {
            throw new ServiceUnavailableException("unable to acquire on-behalf-of token for client ");
        }
        return result;
    }

    public String getAccessTokenFromIdToken(String idToken) throws Exception {
        try {
            // Microsoft Graph user endpoint
            IAuthenticationResult authenticationResult = acquireTokenUsingIdTokenMSAL(idToken, azureScope);

            return authenticationResult.accessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
