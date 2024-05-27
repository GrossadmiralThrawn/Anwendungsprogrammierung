package de.ufo.cinemasystem.controller;

import org.salespointframework.useraccount.UserAccount.UserAccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import de.ufo.cinemasystem.models.Bestellung;
import de.ufo.cinemasystem.models.CinemaShow;
import de.ufo.cinemasystem.repository.CinemaShowRepository;
import de.ufo.cinemasystem.repository.OrderRepository;
import de.ufo.cinemasystem.repository.ReservationRepository;
import de.ufo.cinemasystem.repository.SnacksRepository;
import de.ufo.cinemasystem.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class MakeOrderController {

	public static final String orderSessionKey = "current-reservation";

	private @Autowired OrderRepository orderRepo;
	private @Autowired ReservationRepository reservationRepo;
	private @Autowired SnacksRepository snacksRepository;
	private @Autowired CinemaShowRepository showsRepo;
	private @Autowired UserRepository userRepo;

	@GetMapping("/sell#tickets")
	public String startOrder(Model m, @PathVariable CinemaShow what, @AuthenticationPrincipal UserAccountIdentifier currentUser, HttpSession session) {

		if(session.getAttribute(orderSessionKey) == null){
            session.setAttribute(orderSessionKey, new Bestellung(currentUser));
        }

		Bestellung bestellung = (Bestellung) session.getAttribute(orderSessionKey);
        m.addAttribute("tickets", bestellung.getTickets());
        m.addAttribute("show", bestellung.());
		return "films-rental-renderer";
	}

}
