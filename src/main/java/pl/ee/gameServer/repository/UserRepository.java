package pl.ee.gameServer.repository;

import pl.ee.gameServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
