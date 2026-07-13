/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Packs;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.repository.PacksRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */
@Service
public class PackService {

    private PacksRepository packsRepository;

    public PackService(PacksRepository packsRepository) {
        this.packsRepository = packsRepository;
    }

    public Optional<Packs> findPackById(Long id) {
        Optional<Packs> pack = packsRepository.findById(id);
        if (pack.isEmpty()) {
            throw new SourceNotFound("id" + id);
        }
        
        return pack;
    }

    public List<Packs> retrievePacks() {
        return packsRepository.findAll();
    }
    
  

    public void deleteById(Long id) {
      Optional<Packs> pack = packsRepository.findById(id);
        if (pack.isEmpty()) {
            throw new SourceNotFound("id" + id);
        }
        
        packsRepository.deleteById(id);
    }
    
    public Packs createPack(Packs pack){
        return packsRepository.save(pack);
    }
    
}
