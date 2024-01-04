package com.sso.azure.Controller;

import com.sso.azure.DTO.UserAccessToken;
import com.sso.azure.DTO.UserDto;
import com.sso.azure.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public UserDto createUser(@RequestBody UserAccessToken userAccessToken) {
        return userService.fetchProfileFromAzureAndSave(userAccessToken);
    }
}
