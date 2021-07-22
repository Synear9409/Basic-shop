package com.synear.service;

import com.synear.utils.PagedGridResult;

public interface ItemsESService {

    public PagedGridResult searchItems(String keywords,
                                       String sort,
                                       Integer page,
                                       Integer pageSize);

}
