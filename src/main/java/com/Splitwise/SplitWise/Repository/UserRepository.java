package com.Splitwise.SplitWise.Repository;

import com.Splitwise.SplitWise.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
