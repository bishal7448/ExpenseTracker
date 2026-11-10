package com.example.UserService.repository;

import com.example.UserService.entities.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserInfo, String> {

    Optional<UserInfo> findByUserId(String userid);

}
