package com.gabchak.weather.services;

import com.gabchak.weather.dto.enums.Roles;
import com.gabchak.weather.dto.jwt.RegisterRequest;
import com.gabchak.weather.dto.mappers.RegisterUserMapper;
import com.gabchak.weather.models.Role;
import com.gabchak.weather.models.User;
import com.gabchak.weather.repositories.UserRepository;
import com.gabchak.weather.services.security.jwt.JwtUser;
import com.gabchak.weather.services.security.jwt.JwtUserFactory;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RegisterUserMapper mapper;

  /**
   * Constructor of the class.
   *
   * @param userRepository  {@link UserRepository}
   * @param passwordEncoder {@link PasswordEncoder}
   * @param mapper          {@link RegisterUserMapper}
   */
  @Autowired
  public UserService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     RegisterUserMapper mapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.mapper = mapper;
  }

  /**
   * Saves a new user.
   *
   * @param user a new user data
   */
  public User save(User user) {
    return userRepository.save(user);
  }

  /**
   * Finds user by email.
   *
   * @param email user identifier
   * @return a new user if exists
   */
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  /**
   * Registers a new user.
   *
   * @param request {@link RegisterRequest}
   * @return a new {@link User}
   */
  public JwtUser register(RegisterRequest request) {
    User user = mapper.map(request, User.class);
    Role defaultRole = Role.fromEnum(Roles.FREE_USER);
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRoles(Collections.singletonList(defaultRole));
    User savedUser = userRepository.save(user);
    return JwtUserFactory.create(savedUser);
  }

  /**
   * The method update subscription date
   * if it is not exist of expired.
   *
   * @param email user identifier
   * @return date of subscription
   */
  public LocalDate subscribe(String email)
      throws UsernameNotFoundException {
    Optional<User> byEmail = userRepository.findByEmail(email);
    return byEmail.map(user -> {
      LocalDate paidBeforeDate = LocalDate.now().plusMonths(1);
      LocalDate currentDate = user.getPaidBeforeDate();
      if (currentDate == null || currentDate.isBefore(paidBeforeDate)) {
        HashSet<Role> roles = new HashSet<>(user.getRoles());
        roles.add(Role.fromEnum(Roles.PAID_USER));
        user.setRoles(roles);
        user.setPaidBeforeDate(paidBeforeDate);
        userRepository.save(user);
        return paidBeforeDate;
      }
      return currentDate;
    }).orElseThrow(() ->
        new UsernameNotFoundException(
            "User Not Found with email: " + email));
  }
}
