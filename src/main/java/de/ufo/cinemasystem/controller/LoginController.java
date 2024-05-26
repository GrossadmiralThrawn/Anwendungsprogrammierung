package de.ufo.cinemasystem.controller;



import de.ufo.cinemasystem.additionalfiles.LoginForm;
import de.ufo.cinemasystem.additionalfiles.RegistrationForm;
import de.ufo.cinemasystem.additionalfiles.UserService;
import de.ufo.cinemasystem.models.UserEntry;
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


@Controller
@RequestMapping("/lunar_space_port")
public class LoginController {
	UserRepository  userRepository;
	PasswordEncoder passwordEncoder;
	UserService     userService;


	public LoginController(UserRepository userRepository, PasswordEncoder passwordEncoder, UserService userService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
	}




	@PostMapping("/register")
	String register(@Valid RegistrationForm form, Errors result) {

		if (result.hasErrors()) {
			System.out.println(result.getAllErrors());
			return "registration";
		}



		System.out.println("Alles läuft.");
		userService.createUser(form);

		return "redirect:/";
	}



	@GetMapping("/register")
	String register(Model model, RegistrationForm form) {
		return "registration";
	}




	@PostMapping
	String login(@Valid LoginForm form, Errors result, HttpSession session) {
		UserEntry toSignInUser;



		if (result.hasErrors()) {
			System.out.println(result.getAllErrors());
			return "login";
		}


		toSignInUser = userService.login(form);

		if(toSignInUser != null)
		{
			session.setAttribute("user", form);
			return "redirect:/";
		}



		return "login";

	}





	@GetMapping(path = "/login")
	String login() {
		return "login";
	}




	@GetMapping(path = "/test")
	String test() {
		return "welcome";
	}





	@GetMapping("/customers")
	@PreAuthorize("hasRole('BOSS')")
	String customers(Model model) {

		model.addAttribute("customerList", userService.findAll());

		return "customers";
	}
}


