package com.redstrings.backend;

import com.redstrings.backend.model.Role;
import com.redstrings.backend.model.User;
import com.redstrings.backend.model.UserRole;
import com.redstrings.backend.repository.RoleRepository;
import com.redstrings.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Autowired
	private UserService userService;

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public void run(String... args) throws Exception {
		User user1 = new User();
		user1.setFirstName("Admin");
		user1.setLastName("Admin");
		user1.setUsername("Admin");
		user1.setPassword("password");
		user1.setEmail("Admin@gmail.com");
		Set<UserRole> userRoles = new HashSet<>();
		Role role1 = new Role();
		role1.setName("ROLE_ADMIN");
		userRoles.add(new UserRole(user1, role1));
		userService.createUser(user1, userRoles, "regular");

		userRoles = new HashSet<>();
		User user2 = new User();
		user2.setFirstName("Ben");
		user2.setLastName("Franklin");
		user2.setUsername("BFranklin");
		user2.setPassword("password");
		user2.setEmail("BFranklin@gmail.com");
		Role role2 = new Role();
		role2.setName("ROLE_USER");
		userRoles.add(new UserRole(user2, role2));
		userService.createUser(user2, userRoles, "regular");

	}
}
