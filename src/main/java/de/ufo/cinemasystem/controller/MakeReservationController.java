
package de.ufo.cinemasystem.controller;

import de.ufo.cinemasystem.additionalfiles.AdditionalDateTimeWorker;
import de.ufo.cinemasystem.additionalfiles.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.ufo.cinemasystem.models.CinemaShow;
import de.ufo.cinemasystem.models.CinemaShowService;
import de.ufo.cinemasystem.models.Reservation;
import de.ufo.cinemasystem.models.Seat;
import de.ufo.cinemasystem.models.Ticket;
import de.ufo.cinemasystem.repository.CinemaShowRepository;
import de.ufo.cinemasystem.repository.ReservationRepository;
import de.ufo.cinemasystem.repository.TicketRepository;
import de.ufo.cinemasystem.repository.UserRepository;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Spring MVC Controller for making reservations.
 * @author Jannik Schwaß
 * @version 1.0
 */
@Controller
public class MakeReservationController {
    
    /**
     * session key of where we store an in-progress reservation.
     */
    public static final String reservationSessionKey = "current-reservation";
    /**
     * session key where we store privilege information.
     */
    public static final String privilegedReservationKey = "current-reservation-privileged";
    
    private @Autowired ReservationRepository repo;
    private @Autowired CinemaShowRepository showsRepo;
    private @Autowired UserRepository uRepo;
    private @Autowired TicketRepository ticketRepo;
    private @Autowired CinemaShowService showService;
    
    /**
     * Entry point from the main nav menu.
     * @param m
     * @return 
     */
    @GetMapping("/reserve-spots/reserve")
    public String startReservation(Model m){
        m.addAttribute("title", "Plätze reservieren");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.plusDays(7);
        List<CinemaShow> toOffer = showsRepo.findCinemaShowsInWeek(now.getYear(), AdditionalDateTimeWorker.getWeekOfYear(now)).toList();
        //unhinge any wannabe-unmodifyables by making a copy to a known-writable list type.
        toOffer=new ArrayList<>(toOffer);
        toOffer.addAll(showsRepo.findCinemaShowsInWeek(next.getYear(), AdditionalDateTimeWorker.getWeekOfYear(next)).toList());
        Iterator<CinemaShow> iterator = toOffer.iterator();
        while(iterator.hasNext()){
            CinemaShow cs= iterator.next();
            if(LocalDateTime.now().until(cs.getStartDateTime(), ChronoUnit.MILLIS) < Duration.ofMinutes(45).toMillis()){
                iterator.remove();
            }
        }
        m.addAttribute("shows", toOffer);
        return "make-reservation-cinema-show-selection";
    }
    
    /**
     * Reservation entry point when the links on current-films are clicked.
     * @param m
     * @param what
     * @param currentUser
     * @param session
     * @return 
     */
    @GetMapping("/reserve-spots/reserve/{what}")
    public String startReservation(Model m, @PathVariable CinemaShow what, @AuthenticationPrincipal UserDetails currentUser, HttpSession session){
        if(session.getAttribute(reservationSessionKey) == null){
            session.setAttribute(reservationSessionKey, new Reservation(uRepo.findByUserAccountUsername(currentUser.getUsername()), what));
            session.setAttribute(privilegedReservationKey, currentUser.getAuthorities().toArray()[0] != UserService.USER_ROLE);
        }
        Reservation work = (Reservation) session.getAttribute(reservationSessionKey);
        if(!work.getCinemaShow().equals(what)){
            deleteTickets(work);
            work = new Reservation(work.getReservingAccount(), what);
            session.setAttribute(reservationSessionKey, work);
            session.setAttribute(privilegedReservationKey, currentUser.getAuthorities().toArray()[0] != UserService.USER_ROLE);
        }
        m.addAttribute("title", "Plätze reservieren");
        m.addAttribute("tickets", work.getTickets());
        m.addAttribute("show", work.getCinemaShow());
        m.addAttribute("price",work.getTotalPrice());
        m.addAttribute("isPrivileged", session.getAttribute(privilegedReservationKey));
        if(LocalDateTime.now().until(what.getStartDateTime(), ChronoUnit.MILLIS) < Duration.ofMinutes(45).toMillis()){
            m.addAttribute("errors", "Reservierungen sind nur bis 45 Minuten vor Vorstellungsbeginn möglich!");
        }
        return "make-reservation-ticket-adder";
    }
    
