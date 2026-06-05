package com.xaxin.qrcode.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.xaxin.qrcode.model.Checkpoint;
import com.xaxin.qrcode.repository.CheckpointRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CheckpointRepository checkpointRepository;

    public DataInitializer(CheckpointRepository checkpointRepository) {
        this.checkpointRepository = checkpointRepository;
    }

    @Override
    public void run(String... args) {
        // Só popula se a tabela estiver vazia
        if (checkpointRepository.count() > 0) {
            return;
        }

        checkpointRepository.save(new Checkpoint(1, "1001", "Checkpoint 1 - Início"));
        checkpointRepository.save(new Checkpoint(2, "2002", "Checkpoint 2 - Meio do trajeto"));
        checkpointRepository.save(new Checkpoint(3, "3003", "Checkpoint 3 - Quase lá"));
        checkpointRepository.save(new Checkpoint(4, "4004", "Checkpoint 4 - Final"));

        System.out.println(">>> 4 checkpoints padrão criados com sucesso!");
        System.out.println(">>> Códigos: 1001, 2002, 3003, 4004");
    }
}