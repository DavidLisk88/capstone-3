package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.sql.SQLException;

@RestController
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private ProfileDao profileDao;
    private UserDao userDao;

    @Autowired
    public ProfileController (UserDao userDao, ProfileDao profileDao){
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userDao.getByUserName(username);
    }

    @GetMapping
    public Profile getByUserId(){
try {
    User user = getCurrentUser();

    Profile profile = profileDao.getByUserId(user.getId());

    if (profile == null){
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
    }
    return profileDao.getByUserId(profile.getUserId());

} catch (Exception e){
    throw new ResponseStatusException( HttpStatus.INTERNAL_SERVER_ERROR, "Error");
}
    }




    @PostMapping
    public Profile create(@RequestBody Profile profile) {
        try {
            User user = getCurrentUser();
            profile.setUserId(user.getId());

            Profile existingProfile = profileDao.getByUserId(user.getId());
            if (existingProfile != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists");
            }

            return profileDao.create(profile);

        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating profile");
        }
    }

    @PutMapping
    public void update (@RequestBody Profile profile){
        try{
            User user = getCurrentUser();
            profile.setUserId(user.getId());

            profileDao.updateProfile(profile);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error");
        }
    }


}
