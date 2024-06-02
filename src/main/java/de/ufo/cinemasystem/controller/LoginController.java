package de.ufo.cinemasystem.controller;



import de.ufo.cinemasystem.additionalfiles.RegistrationForm;
import de.ufo.cinemasystem.additionalfiles.LoginForm;
import de.ufo.cinemasystem.additionalfiles.RegistrationForm;
import de.ufo.cinemasystem.additionalfiles.UserService;
import de.ufo.cinemasystem.models.UserEntry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import de.ufo.cinemasystem.repository.UserRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class LoginController {
	UserRepository  userRepository;
	PasswordEncoder passwordEncoder;
	UserService     userService;


	public LoginController(UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}




	@PostMapping("/registration")
	String register(@Valid RegistrationForm form, Errors result, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			System.out.println(result.getAllErrors());
			return "registration";
		}

		System.out.println(form);

		userService.createUser(form);
		redirectAttributes.addFlashAttribute("createdUser", "Ein neuer Nutzer wurde erfolgreich angelegt");

		return "redirect:/login";
	}


	@GetMapping("/registration")
	String register(Model m, RegistrationForm form) {
		return "registration";
	}




	/*
	@PostMapping("/login")
	String login(@Valid LoginForm form, Errors result, HttpSession session, RedirectAttributes redirectAttributes) {

		return "redirect:/";
	}


	@GetMapping(path = "/login")
	String login() {
		return "login";
	}
	*/



	@GetMapping("/customers")
	//@PreAuthorize("hasRole('BOSS')")
	String customers(Model model) {
		List<UserEntry> userEntries = userService.findAll().toList();
		System.out.println(userEntries);
		model.addAttribute("customerList", userEntries);
		return "welcome";
	}


	@RequestMapping("/logout")
	public String logout(HttpServletRequest request) throws ServletException {
		request.logout();
		return "redirect:/";
	}



}


