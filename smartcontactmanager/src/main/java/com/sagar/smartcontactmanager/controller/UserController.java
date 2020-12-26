package com.sagar.smartcontactmanager.controller;

import java.security.Principal;

import com.sagar.smartcontactmanager.dao.UserRepository;
import com.sagar.smartcontactmanager.entities.Contact;
import com.sagar.smartcontactmanager.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    //Adding common data to response, making method to add all users
    @ModelAttribute //now this works for all i.e for /index and /add-contact also
    public void addCommonData(Model model, Principal principal){ //addCommanData will work for both index as well as add-contact
        //to get username we need object of dao i.e UserRepository.
        String userName = principal.getName();
		System.out.println("USERNAME " + userName);

        // get the user using username (Email)
        User user = userRepository.getUserByUserName(userName);
        System.out.println("USER " +user);

        //sending above user to user_dashboard
        model.addAttribute("user", user);
    }
    
    // dashboard Home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {

        return "normal/user_dashboard";
    }


    //open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", "new Contact()"); //adding new blank object contact
        return "normal/add_contact_form";
    }

    //adding add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, Principal principal) {

        try {
            String name = principal.getName(); //we will get name of only logged in user.
            User user = this.userRepository.getUserByUserName(name);

            //this is bi-direction mapping contact to user and user to contact
            contact.setUser(user);
            user.getContacts().add(contact);

            this.userRepository.save(user);

            System.out.println("DATA " +contact);
            System.out.println("Contact added to database successfully.");
        } catch(Exception e){
            System.out.println("ERROR!" +e.getMessage());
            e.printStackTrace();
        }
        return "normal/add_contact_form";
    }
}
