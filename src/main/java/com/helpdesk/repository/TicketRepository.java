package com.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Ticket findById(int id);
    List<Ticket> findByAssignedToId(int id);
    
    @Query(value = "SELECT stage FROM ticket WHERE created_by_id = :userID", nativeQuery = true)
    ArrayList<Integer> findStagesGivenId(@Param("userID") int userID);
    
    @Query(value = "SELECT id FROM ticket WHERE created_by_id = :userID", nativeQuery = true)
    ArrayList<Integer> findIdGivenId(@Param("userID") int userID);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ticket WHERE created_by_id = :userID", nativeQuery = true)
    void deleteUserTicket(@Param("userID") int userID);
}
