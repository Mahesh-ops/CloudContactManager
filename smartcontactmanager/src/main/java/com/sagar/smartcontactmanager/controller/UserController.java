package com.sagar.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.sagar.smartcontactmanager.dao.ContactRepository;
import com.sagar.smartcontactmanager.dao.UserRepository;
import com.sagar.smartcontactmanager.entities.Contact;
import com.sagar.smartcontactmanager.entities.User;
import com.sagar.smartcontactmanager.helper.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

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
            Principal principal,
            HttpSession session) {

        try {
            String name = principal.getName(); //we will get name of only logged in user.
            User user = this.userRepository.getUserByUserName(name);

            //processing and uploading file.
            if(file.isEmpty()){
                //for developers only..
                System.out.println("Image File is empty...");

                contact.setImage("image"); //setting default contact image
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
            System.out.println("Contact saved to database successfully.");

            //success message on contact saving.
            session.setAttribute("message", new Message("Contact Added Successfully!", "success"));


        } catch(Exception e){
            System.out.println("ERROR!" +e.getMessage());
            e.printStackTrace();

            //error message on contact not saving.
            session.setAttribute("message", new Message("Something Went Wrong!", "danger"));
        }
        return "normal/add_contact_form";
    }

    //show view contacts handler
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model  m, Principal principal){

        m.addAttribute("title", "View Contacts");

        String userName = principal.getName(); //getting email
        User user = this.userRepository.getUserByUserName(userName); //this returns user

        //making object for pagination
        Pageable pageable = PageRequest.of(page, 3);

        //this is going to return Page<Contact> of contacts not List<> of contacts
        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable); //using user, we can get their id.

        //adding pagination, getting list of contacts
        m.addAttribute("contacts", contacts); //getting current page (already getting earlier also)
        m.addAttribute("currentPage", page); //contacts per page.
        m.addAttribute("totalPages", contacts.getTotalPages()); //returns all pages 

        return "normal/show_contacts";
    }

    //showing particular contact details
    @GetMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) { //using cId we fetch contact details.
        
        System.out.println("CID " + cId);

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get(); //so now we will get only those contact who have pass that their id

        //solving security bug, get details of logged in user using Principal.
        String userName = principal.getName(); //getting email
        User user = this.userRepository.getUserByUserName(userName); //this returns user

        //sending this contact to view, we have to use Model
        //checking if user id is equal to contact id of only logged in user.
        if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

        return "normal/contact_detail";
    }

    //delete contact handler
    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session){

        Optional<Contact> contactoptional = this.contactRepository.findById(cId);
        Contact contact = contactoptional.get();

        //above contact can be delete of respective id using contactRepository 
        this.contactRepository.delete(contact);

        //checking for securely delete using id..Assingment
        System.out.println("Contact " +contact.getcId());

        //If contact is not deleting directly, then before delete we are going to unlink contact
        contact.setUser(null);

        //remove image after contact is deleted, hint get image path and name
        contact.getImage();

        this.contactRepository.delete(contact);
        System.out.println("Contact Deleted ");
        
        session.setAttribute("message", new Message("Contact deleted sucessfully...!", "success"));
        
        return "redirect:/user/show-contacts/0";
    }

	
}

