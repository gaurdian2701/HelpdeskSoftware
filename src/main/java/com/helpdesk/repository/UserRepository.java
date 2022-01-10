package com.helpdesk.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.helpdesk.model.User;


@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findById(int id);
    
    User deleteById(int id);
    
    @Query(value = "SELECT role_id FROM helpdesk.user_role ur INNER JOIN helpdesk.user u ON u.id = ur.user_id AND u.id = :userID", nativeQuery = true)
    ArrayList<Integer> findRoleGivenId(@Param("userID") int userID);

}
