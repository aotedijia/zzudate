package org.example.zzudate.service;

import org.example.zzudate.entity.Info;
import java.util.List;

public interface InfoService {
    int saveInfo(Info info);
    int updateInfo(Info info);
    int deleteInfo(String id);
    List<Info> listAll();
    List<Info> listByCategory(String category);
}
