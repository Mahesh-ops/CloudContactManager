package com.sagar.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import com.sagar.smartcontactmanager.dao.UserRepository;
import com.sagar.smartcontactmanager.entities.Contact;
import com.sagar.smartcontactmanager.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    public String processContact(
            @ModelAttribute Contact contact, 
            @RequestParam("profileImage") 
            MultipartFile file, 
            Principal principal) {

        try {
            String name = principal.getName(); //we will get name of only logged in user.
            User user = this.userRepository.getUserByUserName(name);

            //processing and uploading file.
            if(file.isEmpty()){
                //for developers only..
                System.out.println("Image File is empty...");
            } else {
                //upload file to folder and update name of contact.
                contact.setImage(file.getOriginalFilename());

                //now we require path to save file to which folder
                File saveFile = new ClassPathResource("static/img").getFile();

                //getting path name and appending name to file
                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename()+file.getName());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image uploded successfully!");

            }

            //this is bi-direction mapping contact to user and user to contact
            contact.setUser(user);
            user.getContacts().add(contact);

            //now before saving image to db, we will first upload file and save it to database.

            this.userRepository.save(user); //this line is saving data into database/

            System.out.println("DATA " +contact);
            System.out.println("Contact added to database successfully.");
        } catch(Exception e){
            System.out.println("ERROR!" +e.getMessage());
            e.printStackTrace();
        }
        return "normal/add_contact_form";
    }
}


/*
now before saving image to db, we will first upload file and save it to database.

2. Now this file will come to which variable?
@RequestParam("profileImage")

 Again this file has to save in some format right?
 -> for this we have object called MultipartFile file

3. Finally we will process that file.
*/