    /**
     * Form submit of the film selection form.
     * @param m
     * @param what 
     * @param currentUser 
     * @param session 
     * @return the view name
     */
    @PostMapping("/reserve-spots/reserve")
    public String onShowSelected(Model m, @RequestParam("event") CinemaShow what, @AuthenticationPrincipal UserDetails currentUser, HttpSession session){
        if(session.getAttribute(reservationSessionKey) == null){
            System.out.println("[MakeReservationController] UserDetails type:" + currentUser.getClass().getName());
            System.out.println("[MakeReservationController] UserDetails right: " + currentUser.getAuthorities());
            session.setAttribute(reservationSessionKey, new Reservation(uRepo.findByUserAccountUsername(currentUser.getUsername()), what));
            session.setAttribute(privilegedReservationKey, currentUser.getAuthorities().toArray()[0] != UserService.USER_ROLE);
        }
        Reservation work = (Reservation) session.getAttribute(reservationSessionKey);
        if(!work.getCinemaShow().equals(what)){
            deleteTickets(work);
            work = new Reservation(work.getReservingAccount(), what);
            session.setAttribute(reservationSessionKey, work);
            session.setAttribute(privilegedReservationKey, currentUser.getAuthorities().toArray()[0] != UserService.USER_ROLE);
        }
        m.addAttribute("title", "Plätze reservieren");
        m.addAttribute("tickets", work.getTickets());
        m.addAttribute("show", work.getCinemaShow());
        m.addAttribute("price",work.getTotalPrice());
        m.addAttribute("isPrivileged", session.getAttribute(privilegedReservationKey));
        if(LocalDateTime.now().until(what.getStartDateTime(), ChronoUnit.MILLIS) < Duration.ofMinutes(45).toMillis()){
            m.addAttribute("errors", "Reservierungen sind nur bis 45 Minuten vor Vorstellungsbeginn möglich!");
        }
        return "make-reservation-ticket-adder";
    }
    
    
    /**
     * Form submit of the addTicket form.
     * @param m
     * @param session
     * @param ticketType
     * @return 
     */
    @PostMapping("/reserve-spots/add-ticket")
    public String addTicketToReservation(Model m, HttpSession session, @RequestParam("ticketType") String ticketType, @RequestParam("spot") String spot){
        if(session.getAttribute(reservationSessionKey) == null){
            return "redirect:/reserve-spots/reserve";
        }
        spot = spot.trim().toUpperCase();
        List<String> errors = new ArrayList<>();
        Reservation work = (Reservation) session.getAttribute(reservationSessionKey);
        
        if(!PatternHolder.validSeat.matcher(spot).matches()){
            errors.add("Ungültiger Sitzplatz: " + spot);
        }
        if(errors.isEmpty()&&!work.getCinemaShow().containsSeat(toRowID(spot), Integer.parseInt(spot.substring(1)))){
            errors.add("Ungültiger Sitzplatz: " + spot);
        }
        if(LocalDateTime.now().until(work.getCinemaShow().getStartDateTime(), ChronoUnit.MILLIS) < Duration.ofMinutes(45).toMillis()){
            errors.add("Reservierungen sind nur bis 45 Minuten vor Vorstellungsbeginn möglich!");
        }
        try {
            if (errors.isEmpty()&&showsRepo.findById(work.getCinemaShow().getId()).orElseThrow().getOccupancy(toRowID(spot), Integer.parseInt(spot.substring(1))).orElseThrow() != Seat.SeatOccupancy.FREE) {
                errors.add("Sitzplatz nicht mehr verfügbar: " + spot);
            }
        } catch (NoSuchElementException ex) {
            //CinemaShow no longer exists.
            errors.add("Diese Veranstaltung wurde abgesagt");
        }
        if(toCategoryType(ticketType) == null){
            errors.add("Nicht existenter Kartentyp");
        }
        //10 ticket limit for non-staff
        if(work.getTickets().length == 10 && ! ((boolean) session.getAttribute(privilegedReservationKey))){
            errors.add("Es können nur maximal 10 Tickets im voraus reserviert werden. Sollten Sie legitimen Bedarf an einer größeren Reservierung haben, sprechen Sie bitte unser Kassenpersonal vor Ort an.");
        }
        System.out.println("u: " + work.getReservingAccount().getUserAccount());
        System.out.println("t: " + Arrays.toString(work.getTickets()));
        
        if(errors.isEmpty()){
            //add ticket
            Ticket t = new Ticket(toCategoryType(ticketType), work.getCinemaShow());
            t.setSeatID(100 * toRowID(spot) + Integer.parseInt(spot.substring(1)));
            work.addTicket(ticketRepo.save(t));
            showService.update(work.getCinemaShow()).setSeatOccupancy(new Seat(toRowID(spot), Integer.parseInt(spot.substring(1))), Seat.SeatOccupancy.RESERVED).save();
        }
        // else we had errors, do not add
        m.addAttribute("title", "Plätze reservieren");
        m.addAttribute("tickets", work.getTickets());
        m.addAttribute("show", work.getCinemaShow());
        m.addAttribute("errors",errors);
        m.addAttribute("price",work.getTotalPrice());
        m.addAttribute("isPrivileged", session.getAttribute(privilegedReservationKey));
        return "make-reservation-ticket-adder";
    }
    
