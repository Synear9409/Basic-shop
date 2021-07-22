package com.synear.controller;


import com.synear.service.ItemsESService;
import com.synear.utils.PagedGridResult;
import com.synear.utils.SYNEARJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

;


@RestController
@RequestMapping("items")
public class ElasticsearchController {

    @Autowired
    private ItemsESService itemsESService;

    @GetMapping("/search")
    public SYNEARJSONResult search(
                                    String keywords,
                                    String sort,
                                    @RequestParam(required = false, defaultValue = "1") Integer page,
                                    @RequestParam(required = false, defaultValue = "20")Integer pageSize){

        if (StringUtils.isBlank(keywords)) {
            return SYNEARJSONResult.errorMsg(null);
        }

//        page --;

        PagedGridResult gridResult = itemsESService.searchItems(keywords, sort, page, pageSize);

        return SYNEARJSONResult.ok(gridResult);

    }


}
