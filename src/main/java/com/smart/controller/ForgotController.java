package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.services.EmailService;

@Controller
public class ForgotController {
	
	
	@Autowired
	private EmailService  emailService;
		

	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		
		return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		
		Random random=new Random(1000);

		int otp =random.nextInt(999999999);
		
		System.out.println("email"+email);
		
		String message = "<h1>OTP="+otp+"</h1>";
		String subject = "OTP From SCM";
		String to = email;
		String from = "sajidAli94571@gmail.com";

		boolean flag=this.emailService.sendEmail(subject, message,to,from);
		if(flag) {
			return "varify_otp";
		
		}else {
			session.setAttribute("message","check your email id !!");
			
			
			return "forgot_email_form";
		}
		
	}
}
