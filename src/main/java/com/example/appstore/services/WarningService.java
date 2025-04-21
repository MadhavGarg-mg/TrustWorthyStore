package com.example.appstore.services;

import com.example.appstore.models.User;
import com.example.appstore.models.Warning;
import com.example.appstore.repository.WarningRepository;
import com.example.appstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarningService {

    private final WarningRepository warnRepo;
    private final UserRepository  userRepo;

    @Autowired
    public WarningService(WarningRepository warnRepo, UserRepository userRepo) {
        this.warnRepo = warnRepo;
        this.userRepo = userRepo;
    }

    /** Issue a warning and suspend after 3 */
    @Transactional
    public long issueWarning(User dev, String reason) {
        Warning w = new Warning();
        w.setDeveloper(dev);
        w.setReason(reason);
        warnRepo.save(w);

        long count = warnRepo.countByDeveloper(dev);
        if (count >= 3 && !dev.isSuspended()) {
            dev.setSuspended(true);
            userRepo.save(dev);
        }
        return count;
    }

    @Transactional
    public void clearWarnings(User dev) {
        warnRepo.deleteByDeveloper(dev);
    }
}
