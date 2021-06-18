package pl.ee.gameServer.service;

import pl.ee.gameServer.model.Player;
import pl.ee.gameServer.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
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
        if(playerRepository.findById(uuid).isPresent()) return playerRepository.findById(uuid).get();
        return null;
    }

    public void deletePlayer(UUID uuid) {
        playerRepository.deleteById(uuid);
    }

    public boolean isPrivateTokenValid(UUID playerUuid, UUID token) {
        Player player = null;
        try {
            if (playerRepository.findById(playerUuid).isPresent())
                player = playerRepository.findById(playerUuid).get();
            assert player != null;
            return player.getPrivateToken().equals(token);
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
