package com.helpdesk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import com.helpdesk.model.Stage;
import com.helpdesk.model.Status;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.repository.StatusRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.service.UserService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    UserService userService;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    StatusRepository statusRepository;
    
    @Autowired
    Ticket ticket1;

    private String id;

    //this allows the "logged in user" to have information populated into the methods, i.e. id, etc. I'm not entirely
    //sure it's necessary, but I haven't researched how to change it or where to move it so it isn't reused so often.
    @ModelAttribute("loggedInUser")
    public User populateUserDetails(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = userService.findUserByEmail(auth.getName());
        model.addAttribute("isUser", userService.isUser(loggedInUser));
        model.addAttribute("isAdmin", userService.isAdmin(loggedInUser));
        return loggedInUser;
    }

    //Opens the main ticket page, showing a list of all tickets and tickets assigned
    @RequestMapping
        public String ticketIndex(Model model, @ModelAttribute User loggedInUser){
        populateUserDetails(model);
        List<Ticket> openTickets = ticketRepository.findAll();
        model.addAttribute("openTickets", openTickets);

        return "ticket/index";
    }

    @RequestMapping(value = "all")
        public String ticketListAll(Model model, @ModelAttribute User loggedInUser){
        List<Ticket> openTickets = ticketRepository.findAll();
        model.addAttribute("openTickets", openTickets);
        return "ticket/all-tickets";
    }
    
    @RequestMapping(value = "myTicket", method = RequestMethod.GET)
    public String myOpenTicket(Model model, @ModelAttribute("loggedInUser") User loggedInUser)
    {
    	List<Ticket> chosenTickets = new ArrayList<Ticket>();
    	ArrayList<Integer> stages = ticketRepository.findStagesGivenId(loggedInUser.getId());
    	ArrayList<Integer> IDs = ticketRepository.findIdGivenId(loggedInUser.getId());
    	for(int i=0;i<stages.size();i++)
    	{
    		if(stages.get(i) == 0)
    		{
    			Optional<Ticket> optionalticket1 = ticketRepository.findById(IDs.get(i));
    			Ticket ticket1 = optionalticket1.get();
    			chosenTickets.add(ticket1);
    		}	
    	}
    	System.out.println(stages);

    	model.addAttribute("chosenTickets", chosenTickets);
    	return "ticket/myTicket";
}
    @RequestMapping(value = "myCTicket", method = RequestMethod.GET)
    public String myCloseTicket(Model model, @ModelAttribute("loggedInUser") User loggedInUser)
    {
    	List<Ticket> chosenTickets = new ArrayList<Ticket>();
    	ArrayList<Integer> stages = ticketRepository.findStagesGivenId(loggedInUser.getId());
    	ArrayList<Integer> IDs = ticketRepository.findIdGivenId(loggedInUser.getId());
    	for(int i=0;i<stages.size();i++)
    	{
    		if(stages.get(i) == 5)
    		{
    			Optional<Ticket> optionalticket1 = ticketRepository.findById(IDs.get(i));
    			Ticket ticket1 = optionalticket1.get();
    			chosenTickets.add(ticket1);
    		}	
    	}
    	model.addAttribute("chosenTickets", chosenTickets);
    	return "ticket/myCTicket";
}

    //shows form to create new tickets
    @GetMapping(value = "new")
    public String createNewTicket(Model model){
        
        Ticket newTicket = new Ticket();
        model.addAttribute(newTicket);
        return "ticket/new";
    }

    //sends ticket creation form, and forwards to the newly created page for the individual ticket
    @PostMapping(value = "new")
    public String processTicketCreation(Model model, @Valid Ticket newTicket, Errors errors){

        if(errors.hasErrors()){
            return "ticket/new";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = userService.findUserByEmail(auth.getName());
        Date currentDate = new Date();
        newTicket.setDateOpened(currentDate);
        newTicket.setCreatedBy(loggedInUser);
        newTicket.setStage(Stage.OPEN);
        newTicket.setAssignedTo(loggedInUser);
        ticketRepository.save(newTicket);

        return "redirect:/ticket/" + newTicket.getId();
    }


    //handles handles the viewing of an individual ticket
    @RequestMapping(value="{id}")
    public String ticketIndexSingle(Model model, @PathVariable("id") int id){
        Ticket activeTicket = ticketRepository.findById(id);
        Status newStatus = new Status();

        //creates a list of updates tied to that ticket, and then sorts them in reverse order,
        //so that the newest "status" is on top
        List<Status> updates = activeTicket.getUpdates();
        Collections.reverse(updates);

        model.addAttribute("ticket", activeTicket);
        model.addAttribute("statuses", activeTicket.getUpdates());
        model.addAttribute("status", newStatus);
        model.addAttribute("stages", Stage.values());

        //TODO: updates are not showing in date order, need to ensure they are date-sorted

        return "ticket/ticket-detail";
    }

    //processes adding of a new status
    @PostMapping(value = "{id}/add")
    public String addStatusUpdate(Model model, @PathVariable("id") int id, @Valid Status newStatus){
        Ticket activeTicket = ticketRepository.findById(id);
        User activeUser = populateUserDetails(model);
        newStatus.setAuthor(activeUser.getEmail());
        activeTicket.addUpdate(newStatus);
        activeTicket.setLastUpdated(new Date());
        statusRepository.save(newStatus);
        return "redirect:/ticket/" + id;
    }
    
    @RequestMapping(value = "AssignedTo", method = RequestMethod.GET)
    @Transactional
    public String processTicketUpdateAssignedTo(@RequestParam(value="id") int id, @ModelAttribute("loggedInUser") User loggedInUser){

        ticket1 = ticketRepository.findById(id);
        System.out.println("Hi!");
        ticket1.getAssignedTo().setEmail(loggedInUser.getEmail());
        ticketRepository.save(ticket1);

        return "redirect:/ticket/" + id;
    }

    //processes the updating of specific information tied to the initial ticket when it was created, i.e. the
    //description or stage
    @PostMapping(value = "{id}/update")
    public String processTicketUpdate(Model model, @PathVariable("id") int id, @Valid Ticket ticket, Errors errors){

        Ticket activeTicket = ticketRepository.findById(id);

        activeTicket.setDescription(ticket.getDescription());
        activeTicket.setStage(ticket.getStage());
        System.out.println("Hi!");
        ticketRepository.save(activeTicket);

        return "redirect:/ticket/" + id;
    }

    //processes ticket closure
    @PostMapping(value = "{id}/close")
        public String processTicketClosure(Model model, @PathVariable("id") int id, @Valid Ticket ticket, Errors errors){

        Ticket activeTicket = ticketRepository.findById(id);
        activeTicket.setStage(Stage.CLOSED);
        activeTicket.setDateClosed(new Date());
        ticketRepository.save(activeTicket);

        return "redirect:/ticket/";
    }

    //enables the ability to reopen tickets when they have been closed
    @PostMapping(value = "{id}/reopen")
    public String processTicketReopen(Model model,@PathVariable("id") int id, @Valid Ticket ticket, Errors errors)
    {
        Ticket activeTicket = ticketRepository.findById(id);
        activeTicket.setStage(Stage.OPEN);
        activeTicket.setDateClosed(null);
        return "redirect:/ticket/" + id;
    }



}
