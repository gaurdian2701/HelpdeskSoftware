package com.helpdesk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.helpdesk.model.Role;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.repository.RoleRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TicketRepository ticketRepository;

    @ModelAttribute("loggedInUser")
    public User populateUserDetails(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = userService.findUserByEmail(auth.getName());
        model.addAttribute("isUser", userService.isUser(loggedInUser));
        model.addAttribute("isAdmin", userService.isAdmin(loggedInUser));
        return loggedInUser;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String userHome(Model model, @ModelAttribute User loggedInUser){
        populateUserDetails(model);

        return "user/index";
    }

    @RequestMapping(value = "{id}")
    public String userPage(Model model, @PathVariable("id") int id){
        //TODO: create stats page
        return "user/stats";
    }

    @RequestMapping(value = "{id}/queue")
    @ResponseBody
    public List<Ticket> userQueue(Model model, @PathVariable("id") int id){
        List<Ticket> openTickets = ticketRepository.findByAssignedToId(id);
        return openTickets;
        
		/*
		 * model.addAttribute("userTickets", openTickets); //TODO: create user queue
		 * page return "user/queue";
		 */
    }
    
	/*
	 * @RequestMapping(value = "account", method = RequestMethod.GET) public String
	 * userSettings(Model model, @RequestParam("id") int id){ User user = new
	 * User(); user = userRepository.findById(id); model.addAttribute("user",user);
	 * return "stats"; }
	 */
}
