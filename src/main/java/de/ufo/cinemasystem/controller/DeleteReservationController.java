/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ufo.cinemasystem.controller;

import de.ufo.cinemasystem.models.CinemaShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.ufo.cinemasystem.models.Reservation;
import de.ufo.cinemasystem.models.Seat;
import de.ufo.cinemasystem.models.Ticket;
import de.ufo.cinemasystem.repository.ReservationRepository;
import de.ufo.cinemasystem.repository.TicketRepository;
import de.ufo.cinemasystem.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Jannik
 */
@Controller
public class DeleteReservationController {
    
    private @Autowired ReservationRepository repo;
    private @Autowired TicketRepository ticketRepo;
    private @Autowired CinemaShowService showService;
    private @Autowired UserRepository uRepo;
    
    @GetMapping("/my-reservations")
    //@PreAuthorize("USER")
    public String getReservations(Model m, @AuthenticationPrincipal UserDetails currentUser){
        m.addAttribute("title", "Meine Reservierungen");
        m.addAttribute("reservations", repo.findAllByUser(uRepo.findByUserAccountUsername(currentUser.getUsername())));
        return "reservation-list";
    }
    
    @PostMapping("/my-reservations/delete/")
    //@PreAuthorize("USER")
    public String getDeleteForm2(Model m, @RequestParam("reserveNumber") Reservation id, @AuthenticationPrincipal UserDetails currentUser){
        if(!id.getReservingAccount().getId().equals(uRepo.findByUserAccountUsername(currentUser.getUsername()).getId())){
            //do NOT leak the reservation context
            return "redirect:/my-reservations";
        }
        
        m.addAttribute("title", "Reservierung #"+id.getId());
        m.addAttribute("reservation", id);
        return "cancel-reservation";
    }
    
    @GetMapping("/cancel-reservation/{id}")
    //@PreAuthorize("USER")
    public String getDeleteForm(Model m, @PathVariable Reservation id, @AuthenticationPrincipal UserDetails currentUser){
        if(!id.getReservingAccount().getId().equals(uRepo.findByUserAccountUsername(currentUser.getUsername()).getId())){
            //do NOT leak the reservation context
            return "redirect:/my-reservations";
        }
        
        m.addAttribute("title", "Reservierung #"+id.getId());
        m.addAttribute("reservation", id);
        return "cancel-reservation";
    }
    
    @PostMapping("/cancel-reservation/{id}")
    //@PreAuthorize("USER")
    public String deleteReservation(@PathVariable Reservation id, @AuthenticationPrincipal UserDetails currentUser){
        if(!id.getReservingAccount().getId().equals(uRepo.findByUserAccountUsername(currentUser.getUsername()).getId())){
            //do NOT leak the reservation context
            return "redirect:/my-reservations";
        }
        
        deleteTickets(id);
        repo.delete(id);
        return "redirect:/my-reservations";
    }
    
    /**
     * Internal function to remove tickets from a reservation before the reservation is deleted.
     * @param rev the reservation
     */
    private void deleteTickets(Reservation rev){
        Ticket[] tickets = rev.getTickets();
        for(Ticket t:tickets){
            rev.removeTicket(t);
            rev = repo.save(rev);
            showService.update(rev.getCinemaShow().getId()).setSeatOccupancy(new Seat(t.getSeatID() / 100, t.getSeatID() % 100), Seat.SeatOccupancy.FREE).save();
            ticketRepo.delete(t);
        }
    }
}
