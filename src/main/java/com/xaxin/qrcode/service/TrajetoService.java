package com.xaxin.qrcode.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xaxin.qrcode.model.Checkpoint;
import com.xaxin.qrcode.model.QrNumber;
import com.xaxin.qrcode.repository.CheckpointRepository;
import com.xaxin.qrcode.repository.QrNumberRepository;

@Service
public class TrajetoService {

    private final CheckpointRepository checkpointRepository;
    private final QrNumberRepository qrNumberRepository;
    private final AlbumService albumService;
    private final com.xaxin.qrcode.service.UserService userService;

    public TrajetoService(CheckpointRepository checkpointRepository,
                          QrNumberRepository qrNumberRepository,
                          AlbumService albumService,
                          com.xaxin.qrcode.service.UserService userService) {
        this.checkpointRepository = checkpointRepository;
        this.qrNumberRepository = qrNumberRepository;
        this.albumService = albumService;
        this.userService = userService;
    }

    /**
     * Retorna a lista dos 4 checkpoints ordenados.
     */
    public List<Checkpoint> listarCheckpoints() {
        return checkpointRepository.findAllByOrderByOrdemAsc();
    }

    /**
     * Valida se um QR code lido é um dos 4 checkpoints válidos.
     * Se for, salva no banco (qr_numbers) e retorna o checkpoint.
     * Retorna null se o QR code não for um checkpoint válido.
     */
    @Transactional
    public Checkpoint validarELancarQrCode(String codigoLido, String username) {
        Checkpoint checkpoint = checkpointRepository.findByCodigo(codigoLido).orElse(null);
        if (checkpoint == null) {
            return null;
        }

        // Salva o número (código) lido no banco, associado ao usuário quando disponível
        com.xaxin.qrcode.model.User user = null;
        if (username != null) {
            user = userService.buscarPorUsername(username).orElse(null);
        }

        try {
            int numero = Integer.parseInt(codigoLido);
            qrNumberRepository.save(new com.xaxin.qrcode.model.QrNumber(numero, user));
        } catch (NumberFormatException e) {
            qrNumberRepository.save(new com.xaxin.qrcode.model.QrNumber(-1, user));
        }

        // Depois de salvar, se usuário existe, verifica completude e marca álbum
        if (user != null) {
            Map<String, Object> progresso = getProgresso(username);
            Boolean completo = (Boolean) progresso.get("completo");
            if (Boolean.TRUE.equals(completo)) {
                albumService.marcarRotaCompleta(user);
            }
        }

        return checkpoint;
    }

    /**
     * Mostra o progresso atual: quais checkpoints já foram lidos.
     */
    public Map<String, Object> getProgresso() {
        return getProgresso(null);
    }

    public Map<String, Object> getProgresso(String username) {
        List<Checkpoint> todos = listarCheckpoints();

        List<QrNumber> lidos;
        if (username == null) {
            lidos = qrNumberRepository.findAll();
        } else {
            lidos = qrNumberRepository.findAllByUserUsername(username);
        }

        List<Integer> numerosLidos = lidos.stream()
                .map(QrNumber::getNumero)
                .collect(Collectors.toList());

        List<Map<String, Object>> checkpoints = todos.stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("ordem", c.getOrdem());
            item.put("codigo", c.getCodigo());
            item.put("titulo", c.getTitulo());
            try {
                item.put("lido", numerosLidos.contains(Integer.parseInt(c.getCodigo())));
            } catch (NumberFormatException e) {
                item.put("lido", false);
            }
            return item;
        }).collect(Collectors.toList());

        long lidosCount = checkpoints.stream().filter(m -> (boolean) m.get("lido")).count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", todos.size());
        result.put("lidos", lidosCount);
        result.put("completo", lidosCount == todos.size());
        result.put("checkpoints", checkpoints);

        return result;
    }
}