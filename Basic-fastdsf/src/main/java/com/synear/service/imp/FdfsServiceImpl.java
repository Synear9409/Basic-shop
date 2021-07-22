package com.synear.service.imp;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.synear.service.FdfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FdfsServiceImpl implements FdfsService {

    @Autowired
    private FastFileStorageClient fileStorageClient;


    @Override
    public String upload(MultipartFile file, String fileExtName) throws Exception {
        StorePath storePath = fileStorageClient.uploadFile(file.getInputStream(),
                                                            file.getSize(),
                                                            fileExtName,
                                                            null);
        return storePath.getFullPath();
    }


}
