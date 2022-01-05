package com.helpdesk.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.model.Status;

@Repository
@Transactional
public interface StatusRepository extends JpaRepository<Status, Integer> {

}
