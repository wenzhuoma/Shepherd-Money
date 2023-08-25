package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class UserController {

    /**
     *user repository
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * create user API
     * @param payload
     * @return response
     */
    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        //create an user entity with information given in the payload
        User user = new User();
        user.setName(payload.getName());
        user.setEmail(payload.getEmail());

        try {
            //store user in the database
            userRepository.save(user);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }


        //return the id of the user in 200 OK response
        return ResponseEntity.ok(user.getId());
    }

    /**
     * delete user API
     * @param userId
     * @return response
     */
    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        //get user by user id in the database
        Optional<User> user = userRepository.findById(userId);

        if(user.isPresent()){
            try {
                //delete the user by id in the datebase
                userRepository.deleteById(userId);
            }catch (Exception e){
                return ResponseEntity.badRequest().body(userId + " can not be deleted with credit card!");
            }
            //return 200 OK  if a user with thd given ID exists, and the deletion is successful
            return ResponseEntity.ok(userId + " is deleted.");
        }else{
            // return 400 Bad Request if user with userId does not exist
            return ResponseEntity.badRequest().body(userId + " does not exist!");
        }
      }
}
