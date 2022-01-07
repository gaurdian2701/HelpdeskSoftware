package com.helpdesk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.helpdesk.model.User;
import com.helpdesk.repository.RoleRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.service.UserService;

@Controller
@RequestMapping("/account")
public class AccountController {
	@Autowired
	RoleRepository repo;
    
	@Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @ModelAttribute("loggedInUser")
    public User populateUserDetails(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = userService.findUserByEmail(auth.getName());
        model.addAttribute("isUser", userService.isUser(loggedInUser));
        model.addAttribute("isAdmin", userService.isAdmin(loggedInUser));
        return loggedInUser;
    }
    
	 @RequestMapping(value = "", method = RequestMethod.GET) 
	 public String userSettings(Model model, @RequestParam("id") int id)
	 { 
		 User user = new User(); 
		 user = userRepository.findById(id); 
		 model.addAttribute("user",user);
		 return "stats"; 
	}
	 
}