    /**
     * Form submit of the removeTicket form.
     * @param m
     * @param session
     * @param ticket
     * @return model name or redirect url
     */
    @PostMapping("/reserve-spots/remove-ticket")
    public String removeTicketFromReservation(Model m, HttpSession session, @RequestParam("deleteCartEntry") Ticket ticket){
        if(session.getAttribute(reservationSessionKey) == null){
            return "redirect:/reserve-spots/reserve";
        }
        Reservation work = (Reservation) session.getAttribute(reservationSessionKey);
        System.out.println("u: " + work.getReservingAccount().getUserAccount());
        System.out.println("t: " + Arrays.toString(work.getTickets()));
        work.removeTicket(ticket);
        showService.update(work.getCinemaShow().getId()).setSeatOccupancy(new Seat(ticket.getSeatID() / 100, ticket.getSeatID() % 100), Seat.SeatOccupancy.FREE).save();
        m.addAttribute("title", "Plätze reservieren");
        m.addAttribute("tickets", work.getTickets());
        m.addAttribute("show", work.getCinemaShow());
        m.addAttribute("price",work.getTotalPrice());
        m.addAttribute("isPrivileged", session.getAttribute(privilegedReservationKey));
        if(LocalDateTime.now().until(work.getCinemaShow().getStartDateTime(), ChronoUnit.MILLIS) < Duration.ofMinutes(45).toMillis()){
            m.addAttribute("errors", "Reservierungen sind nur bis 45 Minuten vor Vorstellungsbeginn möglich!");
        }
        return "make-reservation-ticket-adder";
    }
    
    /**
     * form submit of the commit button.
     * @param redir
     * @param session
     * @return 
     */
    @PostMapping("/reserve-spots/commit")
    public String commitReservation(RedirectAttributes redir, HttpSession session){
        if(session.getAttribute(reservationSessionKey) == null){
            return "redirect:/reserve-spots/reserve";
        }
        Reservation work = (Reservation) session.getAttribute(reservationSessionKey);
        //No ticket? get out of here!
        if(work.getTickets().length == 0){
            return "redirect:/reserve-spots/reserve/" + work.getCinemaShow().getId();
        }
        //redirect to reserve, where the error will be displayed
        if(LocalDateTime.now().until(work.getCinemaShow().getStartDateTime(), ChronoUnit.MILLIS) < Duration.ofMinutes(30).toMillis()){
            return "redirect:/reserve-spots/reserve/" + work.getCinemaShow().getId();
        }
        System.out.println("u: " + work.getReservingAccount().getUserAccount());
        System.out.println("t: " + Arrays.toString(work.getTickets()));
        
        redir.addFlashAttribute("ok", "created");
        repo.save(work);
        session.removeAttribute(reservationSessionKey);
        session.removeAttribute(privilegedReservationKey);
        return "redirect:/my-reservations";
    }
    
    /**
     * converts a spot value to the corresponding row id.
     * @param spot
     * @return 
     */
    private static int toRowID(String spot){
        char rowChar = spot.charAt(0);
        return rowChar - 'A';
    }
    
    /**
     * Internal function to remove tickets from a reservation before the reservation is deleted.
     * Note: keep in sync with {@linkplain de.ufo.cinemasystem.controller.DeleteReservationController#deleteTickets(de.ufo.cinemasystem.models.Reservation) }
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

    /**
     * Turn the html ticket type constants (&lt;option&gt;-values to our real enums.
     * @param ticketType
     * @return real enum type or null
     */
    private static Ticket.TicketCategory toCategoryType(String ticketType) {
        return switch (ticketType) {
            case "adult" -> Ticket.TicketCategory.normal;
            case "child" -> Ticket.TicketCategory.children;
            case "disabled" -> Ticket.TicketCategory.reduced;
            default -> null;
        };
    }
    
    /**
     * lazely initialised since Pattern.compile is heavy.
     */
    private static class PatternHolder {
        
        /**
         * pattern describing a theoretically valid seat.
         */
        public static Pattern validSeat = Pattern.compile("[A-La-l]([0-9]|1[0-9])$",Pattern.CASE_INSENSITIVE);
    }
}
