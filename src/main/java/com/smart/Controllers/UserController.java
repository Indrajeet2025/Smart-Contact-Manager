package com.smart.Controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ContactRepository contactRepository;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);
        model.addAttribute("user", user);
    }

    @GetMapping("/user/index")
    public String dashboard(Model model) {
        return "normal/user_dashboard"; // Return the user dashboard
    }

    @GetMapping("/user/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form"; // Return the add contact form
    }

    @PostMapping("/user/process-contact")
    public String processContact(@ModelAttribute Contact contact, 
                                  @RequestParam("profileImage") MultipartFile file, 
                                  Principal principal, HttpSession session) {
        try {
            String username = principal.getName();
            User user = userRepository.getUserByUserName(username);
            
            if (file.isEmpty()) {
                session.setAttribute("message", new Message("File is empty. Please upload a valid image.", "danger"));
                return "redirect:/user/add-contact"; // Redirect back to the add contact form
            }

            // Save the uploaded file to the specified directory
            contact.setImage(file.getOriginalFilename());
            File saveFile = new ClassPathResource("static/img").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Associate the contact with the user and save it
            contact.setUser(user);
            user.getContacts().add(contact);
            userRepository.save(user);

            session.setAttribute("message", new Message("Your contact has been added successfully!", "success"));
        } catch (Exception e) {
            session.setAttribute("message", new Message("Something went wrong! Please try again.", "danger"));
            e.printStackTrace(); // Log the exception for debugging
        }
        
        return "redirect:/user/add-contact"; // Redirect after processing
    }

    @GetMapping("/user/view-contacts/{page}")
    public String viewContacts(@PathVariable("page") Integer page,Model m,Principal principal ) {
        m.addAttribute("title", "View Contacts");
         
           String userName=  principal.getName();
          User user=this.userRepository.getUserByUserName(userName);
          
          Pageable pageable=PageRequest.of(page,5);
          
         Page<Contact> contacts= this.contactRepository.findContactsByUser(user.getId(),pageable);
          
          m.addAttribute("contacts", contacts);
          m.addAttribute("currentPage", page);
          
          m.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/view_contacts"; // Return the view contacts page
    }

    // Other methods for user profile, settings, etc. can be added here
}
