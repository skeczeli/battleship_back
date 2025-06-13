package com.example.demo.ranking;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RankingService {

    @Autowired
    private UserRepository userRepository;

    public List<PlayerRankingDTO> getRanking() {
        List<User> users = (List<User>) userRepository.findAll();

        // Calculamos el score y ordenamos
        List<PlayerRankingDTO> ranking = users.stream()
                .map(user -> new PlayerRankingDTO(
                        user.getUsername(),
                        (user.getWins() * 10) - (user.getLosses() * 10),
                        0 // inicializamos el ranking en 0, lo actualizamos después
                ))
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());

        // Asignamos la posición
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }

        return ranking;
    }

    public Optional<Integer> getScoreIfExists(String username) {
        return getRanking().stream()
                .filter(p -> p.getUsername().equals(username))
                .map(PlayerRankingDTO::getScore)
                .findFirst();
    }

}

