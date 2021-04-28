package pl.ee.gameServer.service;

import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;
    public List<Player> listAllPlayer() {
        return playerRepository.findAll();
    }

    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    public Player getPlayer(UUID uuid) {
        return playerRepository.findById(uuid).get();
    }

    public void deletePlayer(UUID uuid) {
        playerRepository.deleteById(uuid);
    }
}