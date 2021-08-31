package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entites.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
    
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEnable;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home-Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About-Smart Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register-Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("You have not agreed the term and conditions");
				throw new Exception("You have not agreed the term and conditions");
			}

			if (result1.hasErrors()) {
				System.out.println("Error" + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEnable.encode(user.getPassword()));
			
			System.out.println("Agreement" + agreement);
			System.out.println("USER" + user);

			User result = this.userRepository.save(user);

			model.addAttribute("User", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("User", user);
			session.setAttribute("message", new Message("Something Went to wrong !!" + e.getMessage(), "alert-danger"));

		}
		return "signup";
	}

	@RequestMapping("/signin")
	public String signin(Model model) {
		model.addAttribute("title", "Login Page");
//		model.addAttribute("user", new User());
		return "login";
	}
	@RequestMapping("/loginfaild")
	public String loginFaild(Model model) {
		model.addAttribute("title", "Login faild");
		return "loginfaild";
	}
}
