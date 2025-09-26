package com.payment.service;


import com.payment.Repository.UserRepo;
import com.payment.dto.ReplaceEmailDto;
import com.payment.dto.ReplaceMobile;
import com.payment.entity.Card;
import com.payment.entity.NetBanking;
import com.payment.entity.User;
import com.payment.exceptions.EmailAlreadyRegistered;
import com.payment.exceptions.MobileNumberAlreadyExist;
import com.payment.exceptions.UserNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    public User addUser(User user) throws MobileNumberAlreadyExist {
        if(user.getMobileNumber() != null){
            if(userRepo.existsByMobileNumber((user.getMobileNumber()))){
                throw new MobileNumberAlreadyExist();
            };
        }

        if(user.getEmail() != null){
            if(userRepo.existsByEmail(user.getEmail())){
                throw new EmailAlreadyRegistered();
            }
        }

        return userRepo.save(user);
    }

    public User getUserById(Long id) throws UserNotFoundException {
        Optional<User> user = userRepo.findById(id);
        if(user.isPresent()){
            return user.get();
        }else{
            throw  new UserNotFoundException(user.get().getAccountId());
        }
    }

    public User replaceEmail(ReplaceEmailDto replaceEmailDto) throws EmailAlreadyRegistered, UserNotFoundException {
        Optional<User> user = userRepo.findById(replaceEmailDto.getId());
        if(user.isPresent()){
            User user1 = user.get();
            user1.setEmail(replaceEmailDto.getEmail());
            return userRepo.save(user1);

        }else{
            throw new UserNotFoundException(replaceEmailDto.getId());
        }
    }

    public User replaceMobileNo(ReplaceMobile replaceMobileDto) throws MobileNumberAlreadyExist {
        Optional<User> user = userRepo.findById(replaceMobileDto.getId());
        if(user.isPresent()){
            User user1 = user.get();
            user1.setMobileNumber(replaceMobileDto.getMobile());
            return userRepo.save(user1);
        }else{
            throw new MobileNumberAlreadyExist();
        }
    }



}
