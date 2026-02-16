package com.screenblock.backend.controller;

import com.screenblock.backend.model.BlockedApp;
import com.screenblock.backend.repository.BlockedAppRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocked-apps")
public class BlockedAppController {

    private final BlockedAppRepository repository;

    public BlockedAppController(BlockedAppRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{userId}")
    public List<BlockedApp> getBlockedApps(@PathVariable String userId) {
        return repository.findByUserId(userId);
    }

    @PostMapping
    public BlockedApp addBlockedApp(@RequestBody BlockedApp app) {
        return repository.save(app);
    }

    @PutMapping("/{id}")
    public BlockedApp updateBlockedApp(@PathVariable String id, @RequestBody BlockedApp app) {
        app.setId(id);
        return repository.save(app);
    }

    @DeleteMapping("/{id}")
    public void deleteBlockedApp(@PathVariable String id) {
        repository.deleteById(id);
    }
}
