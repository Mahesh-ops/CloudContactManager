package com.sagar.smartcontactmanager.controller;

import java.security.Principal;

import com.sagar.smartcontactmanager.dao.UserRepository;
import com.sagar.smartcontactmanager.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {

        //to get username we need object of dao i.e UserRepository.
        String userName = principal.getName();
		System.out.println("USERNAME " + userName);

        // get the user using username (Email)
        User user = userRepository.getUserByUserName(userName);
        System.out.println("USER " +user);

        //sending above user to user_dashboard
        model.addAttribute("user", user);

        return "normal/user_dashboard";
    }
}
