package com.helpdesk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.helpdesk.model.Role;
import com.helpdesk.model.User;
import com.helpdesk.repository.RoleRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.service.UserService;

import javassist.bytecode.Descriptor.Iterator;

import javax.transaction.Transactional;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	RoleRepository repo;
    
	@Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private TicketRepository ticketrepo;


    @ModelAttribute("loggedInUser")
    public User populateUserDetails(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = userService.findUserByEmail(auth.getName());
        model.addAttribute("isUser", userService.isUser(loggedInUser));
        model.addAttribute("isAdmin", userService.isAdmin(loggedInUser));
        return loggedInUser;
    }

    @RequestMapping(value = "")
    public String adminConsole(Model model, @ModelAttribute User loggedInUser){
        List<User> allUsers = userRepository.findAll();
        List<User> selectedUsers = new ArrayList<User>();
        ArrayList<Integer> userRoles;
        for(User user : allUsers)
        {
        	userRoles = userRepository.findRoleGivenId(user.getId());
        	if(userRoles.size()==1)
        		selectedUsers.add(user);
        }
        model.addAttribute("selectedUsers", selectedUsers);
        return "admin/users";
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String create(Model model, @ModelAttribute User loggedInUser){

        populateUserDetails(model);
        model.addAttribute("userName", loggedInUser.getFirstName() + " " + loggedInUser.getLastName());

        User newUser = new User();
        model.addAttribute("user", newUser);

        return "admin/create";
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public String createNewAccount(Model model, @Valid User user, BindingResult bindingResult){
        User exists = userService.findUserByEmail(user.getEmail());

        if(exists != null){
            bindingResult.rejectValue("email", "error.user", "This email already exists!");
        }
        if(bindingResult.hasErrors()){
            return "admin/create";
        } else {
            //TODO: allow user to login using either username or email
            char firstInitial = user.getFirstName().charAt(0);
            user.setUsername((firstInitial + user.getLastName()).toLowerCase());
            userService.saveUser(user);
            model.addAttribute("msg","User registered successfully");
            model.addAttribute("user", new User());
            return "admin/create";
        }
    }

    @RequestMapping(value = "users")
    public String manageUsers(Model model){
        List<User> allUsers = userRepository.findAll();
        model.addAttribute("allUsers", allUsers);
        return "admin/users";
    }

	/*
	 * @PostMapping(value = "users/add-role/{id}") public String addUserRoles(Model
	 * model, @PathVariable("id") int id){ User user = userRepository.findById(id);
	 * Set<Role> userRoles = user.getRoles(); userRoles.addAll(Arrays.asList(new
	 * Role[] {roleRepository.findById(3)})); userRepository.save(user); return
	 * "redirect:/admin/users"; }
	 */
    
    @ModelAttribute
    public String preLoad(Model model) {
    	List<Role> roleList = repo.findAll();
    	model.addAttribute("roleList", roleList);
    	model.addAttribute("role",new Role());
    	return "admin/users";
    }
    
	/*
	 * @RequestMapping(value = "account", method = RequestMethod.GET) public String
	 * userSettings(Model model, @RequestParam("id") int id){ User user = new
	 * User(); user = userRepository.findById(id); model.addAttribute("user",user);
	 * return "user/stats"; }
	 */
    
    @RequestMapping(value="deleteUser", method = RequestMethod.GET)
    @Transactional
    public String deleteUser(@RequestParam("id") int id)
    {
    	User user = new User();
    	user = userRepository.findById(id);
    	userRepository.delete(user);
    	ticketrepo.deleteUserTicket(id);
    	return "redirect:/admin/users";
    	
    }

}
