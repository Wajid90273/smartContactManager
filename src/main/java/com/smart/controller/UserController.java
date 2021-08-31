package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEnable;
    
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model ,Principal principal) {
		
		String username=principal.getName();
		
		User user=userRepository.getUserByUserName(username);
		
		model.addAttribute("user", user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		return "normal/user_dashboard";
	}
	@GetMapping("/add-contact")
	public String openAddContact(Model model) {

		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal pricipal,HttpSession session) {

		try {

	        System.out.println("Data"+contact);
	        
	        String name=pricipal.getName();
	        User user=this.userRepository.getUserByUserName(name);
	        
	        if(file.isEmpty()) {
	        	
	        	System.out.println("File is Empty");
	        	contact.setImage("contact.jpg");
	        	session.setAttribute("message",new Message("File is Empty  !! Try Again","danger"));
	        }else {
	        	
	        	contact.setImage(file.getOriginalFilename());
	        	
	        	File saveFile=new ClassPathResource("static/img").getFile();
	        	
	        	Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	        	
	        	Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
	    
	        	System.out.println("Image is Uploaded");
	        	
	          	session.setAttribute("message",new Message("Your contact is added !! Add More..","success"));
	        }
	        
	        contact.setUser(user);
	        user.getContacts().add(contact);
	        this.userRepository.save(user);
			
		}catch(Exception e) {
			  System.out.println("Error"+e.getMessage());
			  e.printStackTrace();
			  session.setAttribute("message",new Message("Something went to wrong !! Try Again","danger"));
		}
	        
		
		return "normal/add_contact_form";
	}
	
	// show contact
	@GetMapping("/show-contacts/{page}")
	public String ShowContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		String userNameString =principal.getName();
		
		User user=this.userRepository.getUserByUserName(userNameString);
		
		Pageable pagable =PageRequest.of(page, 5);
		
		Page<Contact>contacts=this.contactRepository.findContactByUser(user.getId(),pagable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		m.addAttribute("title", "Show User Contacts");
		return "normal/show_contacts";
	}
	
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		System.out.println("cid"+cid);
		
		Optional<Contact>contactOptional=this.contactRepository.findById(cid);
		Contact contact=contactOptional.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
	
		
		model.addAttribute("contact", contact);
		
		return "normal/contact_detail"; 
	}
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model model,HttpSession session) {
		
		Optional<Contact>contactOptional=this.contactRepository.findById(cid);
		Contact contact=contactOptional.get();
		
		contact.setUser(null);
		
		this.contactRepository.delete(contact);
		
		session.setAttribute("message",new Message("Contact Detail Succesfully !!","success"));
		
		return "redirect:/user/show-contacts/0";
	}
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		m.addAttribute("title", "Update Contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String processUpdateContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,HttpSession session,Principal pricipal) {

		try {

	         Contact OldContact=this.contactRepository.findById(contact.getCid()).get();
	         
	        
//	        String name=pricipal.getName();

	        
	        if(!file.isEmpty()) {
	        	
	        	
	        	File deleteFile=new ClassPathResource("static/img").getFile();
	        	File file1=new File(deleteFile,OldContact.getImage());
	        	file1.delete();
	        	
	        	
	        	File saveFile=new ClassPathResource("static/img").getFile();
	        	Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	        	
	        	Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
	    
	        	contact.setImage(file.getOriginalFilename());
	        	
	          	
	        }else {	
	        	contact.setImage(OldContact.getImage());
	        }
	        
	        User user=this.userRepository.getUserByUserName(pricipal.getName());
	        contact.setUser(user);
	        this.contactRepository.save(contact);
	        session.setAttribute("message",new Message("Your contact Update sucessfuly !! Add More..","success"));
	        
			
		}catch(Exception e) {
			  System.out.println("Error"+e.getMessage());
			  e.printStackTrace();
		}
	        
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	@GetMapping("/settings")
	public String openSetting(Model model) {
		model.addAttribute("title","Setting Page");
		return "normal/settings";
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword ,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		 
		System.out.println("oldPassword"+oldPassword);
		System.out.println("newPassword"+newPassword);
		
		String username=principal.getName();
		
		User user=userRepository.getUserByUserName(username);
		
		if(this.bCryptPasswordEnable.matches(oldPassword, user.getPassword())) {
			
			user.setPassword(this.bCryptPasswordEnable.encode(newPassword));
			this.userRepository.save(user);
          	session.setAttribute("message",new Message("Your password is sucessfully change !!","success"));
		}else {
			
			session.setAttribute("message",new Message("Wrong old password !!","alert-danger"));
			
			return "redirect:/user/settings";
		}
		
		
		
		return "redirect:/user/index";
	}

}
