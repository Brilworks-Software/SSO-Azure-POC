package com.sso.azure.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sso.azure.DTO.UserAccessToken;
import com.sso.azure.DTO.UserDetail;
import com.sso.azure.DTO.UserDto;
import com.sso.azure.Repository.UserRepository;
import com.sso.azure.dao.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final String GRAPH_API_URL = "https://graph.microsoft.com/v1.0/me";
    private static final String SELECTED_FIELDS = "displayName,mail,userPrincipalName";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public UserDto fetchProfileFromAzureAndSave(UserAccessToken userAccessToken) {
        UserDetail userDetail = FetchUserProfile(userAccessToken);
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

    public UserDetail FetchUserProfile(UserAccessToken userAccessToken) {
        String apiUrl = GRAPH_API_URL + "?$select=" + SELECTED_FIELDS;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userAccessToken.getAccessToken());
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
}
